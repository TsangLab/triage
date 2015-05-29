package triage.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import triage.analyse.Extractor;
import triage.configure.ConfigConstants;
import triage.filter.NaiveFilter;

/**
 * Class to fetch PMIDs from query
 * 
 * @author Marie-Jean Meurs, Hayda Almeida
 * @since 2015
 */

public class PMIDExtractor extends Extractor{	
	

	public static void main(String[] args) throws IOException {

		ConfigConstants pathVars = new ConfigConstants();		
		NaiveFilter filter = new NaiveFilter();
		PMIDExtractor pmidExtractor = new PMIDExtractor();
		
		String pathToLiteratureFolder = pathVars.HOME_DIR + pathVars.CORPUS_DIR + "L" + pathVars.TARGET_LEVEL + "/"; 	
		String pathToListFile = pathVars.HOME_DIR + pathVars.ID_LIST;
		String previousLevel = pathVars.PREVIOUS_LEVEL;
		String targetLevel = pathVars.TARGET_LEVEL;
		String pathToNewListFile = pathVars.HOME_DIR + pathVars.ID_LIST + "List" + targetLevel + "_labeled.txt";
				
		HashMap<String,String> queryList = new HashMap<String,String>();		
		HashMap<String,String> keywordsList = new HashMap<String,String>();	
		HashMap<String,String> PMIDs = new HashMap<>();
		HashMap<String,String> RISIDs = new HashMap<>();
		HashMap<String,String> unreviewRISIDs = new HashMap<>();
		
		boolean searchTitleOnly = true;
		int countSearch = 0;
		
				
		File sourceDir = new File(pathToListFile);
		File[] srcTXT = sourceDir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name){
				return name.endsWith(".txt");
			}
		});		

		try{
			//for each file on the source dir 
			for (File txt : srcTXT){
				
				String filename = txt.toString().toLowerCase();
				
				if(filename.contains(targetLevel)){
					if(filename.contains("included")) pmidExtractor.retrieveRISQuery(txt, RISIDs, queryList, keywordsList, targetLevel);
					else if(filename.contains("unreview")) pmidExtractor.retrieveRISQuery(txt, unreviewRISIDs, queryList, keywordsList, targetLevel);
				}				
				else if(filename.contains(previousLevel)){
					if(filename.contains("fully"))
						pmidExtractor.retrieveRISQuery(txt, RISIDs, queryList, keywordsList, targetLevel);
				}				
			}
			
		}catch(Exception e){
			throw new RuntimeException(e);
		}
				
		filter.loadStopWords(pathVars.HOME_DIR + pathVars.STOP_LIST);
		
		int idsOnSource = PMIDs.size();	
		
		System.out.println("===== > IDs fetched in source file: " + idsOnSource + "\n"
				+ "===== > Titles fetched in source file: " + queryList.size() + "\n");
		
		pmidExtractor.writeLevelList(pathToNewListFile, targetLevel, searchTitleOnly, 
				filter, queryList, RISIDs, unreviewRISIDs, keywordsList);		
        		
		int idsFound = PMIDs.size()-idsOnSource;
		
		System.out.println("\n ===== > IDs found in file: " + idsFound);
		System.out.println("===== > Titles found in file: " + queryList.size());		
		
		pmidExtractor.retrieveFoundPMIDs(pathToNewListFile, PMIDs);
		
		pmidExtractor.retrievePubMedXML(PMIDs, RISIDs, pathToLiteratureFolder);					
		
	}	
	
	
	
	/**
	 * Retrieves and saves in literature folder 
	 * PubMed abstract XMLs corresponding to a list of PMIDs 
	 * @param PMIDs  a list of PMIDs
	 * @param RISIDs  a list of RISIDs
	 * @param pathToLiteratureFolder   location to save XMLs
	 * @throws IOException
	 */	
	public void retrievePubMedXML(HashMap<String,String> PMIDs, HashMap<String,String> RISIDs, String pathToLiteratureFolder) throws IOException{
		
		FileWriter resultFile;
		resultFile = new FileWriter("pmidlist");
		PrintWriter result = new PrintWriter(resultFile);
		
		for(Map.Entry<String,String> item :  PMIDs.entrySet()){
			String risID = item.getKey();
			String PMID = item.getValue();
			
			String label = RISIDs.get(risID);
			
			// fetch XML abstract
			String abstractContent = fetchXmlForPMID(PMID);
			FileWriter abstractFile;

			if(label.contains("positive")){
				abstractContent = abstractContent.replaceAll("</ArticleIdList>", "<TRIAGE>positive</TRIAGE>\n</ArticleIdList>");
				abstractFile = new FileWriter(pathToLiteratureFolder + "/positives/" + PMID + ".xml" );
			}
			else{
				abstractContent = abstractContent.replaceAll("</ArticleIdList>", "<TRIAGE>negative</TRIAGE>\n</ArticleIdList>");
				abstractFile = new FileWriter(pathToLiteratureFolder + "/negatives/" + PMID + ".xml");
			}

			PrintWriter abstractXML = new PrintWriter(abstractFile);
			//System.out.println(abstractXml);

			abstractXML.println(abstractContent);
			abstractXML.flush();
			abstractXML.close();

			// add PMID in list
			
			System.out.println(" ===== > File saved:\t" + PMID + "\t label: " + label );
		}

		result.close();
		System.out.println(" ===== > Finished!");		
		
	}
	
	
	/**
	 * 
	 * @param pathToListFile  location of list file
	 * @param targetLevel   the level to be considered (e.g., 1, 3)
	 * @return 
	 * @throws IOException
	 */
	public void writeLevelList(String pathToNewListFile, String targetLevel, boolean searchTitleOnly, NaiveFilter filter,
			HashMap<String,String> queryList, HashMap<String,String> RISIDs, 
			HashMap<String,String> unreviewRISIDs, HashMap<String,String> keywordsList) throws IOException{

		File listPath = new File(pathToNewListFile);

		FileWriter listFile = new FileWriter(listPath.toString());		
		PrintWriter listWriter = new PrintWriter(listFile);

		if(!listPath.exists()){

			listPath.createNewFile();
			listWriter.println("refMID \t PMID \t L" + targetLevel + "\t" + "title" + "\t" + "keywords");
			listWriter.flush();
		}

		//search queries in PubMed		
		for(Map.Entry<String,String> item : queryList.entrySet()){
			String risID = item.getKey();
			String query = item.getValue();			
			String label = RISIDs.get(risID);
			String line = "";
			String keywords = keywordsList.get(risID); if(keywords == null) keywords = "N/A";

			if(query.contains(" ")){
				if(!(unreviewRISIDs.containsKey(risID))){
					String resultXml = fetchPMID(query, searchTitleOnly, filter);
					String PMID = retrieveXmlPMID(query, label, resultXml, searchTitleOnly);

					if(!(PMID.length()>1)){		

						resultXml = fetchPMID(query, false, filter);
						PMID = retrieveXmlPMID(query, label, resultXml, false);					
					}

					if(PMID.length()<2) PMID = "not_found";

					line = generateLevelLine(risID, PMID, label, query, keywords);

					listWriter.println(line);
					listWriter.flush();

				}
				else System.out.println("\n ===== > " + risID + " not reviewed in L" + targetLevel + "\n"); 
			}
		}

		listWriter.close();							
	}
	
	
	public String generateLevelLine(String refMID, String PMID, String label, String title, String keywords){
						
		String line = refMID + "\t" + PMID + "\t" + label + "\t" + title + "\t" + keywords;
		
		return line;		
	}
	
	
	/**
	 * Reads a RIS file and extracts the 
	 * article titles for each entry
	 * @param file   path to RIS file
	 * @param RISIds   a list of RISIDs-label
	 * @param queryList   a list of article titles-label
	 * @param keyWords   a list of keywords in article-label
	 * @param targetLevel   the level to be considered (e.g., 1, 3)
	 */
	public void retrieveRISQuery(File file, HashMap<String,String> RISIds, HashMap<String,String> queryList, HashMap<String,String> keyWords, String targetLevel){
		//HashMap<String,String> RISIds = new HashMap<String,String>();
		boolean verbose = true;
		
		String path = file.toString();
		
		System.out.println("===== > Reading "+ file.getName() +" source file... \n");
		int counter = 0;
		int numIDs = 0;
		int queries = 0;
		
		try{

			BufferedReader reader = new BufferedReader(new FileReader(path));

			//String level = path.substring(path.indexOf("L"), path.indexOf("L")+2);
			
			String label = "";
			if(path.toLowerCase().contains("included") && path.contains(targetLevel)) label = "L" + targetLevel + "_positive";
			else label = "L" + targetLevel + "_negative";
			
			String line = "";	
			String entryID = "";
			String entryTitle = "";
			String entryKeywords = "";
			String query = "";
			
			
			//loading query list
			while((line = reader.readLine()) != null){					

				if(line.length()>1){
					
					//cleaning data for a new entry
					if(line.contains("TY  - ")){

						if(!entryID.isEmpty() && !query.isEmpty()){
							
							String listValue = RISIds.get(entryID);
							
							if(listValue == null){
								queryList.put(entryID, query);
								RISIds.put(entryID, label);	
							}				
							
							else if(label.contains("positive") && listValue.contains("negative")){								
								queryList.put(entryID, query);
								RISIds.put(entryID, label);								
							}

							if(verbose){
								System.out.println("\n" + entryID + "\t" + query);
								System.out.println("Keywords: " + entryKeywords);
							}

							line = "";	
							entryID = "";
							entryTitle = "";
							entryKeywords = "";
							query = "";	
						}
					}
					
					//fetching entry refManager ID
					if(line.contains("ID  - ")){			
						entryID = StringUtils.substringAfter(line,"  - ");
						//RISIds.put(entryID, label);
						counter++;
					}
					
					//fetching title
					if(line.contains("T1  - ")){						

						entryTitle = StringUtils.substringAfter(line,"  - ");
						while (!(line = reader.readLine()).contains("  - ")){
							entryTitle = entryTitle + " " + line;
						}

						query = entryTitle;

						//cleaning brackets on title
						if(query.contains("[")) 						
							query = query.substring(0, query.indexOf("["));						
						
						queries++;
						//populating query list					
											
					}
				
					//fetching entry keywords
					if(line.contains("KW  - ")){
						String[] keyW = null;
						
						entryKeywords = StringUtils.substringAfter(line,"  - ");
						while (!(line = reader.readLine()).contains("  - ")){
							entryKeywords = entryKeywords + " " + line;
						}

						entryKeywords = removeSpecialChar(entryKeywords);
						
						keyWords.put(entryID, entryKeywords);	
						
					}
				}
//				
			}

			reader.close();

		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 		

		System.out.println("\n ===== > Lines on " + file.getName() + " source file: " + counter + "\n");
		
		//return queryList;
	}
	
	/**
	 * Extracts matching ID from a given XML obtained
	 * after querying PubMed with article title
	 * @param query   article title
	 * @param label   article label (e.g., negative) 
	 * @param XML    XML returned by query search in PubMed
	 * @param searchTitleOnly    boolean to query for keywords only in title 
	 * @return
	 */
	public String retrieveXmlPMID(String query, String label, String XML, boolean searchTitleOnly){
				   
		   Document doc = Jsoup.parse(XML, "UTF-8");
		   Elements pmids = doc.getElementsByTag("Id");
		   String cleanPmid = "";

			//for (Element pmid : pmids) {		
				
				if(pmids.size() == 1){
					
					Element pmid = pmids.first();
				    cleanPmid = pmid.text().trim();
					
				//	this.IDs.put(cleanPmid, label);
				}		
				//extractedContent = cleanPmid;				
			//}				
			else{
				if(!searchTitleOnly)System.out.println(" ===== >  title not found: " + query + "\t" + "===== > label: " + label + "\t" + pmids.size()+ " results");
			}
				
			return cleanPmid;	
	}
	
	/**
	 * Fetch result file from PubMed website for a given query 
	 * @param query
	 * @param true if search is for an exact title
	 * @return
	 */
	public String fetchPMID(String query, boolean title, NaiveFilter filter) {    

		boolean verbose = false;
		String line = null;

		// build url to retrieve PMIDs
		String pubmedUrl = "";
		String newQuery = query;
		
		//Query normalization
		newQuery = removeSpecialChar(newQuery);
		newQuery = filter.removeStopList(StringUtils.split(newQuery," "));				
		newQuery = buildQuery(newQuery, title);
		
		
		pubmedUrl = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=" + newQuery; 
			
		if(verbose)System.out.println("\n" + pubmedUrl );

		// fetch PMID list
		try {
			URL url = new URL(pubmedUrl);

			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			line ="";
			String resultLines ="";
			
			while ((line = in.readLine()) != null) {
				if(!line.isEmpty()){
					line = line + "\n";
				}
				resultLines = resultLines + line;
			}
			//if(verbose) System.out.print(resultLines);
			in.close();
			return resultLines;
		}
		catch (Exception e)
		{
			return null; 
		}
	}
	
	/**
	 * Edit a query to search for an exact title
	 * @param query
	 * @return string query for title
	 */
	public String buildQuery(String query, boolean title){
		
		String querySplit[] = StringUtils.split(query," ");		
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0; i < querySplit.length; i++){
			sb.append(querySplit[i]);
			if(i < querySplit.length-1){			
				if(title)
					sb.append("[Title]+AND+");				
				else sb.append("+AND+");
			}
			else{
				if(title) sb.append("[Title]");				 
			}
		}
		
		
		return sb.toString();		
	}
	
	
	
	

	/**
	 * Fetch XML abstract from PubMed website for a given PMID 
	 * @param pmid
	 * @return
	 */
	public String fetchXmlForPMID(String pmid) {    

		boolean verbose = false;
		String line = null;

		// build url to retrieve PMIDs
		String pubmedUrl = "";

		pubmedUrl = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=" + pmid + "&retmode=xml";
		//pubmedUrl = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=" + query + "&retmax=100000";
		//System.out.print(pubmedUrl);


		// fetch PMID list
		try {
			URL url = new URL(pubmedUrl);

			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			line ="";
			String resultLines ="";
			while ((line = in.readLine()) != null) {
				if(!line.isEmpty()){
					line = line + "\n";
				}
				resultLines = resultLines + line;
			}
			if(verbose){System.out.print(resultLines);}
			in.close();
			return resultLines;
		}
		catch (Exception e)
		{
			return null; 
		}
	}
	
	
	/**
	 * Search for existing ID or query for each entry
	 * @param path for list file
	 * @return 
	 */
	
	public void retrieveFoundPMIDs(String path, HashMap<String,String> listPMIDs){
		
		boolean verbose = false;		
		
		System.out.println("===== > Reading source file... ");
		int counter = 0;
		int numIDs = 0;
		int queries = 0;
		
		try{

			BufferedReader reader = new BufferedReader(new FileReader(path));

			String line = null;	
			String [] entry = new String[4];
			
			
			//loading query list
			while((line = reader.readLine()) != null){
				entry = StringUtils.split(line,"\t");
				String label = "negative";
				String value ="";
				
				if(entry.length > 2)
					value = entry[entry.length-2];
					if (value.contains("true")) label = "positive";
				
				//checking if there is already an ID
				if(!NumberUtils.isDigits(entry[1])){
					String query = entry[entry.length-1];
					
					//cleaning brackets on title
					if(query.contains("[")) 						
						query = query.substring(0, query.indexOf("["));
					
					if(verbose)System.out.println(query);
					
					//populating query list
					//this.queryList.put(query,label);
					queries++;
				}
				
				else{ 
					//populate ID list
					listPMIDs.put(entry[1], label);
					numIDs++;
					
					if(verbose)System.out.println(entry[1]);
				}
				
				counter++;
			}

			reader.close();

		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 		

		System.out.println("===== > PMIDs on list file: " + listPMIDs.size() + "\n");
		
	}
	
/*	
	public HashMap<String, String> getQueryList() {
		return queryList;
	}

	public void setQueryList(HashMap<String, String> queryList) {
		this.queryList = queryList;
	}

	public HashMap<String, String> getQueryTitle() {
		return queryTitle;
	}

	public void setQueryTitle(HashMap<String, String> queryTitle) {
		this.queryTitle = queryTitle;
	}

	public HashMap<String, String> getKeywordsList() {
		return keywordsList;
	}

	public void setKeywordsList(HashMap<String, String> keywordsList) {
		this.keywordsList = keywordsList;
	}

	public HashMap<String, String> getPMIDs() {
		return PMIDs;
	}

	public void setPMIDs(HashMap<String, String> iDs) {
		PMIDs = iDs;
	}
	
	public HashMap<String, String> getRISIDs() {
		return RISIDs;
	}

	public void setRISIDs(HashMap<String, String> rISIDs) {
		RISIDs = rISIDs;
	}*/
	


}
