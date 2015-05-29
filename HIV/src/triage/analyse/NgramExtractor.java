/*
 * The MIT License (MIT)

Copyright (c) 2014 

Hayda Almeida
Marie-Jean Meurs

Concordia University
Tsang Lab


Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package triage.analyse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import triage.filter.NaiveFilter;
import triage.configure.ConfigConstants;

/**
 * This class extracts and parses n-grams
 * from XML doc instances.
 * 
 * @author Hayda Almeida
 * @since 2014
 * 
 */

public class NgramExtractor extends Extractor{
		
	public NgramExtractor(){
		
		//defining relevant paper text fields
		this.id = "PMID";
		this.openJournal = "Title";
		this.openAbst = "AbstractText";		
		this.openEC = "RegistryNumber";
		this.classTag = "TRIAGE";
		this.openTitle = "ArticleTitle";		
	}	
	
		
	public static void main(String[] args) {
		
		ConfigConstants pathVars = new ConfigConstants();
		boolean verbose = false;
		
		String AnCorpus = pathVars.HOME_DIR + pathVars.CORPUS_DIR + "L" + pathVars.TARGET_LEVEL + "/" + pathVars.TRAINING_FILE;
		NgramExtractor nextrac = new NgramExtractor();
		NaiveFilter featFilter = new NaiveFilter();
		File featureDir = new File(pathVars.HOME_DIR + pathVars.FEATURE_DIR + "/");
		
		featFilter.loadStopWords(pathVars.HOME_DIR + pathVars.STOP_LIST);
		
		//store abstract ngrams and its count
		HashMap<String,Integer> ngram_count = new HashMap<String,Integer>();
		//store abstract ngrams, count and "relevance(TBD)"
		HashMap<Map<String,String>,Integer> ngrams  = new HashMap<Map<String,String>,Integer>();
		//store title ngrams and its count
		HashMap<String,Integer> ngram_title_count = new HashMap<String,Integer>();
		//store title ngrams, count and "relevance(TBD)"
		HashMap<Map<String,String>,Integer> ngram_title = new HashMap<Map<String,String>,Integer>();
		
		//store ID and label of documents
		HashMap<String,String> PMIDs = new HashMap<String,String>();
				
		nextrac.initialize(featureDir, pathVars);		
		
		try 
		{		
			
			//Loading file
			File input = new File(AnCorpus);
			//Jsoup parse
			Document doc = Jsoup.parse(input, "UTF-8");

			Elements corpus = doc.body().getElementsByTag("pubmedarticle");
			
			//Fetching elements			
			for(Element paper : corpus ){

				//Fetching elements
				Elements journalTitle = paper.getElementsByTag(nextrac.getOpenJournal());
				Elements title = paper.getElementsByTag(nextrac.getOpenTitle());
				Elements abstractC = paper.getElementsByTag(nextrac.getopenAbst());
				Elements ECnumber = paper.getElementsByTag(nextrac.getOpenEC());
				Elements classDoc = paper.getElementsByTag(nextrac.getClassTag());		

				String journal = "";
				String docID = "";
				String label = "";
				int jTitle = 0;
				
				//fetching the paper ID - 
				//for all items in a paper, retrieve only PMIDs 
				for(Element e : paper.select(nextrac.getId())){
					//only consider the ID if the parent is medline citation
					if(e.parentNode().nodeName().contains("medline")){						
						docID = e.text();
					}
				}			
				//fetch the doc label as well
				if(classDoc.hasText()){
					label = classDoc.text();									
				}
				
				PMIDs.put(docID, label);

				//Extracting the Journal Title
				if(journalTitle.hasText()){

					jTitle++;				
					journal = journalTitle.toString();
					journal = nextrac.removeSpecialChar(journal);				
					journal = nextrac.removeTags(journal);									
				}

				String tit_content = "";
				//Extracting the Paper Title
				if(title.hasText()){
					tit_content = title.toString();
					tit_content = nextrac.removeSpecialChar(tit_content);
					tit_content = nextrac.removeTags(tit_content);

					ArrayList<String> title_c = nextrac.nGrams(tit_content, featFilter, pathVars);
					nextrac.addNGram(title_c, ngram_title_count, pathVars);		
				}

				String abstrac = "";
				//Extracting the Paper abstract
				if(abstractC.hasText()){
					abstrac = abstractC.toString();
					abstrac = nextrac.removeSpecialChar(abstrac);
					abstrac = nextrac.removeAbstractTags(abstrac);

					ArrayList<String> abstract_c = nextrac.nGrams(abstrac, featFilter, pathVars);
					nextrac.addNGram(abstract_c, ngram_count, pathVars);			
				}		
			}


		}catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
        
		if(verbose){
			//print list of extracted n-grams
			nextrac.displayList(PMIDs);
			System.out.println("\n========ABSTRACT==NGRAMS=============");
			nextrac.displayList(ngram_count);
			nextrac.displayList(ngram_title);
			System.out.println("\n===========TITLE==NGRAMS=============");
			nextrac.displayList(ngram_title_count);
		}
				
		//filter features by occurrence
		featFilter.considerOccurence(ngram_count, pathVars);
		featFilter.considerOccurence(ngram_title_count, pathVars);		
		
		System.out.println("\n===========NGRAMS==EXPORT===============\n");	
		nextrac.exportFile(featureDir + "/" + pathVars.DOC_IDS, PMIDs);
		System.out.println("..."+ PMIDs.size()+" document IDs listed.");
		nextrac.exportFile(featureDir + "/" + pathVars.NGRAM_FEATURES, ngram_count);
		System.out.println("..."+ ngram_count.size()+" unique Abstract ngrams saved.");
		nextrac.exportFile(featureDir + "/" + pathVars.TITLE_NGRAMS, ngram_title_count);
		System.out.println("... "+ ngram_title_count.size() +" unique Title ngrams saved.");		
		System.out.println("\n========================================\n");		
               
	}
	
	
	/**
	 * Inserts ngrams into list of features 
	 * with a mapping for ngram count  
	 * @param str relation of ngrams extracted
	 * @param list_count mapping for ngram counts
	 * @param pathVars 
	 */
	
	private void addNGram(ArrayList<String> str, HashMap<String,Integer> list_count, ConfigConstants pathVars){
		
		//iterating over ngram list
		for(int i = 0; i < str.size(); i++){
			String currentNGram = str.get(i);
			
			//checking existence of current ngram on list mapping
			if(list_count.containsKey(currentNGram)){
				//retrieve the amount of current ngrams on mapping
				int count = list_count.get(currentNGram);
				//insert the updated count of ngrams
				list_count.put(currentNGram, count+1);			
			}
			else {
				//insert ngram on mapping list 
				if(currentNGram.length() >= Integer.parseInt(pathVars.FEATURE_MIN_LENGTH)){
					list_count.put(currentNGram, 1);
				}
			}
		}
	}
	
	/**
	 * Extracts n-grams from a given content field
	 * 
	 * @param str text to extract ngrams
	 * @return list of extracted grams
	 */	
	public ArrayList<String> nGrams(String str, NaiveFilter filter, ConfigConstants pathVar){

		//removing ASCII special characters		
		str = str.replace("/", "");
		str = str.replace("\\", "");		
		str = str.replace(" ", "-");
		str = str.replaceAll("\\s+"," ");
		str = str.replace(" ", "-");
		
		//Tokenizing the sentence
		String[] words = StringUtils.split(str,"-"); 
		ArrayList<String> ngramList = new ArrayList<String>();

		int ngram =Integer.parseInt(pathVar.NGRAM_SIZE);

		//Stop-words removal 
		if(Boolean.valueOf(pathVar.NGRAM_STOP)){
			words = StringUtils.split(filter.removeStopList(words)," ");
		}	
		
		//extracting ngrams according to gram size (1, 2, 3)
		for(int i=0; i < words.length - (ngram - 1); i++){
			switch(pathVar.NGRAM_SIZE){
			case "1":
				ngramList.add(words[i].toLowerCase());
				break;
			case "2":
				ngramList.add(words[i].toLowerCase()+" "+words[i+1].toLowerCase());
				break;
			case "3":
				ngramList.add(words[i].toLowerCase()+" "+words[i+1].toLowerCase()+" "+words[i+2].toLowerCase());
				break;				
			}			
		}
		
		return ngramList;
	}
	
		
	@Override
	public void initialize(File featureDir, ConfigConstants pathVars){		
			try{
				featureDir.mkdir();				
				File ngrams = new File(featureDir + "/" + pathVars.NGRAM_FEATURES);
				ngrams.createNewFile();
				
				File titlengrams = new File(featureDir + "/" + pathVars.TITLE_NGRAMS);
				titlengrams.createNewFile();
				
			}catch(Exception e){
				System.out.println("Error creating features folder.");
				System.exit(0);
			}		
	}
	
		
	/**
	 * Displays the keys and values of the
	 * maps created with n-grams and counts.
	 * @param hash  HashMap containing n-grams
	 */
	@Override
	public void displayList(HashMap hash){
		super.displayList(hash);
			//sum = sum + hash.get(str);		
		System.out.println("\n=======================================\n");
		System.out.println("Number of unique n-grams: "+hash.size());
		System.out.println("\n=======================================\n");
	}
	
		
	
}
