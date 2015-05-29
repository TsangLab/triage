package triage.arffvector;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import triage.configure.ConfigConstants;

/**
 * Uses the features extracted and the 
 * generated corpus to create a feature vector
 * (a matrix representation of the corpus) 
 * 
 * @author Hayda Almeida, Marie-Jean Meurs
 * @since 2014
 * 
 */
public class CreateVector {	
	
	ArrayList<String> keywords = new ArrayList<String>();
	ArrayList<String> shareKeywords = new ArrayList<String>();
	ArrayList<String> journalTitles = new ArrayList<String>();
	ArrayList<String> meshterms = new ArrayList<String>();
	ArrayList<String> titleGrams = new ArrayList<String>();
	ArrayList<String> titleKeywords = new ArrayList<String>();
	ArrayList<String> titleShareKeywords = new ArrayList<String>();
	ArrayList<String> nGrams = new ArrayList<String>();
	ArrayList<String> docID = new ArrayList<String>();
		
	ConfigConstants pathVars = null;
	
	/**
	 * Constructor to load all features extracted
	 * from training files. These features will be 
	 * used to generate the ARFF header and the
	 * ARFF vector lines.
	 * 
	 * @param extVars Variables holding system paths
	 */
	
	public CreateVector(ConfigConstants extVars) {
		
		pathVars = extVars;		
		
		String pathJournalT = pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.JOURNAL_TITLE_FEATURES;
		try{
			String journalT = "";
			
			//receiving journal title
			BufferedReader reader = new BufferedReader(new FileReader(pathJournalT));
			int featcount = 0;
			while (( journalT = reader.readLine()) != null) {
				
				if (Boolean.valueOf(pathVars.USE_JOURNAL_TITLE_FEATURE)){
										
					String[] features = StringUtils.split(journalT,"\n"); 

					for(int i = 0; i < features.length; i++){

						String[] featurename = StringUtils.split(features[i],"\t");
						
						//checking for journal titles duplicates 						
						if(featurename[1] != "" && !(journalTitles.contains(featurename[1]))){
							journalTitles.add(featurename[1]);
						}
					}		
				}
				if ( featcount >= Integer.parseInt(pathVars.NB_PARAMS) && Integer.parseInt(pathVars.NB_PARAMS) != -1 ) { break;}

			}
			reader.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
		
		String pathKeywords = pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.KEYWORD_FEATURES;
		String pathTitlekeywords = pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.KEYWORD_TITLE_FEATURES;
		
		try{
			String keyWord = "";
			String titleKeyword = "";
			
			//receiving abstract keywords
			BufferedReader reader = new BufferedReader(new FileReader(pathKeywords));
			BufferedReader readerT = new BufferedReader(new FileReader(pathTitlekeywords));
			
			int featcount = 0;			
			
			while (( keyWord = reader.readLine()) != null) {				
				
				if (Boolean.valueOf(pathVars.USE_KEYWORD_FEATURE)){
					String[] features = StringUtils.split(keyWord,"\n"); 

					for(int i = 0; i < features.length; i++){

						String[] featurename = StringUtils.split(features[i],"\t");
						
						//checking for duplicate abstract keywords
						if(featurename[0] != "" && !(keywords.contains(featurename[0]))){
							keywords.add(featurename[0]);
						}
					}		
				}				
				if ( featcount >= Integer.parseInt(pathVars.NB_PARAMS) && Integer.parseInt(pathVars.NB_PARAMS) != -1 ) { break;}
			}
			
			
			if(!(Boolean.valueOf(pathVars.USE_KEYWORD_TITLE_FEATURE))){
				while((titleKeyword = readerT.readLine()) != null){
					
					String[] features = StringUtils.split(titleKeyword,"\n");
					
					for(int i = 0; i < features.length; i++){

						String[] featurename = StringUtils.split(features[i],"\t");
						
						//checking for duplicate annotations
						if(featurename[0] != "" && !(keywords.contains(featurename[0]))){
							keywords.add(featurename[0]);
						}
					}	
					
				}
				
			}
			
			reader.close();
			readerT.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
			
		
		try{
			String titleKeyword = "";
			
			//receiving title keywords (features)
			BufferedReader reader = new BufferedReader(new FileReader(pathTitlekeywords));
			// int featcount = 0;
			while (( titleKeyword = reader.readLine()) != null) {

				if(Boolean.valueOf(pathVars.USE_KEYWORD_TITLE_FEATURE)){
					
					//String titAnnot = FeatureExtractor.getTitCount();

					String[] features = StringUtils.split(titleKeyword,"\n");				

					for(int i = 0; i < features.length; i++){
						String[] featurename = StringUtils.split(features[i],"\t");
						
						//checking for duplicate title keywords
						if(!(titleKeywords.contains(featurename[0]))){
							titleKeywords.add(featurename[0]);	
						}
					}								
				}
			}
			reader.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
		
		String pathShareKeywords = pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.SHARE_KEYWORD_FEATURES;
		String pathTitleShareKeywords = pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.SHARE_KEYWORD_TITLE_FEATURES;
		
		try{
			String shareKeyword = "";
			String shareTitleKeyword = "";
			
			//receiving abstract keywords (features)
			BufferedReader reader = new BufferedReader(new FileReader(pathShareKeywords));
			BufferedReader readerT = new BufferedReader(new FileReader(pathTitleShareKeywords));
			
			int featcount = 0;			
			
			while (( shareKeyword = reader.readLine()) != null) {				
				
				if (Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_FEATURE)){
					String[] features = StringUtils.split(shareKeyword,"\t"); 

					for(int i = 0; i < features.length; i++){

						String[] featurename = StringUtils.split(features[i],"\t");
						
						//checking for duplicate abstract keywords
						if(featurename[0] != "" && !(shareKeywords.contains(featurename[0]))){
							shareKeywords.add(featurename[0]);
						}
					}		
				}				
				if ( featcount >= Integer.parseInt(pathVars.NB_PARAMS) && Integer.parseInt(pathVars.NB_PARAMS) != -1 ) { break;}
			}
			
			
			if(!(Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_TITLE_FEATURE))){
				while((shareTitleKeyword = readerT.readLine()) != null){
					
					String[] features = StringUtils.split(shareTitleKeyword,"\n");
					
					for(int i = 0; i < features.length; i++){

						String[] featurename = StringUtils.split(features[i],"\t");
						
						//checking for duplicate keywords
						if(featurename[0] != "" && !(shareKeywords.contains(featurename[0]))){
							shareKeywords.add(featurename[0]);
						}
					}	
					
				}
				
			}
			
			reader.close();
			readerT.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
			
		
		try{
			String shareTitleKeywordOnly = "";
			
			//receiving title share keywords (features)
			BufferedReader reader = new BufferedReader(new FileReader(pathTitleShareKeywords));
			// int featcount = 0;
			while (( shareTitleKeywordOnly = reader.readLine()) != null) {

				if(Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_TITLE_FEATURE)){
					
					//String titAnnot = FeatureExtractor.getTitCount();

					String[] features = StringUtils.split(shareTitleKeywordOnly,"\n");				

					for(int i = 0; i < features.length; i++){
						String[] featurename = StringUtils.split(features[i],"\t");
						
						//checking for duplicate title share keywords
						if(!(titleShareKeywords.contains(featurename[0]))){
							titleShareKeywords.add(featurename[0]);	
						}
					}								
				}
			}
			reader.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
		
		
		String pathMeshFeatures = pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.MESH_FEATURES;

		try{
			String mesh = "";

			//receiving EC numbers (features)
			BufferedReader reader = new BufferedReader(new FileReader(pathMeshFeatures));
			// int featcount = 0;
			while ((mesh = reader.readLine()) != null) {

				if(Boolean.valueOf(pathVars.USE_MESH_FEATURE)){

					//String titAnnot = FeatureExtractor.getTitCount();

					String[] features = StringUtils.split(mesh,"\n");				

					for(int i = 0; i < features.length; i++){
						String[] featurename = StringUtils.split(features[i],"\t");

						//checking for duplicate EC numbers
						if(!(meshterms.contains(featurename[0]))){
							meshterms.add(featurename[0]);	
						}
					}								
				}
			}
			reader.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
		
		
		String pathTitleGrams = pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.TITLE_NGRAMS;
		
		
		try{
			String titCont = "";
			// String grams = "";
			
			//receiving title ngrams
			BufferedReader reader = new BufferedReader(new FileReader(pathTitleGrams));
			
			int featcount = 0;
			while (( titCont = reader.readLine()) != null) {

				if(Boolean.valueOf(pathVars.USE_TITLE_NGRAMS)){
					
					String[] content = StringUtils.split(titCont,"\n");				

					for(int i = 0; i < content.length; i++){				
						String[] featurename = StringUtils.split(content[i],"\t");			
						
						//check for duplicate title ngrams
						if(!(titleGrams.contains(featurename[0]))){
							titleGrams.add(featurename[0]);
						}
					}			
				}
			}
						
			reader.close();

		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
				
		String pathNgrams = pathVars.HOME_DIR + pathVars.FEATURE_DIR + pathVars.NGRAM_FEATURES;
		try{
			String grams = "";
			String tgrams = "";
			
			//receiving ngrams
			BufferedReader reader = new BufferedReader(new FileReader(pathNgrams));
			BufferedReader readerT = new BufferedReader(new FileReader(pathTitleGrams));
			
			// int featcount = 0;
			while (( grams = reader.readLine()) != null) {

				if(Boolean.valueOf(pathVars.USE_NGRAM_FEATURE)){

					String[] features = StringUtils.split(grams,"\n");

					for(int i = 0; i < features.length; i++){
						String[] featurename = StringUtils.split(features[i],"\t");

						//check for duplicate abstract ngrams
						if(!(nGrams.contains(featurename[0]))){
							nGrams.add(featurename[0]);
						}
					}
				}

			}
			
			//if not using title grams separately, 
			// then insert title grams with abstract grams.  
			if (!(Boolean.valueOf(pathVars.USE_TITLE_NGRAMS))){
				while (( tgrams = readerT.readLine()) != null) {
					
					String[] features = StringUtils.split(tgrams,"\n");
					
					for(int i = 0; i < features.length; i++){
						String[] featurename = StringUtils.split(features[i],"\t");
						
						//check for duplicate ngrams
						if(!(nGrams.contains(featurename[0]))){
							nGrams.add(featurename[0]);
						}
					}					
				}				
			}
			
			reader.close();
			readerT.close();
			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	/**
	 * Gathers the list of features, according to 
	 * experimental configurations. The list of 
	 * features will be written on the ARFF header.
	 * 
	 * @param pathVars Variables holding system paths
	 * @param exp experiment type: train or test
	 * @return a String containing the ARFF header
	 */
	
	public String genArffHeader(ConfigConstants pathVars, int exp){
		
		StringBuilder headerArff = new StringBuilder();
		
		switch(exp){
			case 0: 
				headerArff.append("% Weka training file - HIV triage - 2015\n\n");
			break;			
			case 1: 
				headerArff.append("% Weka test file - HIV triage - 2015\n\n");
			break;
		}		
		
		headerArff.append("@RELATION triage\n");
		
		if(Boolean.valueOf(pathVars.USE_TEXT_SIZE)){
			// writing the list of text sizes
			headerArff.append("@ATTRIBUTE sizeoftitle \tREAL \t\t%size of title\n");
			headerArff.append("@ATTRIBUTE sizeoftext \tREAL \t\t%size of text\n");			
		}
		
		if(Boolean.valueOf(pathVars.USE_DOC_ID)){
			//writing the docIDs
			headerArff.append("@ATTRIBUTE docID \tREAL \t\t%PMID of paper\n");
						
		}
		
		if(Boolean.valueOf(pathVars.USE_JOURNAL_TITLE_FEATURE)){
			for(int i = 0; i < journalTitles.size(); i++){
			// writing list of journal titles
				String feature = journalTitles.get(i);
				String namefeature = feature.replaceAll("\\s", "-");
				namefeature = namefeature.replaceAll("[,:=+']", "-");
				namefeature = namefeature.replaceAll("<|>", "");
				String ref = "journalTitle" + String.valueOf(i) + namefeature; 
				
				headerArff.append("@ATTRIBUTE " + ref + "\tREAL \t\t%" + feature + "\n");
							
			}
		}
		
		if (Boolean.valueOf(pathVars.USE_KEYWORD_FEATURE)){
			// writing list of annotation features
			for(int i = 0; i < keywords.size(); i++){

				String feature = keywords.get(i);
				String namefeature = feature.replaceAll("\\s", "-");
				namefeature = namefeature.replaceAll("[,:=+']", "-");
				namefeature = namefeature.replaceAll("<|>", "");
				String ref = "keyw" + String.valueOf(i) + namefeature; 
				
				headerArff.append("@ATTRIBUTE " + ref + "\tREAL \t\t%" + feature + "\n");

			}
		}
				
		if(Boolean.valueOf(pathVars.USE_KEYWORD_TITLE_FEATURE)){			
			// write list of title features
			for( int i = 0; i < titleKeywords.size(); i++){

				String feature = titleKeywords.get(i);
				String namefeature = feature.replaceAll("\\s", "-");
				namefeature = namefeature.replaceAll("[,:=+']", "-");
				namefeature = namefeature.replaceAll("<|>", "");
				String ref = "titleKeyw" + String.valueOf(i) + namefeature; 
				
				headerArff.append("@ATTRIBUTE " + ref + "\tREAL \t\t%" + feature + "\n");
				
			}			
			
		}
		
		if(Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_FEATURE)){			
			// write list of title features
			for( int i = 0; i < shareKeywords.size(); i++){

				String feature = shareKeywords.get(i);
				String namefeature = feature.replaceAll("\\s", "-");
				namefeature = namefeature.replaceAll("[,:=+']", "-");
				namefeature = namefeature.replaceAll("<|>", "");
				String ref = "shareKeyw" + String.valueOf(i) + namefeature; 
				
				headerArff.append("@ATTRIBUTE " + ref + "\tREAL \t\t%" + feature + "\n");
				
			}			
			
		}
		
		if(Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_TITLE_FEATURE)){			
			// write list of title features
			for( int i = 0; i < titleShareKeywords.size(); i++){

				String feature = titleShareKeywords.get(i);
				String namefeature = feature.replaceAll("\\s", "-");
				namefeature = namefeature.replaceAll("[,:=+']", "-");
				namefeature = namefeature.replaceAll("<|>", "");
				String ref = "titleShareKeyw" + String.valueOf(i) + namefeature; 
				
				headerArff.append("@ATTRIBUTE " + ref + "\tREAL \t\t%" + feature + "\n");
				
			}			
			
		}
		
		if(Boolean.valueOf(pathVars.USE_MESH_FEATURE)){
			// writing list of EC numbers
			for(int i = 0; i < meshterms.size(); i++){
				String feature = meshterms.get(i);
				String namefeature = feature.replaceAll("\\s", "-");
				namefeature = namefeature.replaceAll("[,:=+']", "-");
				namefeature = namefeature.replaceAll("<|>", "");
				String ref = "MeshTerm" + String.valueOf(i) + namefeature;
				
				headerArff.append("@ATTRIBUTE " + ref + "\tREAL \t\t%" + feature + "\n");				
			}
		}
		
		if (Boolean.valueOf(pathVars.USE_TITLE_NGRAMS)){
			// writing list of ngrams on titles			
			for( int i = 0; i < titleGrams.size(); i++){

				String feature = titleGrams.get(i);
				String namefeature = feature.replaceAll("\\s", "-");
				namefeature = namefeature.replaceAll("[,:=+']", "-");
				namefeature = namefeature.replaceAll("<|>", "");
				String ref = "titleNgram" + String.valueOf(i) + namefeature; 
				
				headerArff.append("@ATTRIBUTE " + ref + "\tREAL \t\t%" + feature + "\n");
				
			}
		}		
		
		if (Boolean.valueOf(pathVars.USE_NGRAM_FEATURE)){
			// write list of ngrams
			for(int i = 0; i < nGrams.size(); i++){

				String feature = nGrams.get(i);
				String namefeature = feature.replaceAll("\\s", "-");
				namefeature = namefeature.replaceAll("[,:=+']", "-");
				String ref = "Ngram" + String.valueOf(i) + namefeature; 
				
				headerArff.append("@ATTRIBUTE " + ref + "\tREAL \t\t%" + feature + "\n");
				
			}
		}
		
		// writing the dataset classes		
		headerArff.append("@ATTRIBUTE class 	{positive, negative}\n");
		headerArff.append("@DATA\n");
		
		return headerArff.toString();
	}	

	/**
	 * Iterates over the list of features and 
	 * counts number of features containing 
	 * on a given document.    
	 * 
	 * @param jTitle title of journal 
	 * @param title  title of paper
	 * @param text  abstract content
	 * @param meshterm  paper EC numbers 
	 * @param classTriage  triage classification: positive or negative
	 * @param exp experiment type: train or test
	 * @return String holding counts for all features found in a document
	 */
	
	public String getArffLine(String paperID, String jTitle, String title, String text, String meshterm, String classTriage, int exp){
		//String vectorArff = "";
		StringBuilder vectorArff = new StringBuilder();
				
		paperID = removeSpecialChar(paperID.toLowerCase());
		text = removeSpecialChar(text.toLowerCase());
		title = removeSpecialChar(title.toLowerCase());
		jTitle = removeSpecialChar(jTitle.toLowerCase());
		meshterm = removeSpecialChar(meshterm);			
		
		int emptyabs = 0;

		// fill title and text sizes (number of words)
		// annotation markups do not matter because
		// they do not introduce blank spaces hence 
		// they do not modify the number of words found	
		if (Boolean.valueOf(pathVars.USE_TEXT_SIZE)){

			String[] titleGrams = StringUtils.split(title," ");
			int titlesize = titleGrams.length;

			String[] abstractcontent = StringUtils.split(text," ");
			int abstractsize = abstractcontent.length;
			
			if(abstractsize == 1){
				emptyabs++;
			}
			
			vectorArff.append(titlesize).append(",").append(abstractsize).append(",");			
		}
		
		//fill ID of documents
		if(Boolean.valueOf(pathVars.USE_DOC_ID)){

				if(paperID.length()>0){					
					vectorArff.append(paperID).append(",");
				}
				else{
					vectorArff.append("0,");
				}			
		}
		
		//fill values of journal titles
		if(Boolean.valueOf(pathVars.USE_JOURNAL_TITLE_FEATURE)){
			
			for(int i = 0; i < journalTitles.size(); i++){
				String jfeat = "";
				int jfeatcount = 0;
				jfeat = journalTitles.get(i).replaceFirst(" ", "");
				
				if(jTitle.contains(jfeat)){
					jfeatcount = StringUtils.countMatches(jTitle, jfeat);
					vectorArff.append(jfeatcount).append(",");
				}
				else{
					vectorArff.append("0,");
				}
			}
		}
		
		// fill values of keywords taken into account 
		// either only the abstract or abstract and title
		// adds on vector the count of occurrences		
		if (Boolean.valueOf(pathVars.USE_KEYWORD_FEATURE)){

			for(int i = 0; i < keywords.size(); i++){		
				String anfeat = "";
				int anfeatcount = 0;
				anfeat = keywords.get(i).replaceFirst(" ", "").toLowerCase();
				
				//in case the text has current annotation
				if (text.contains(anfeat)){
					//check the count of the annotation
					if((Boolean.valueOf(pathVars.USE_KEYWORD_TITLE_FEATURE))){
						anfeatcount = StringUtils.countMatches(text, anfeat);						
					}
					//adding title annot count to annotations
					else if (!(Boolean.valueOf(pathVars.USE_KEYWORD_TITLE_FEATURE))){						
						anfeatcount = StringUtils.countMatches(text, anfeat);
						//in case title has annotation, add to count
						if(title.contains(anfeat)){
							anfeatcount = anfeatcount + StringUtils.countMatches(title, anfeat);
						}
					}					
					vectorArff.append(anfeatcount).append(",");
				}
				//handles the case that only the title (but not abstract) has current annotation
				else if((!(Boolean.valueOf(pathVars.USE_KEYWORD_TITLE_FEATURE)))){
					if(title.contains(anfeat)){
						anfeatcount = StringUtils.countMatches(title, anfeat);
					}
					vectorArff.append(anfeatcount).append(",");
				}
				else{
					vectorArff.append("0,");					
				}
			}			
		}
		
				
		//fill values of title keywords
		if (Boolean.valueOf(pathVars.USE_KEYWORD_TITLE_FEATURE)){
			
			for( int i =0; i < titleKeywords.size(); i++){				
				String titfeat = "";
				int titfeatcount = 0;
				titfeat = titleKeywords.get(i).replaceFirst(" ", "").toLowerCase();
				
				if (title.contains(titfeat)){
					titfeatcount = StringUtils.countMatches(title, titfeat);
					vectorArff.append(titfeatcount).append(",");					
				}
				else{
					vectorArff.append("0,");				
				}				
			}
		}
		
		// fill values of keywords taken into account 
		// either only the abstract or abstract and title
		// adds on vector the count of occurrences		
		if (Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_FEATURE)){

			for(int i = 0; i < shareKeywords.size(); i++){		
				String anfeat = "";
				int anfeatcount = 0;
				anfeat = shareKeywords.get(i).replaceFirst(" ", "").toLowerCase();

				//in case the text has current annotation
				if (text.contains(anfeat)){
					//check the count of the annotation
					if((Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_FEATURE))){
						anfeatcount = StringUtils.countMatches(text, anfeat);						
					}
					//adding title annot count to annotations
					else if (!(Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_FEATURE))){						
						anfeatcount = StringUtils.countMatches(text, anfeat);
						//in case title has annotation, add to count
						if(title.contains(anfeat)){
							anfeatcount = anfeatcount + StringUtils.countMatches(title, anfeat);
						}
					}					
					vectorArff.append(anfeatcount).append(",");
				}
				//handles the case that only the title (but not abstract) has current annotation
				else if((!(Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_TITLE_FEATURE)))){
					if(title.contains(anfeat)){
						anfeatcount = StringUtils.countMatches(title, anfeat);
					}
					vectorArff.append(anfeatcount).append(",");
				}
				else{
					vectorArff.append("0,");					
				}
			}			
		}


		//fill values of title keywords
		if (Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_TITLE_FEATURE)){

			for( int i =0; i < titleShareKeywords.size(); i++){				
				String titfeat = "";
				int titfeatcount = 0;
				titfeat = titleShareKeywords.get(i).replaceFirst(" ", "").toLowerCase();

				if (title.contains(titfeat)){
					titfeatcount = StringUtils.countMatches(title, titfeat);
					vectorArff.append(titfeatcount).append(",");					
				}
				else{
					vectorArff.append("0,");				
				}				
			}
		}
		
		if(Boolean.valueOf(pathVars.USE_MESH_FEATURE)){
			
			for(int i = 0; i < meshterms.size(); i++){
				String meshfeat = "";
				int meshnumcount  = 0;
				meshfeat = meshterms.get(i);
				
				if(meshterm.contains(meshfeat)){
					meshnumcount = StringUtils.countMatches(meshterm, meshfeat);
					vectorArff.append(meshnumcount).append(",");
				}
				else{
					vectorArff.append("0,");
				}
			}
		}
		
		// fill only values of title ngrams
		if(Boolean.valueOf(pathVars.USE_TITLE_NGRAMS)){

			String cleanTitle = removeTags(title.toLowerCase());
					
			for( int i =0; i < titleGrams.size(); i++){
				String titgram = "";
				int titgramcount = 0;
				titgram = titleGrams.get(i).toLowerCase();
				
				//in case the title has current ngram
				if (cleanTitle.contains(titgram)){
					//check the count of the ngram
					titgramcount = StringUtils.countMatches(cleanTitle, titgram);

					//adding weight to current ngram count
					if(Boolean.valueOf(pathVars.USE_WEIGHTED_NGRAM)){
						titgramcount = applyWeight(titgramcount, Integer.parseInt(pathVars.WEIGHT));						
					}
					vectorArff.append(titgramcount).append(",");					
				}
				else{
					vectorArff.append("0,");				
				}
			}					
		}
		
		// fill values of ngrams
		if (Boolean.valueOf(pathVars.USE_NGRAM_FEATURE)){
			String cleanText = removeTags(text.toLowerCase());
			String cleanTitle = removeTags(title.toLowerCase());
						
			for( int i = 0; i < nGrams.size(); i++){
				String ngramfeat = "";
				int ngramcount = 0;
				ngramfeat = nGrams.get(i).toLowerCase();

				//in case the text has current ngram
				if (cleanText.contains(ngramfeat)){
					//check the count of the ngram
					if(Boolean.valueOf(pathVars.USE_TITLE_NGRAMS)){						
						ngramcount = StringUtils.countMatches(cleanText, ngramfeat);
						
						//adding weight to current ngram count 
						if(Boolean.valueOf(pathVars.USE_WEIGHTED_NGRAM)){
							ngramcount = applyWeight(ngramcount, Integer.parseInt(pathVars.WEIGHT));							
						}
					}
					//checking if title ngrams should be added to the count
					else if(!(Boolean.valueOf(pathVars.USE_TITLE_NGRAMS))){
						ngramcount = StringUtils.countMatches(cleanText, ngramfeat);						
						
						//in case title has ngram, add to count
						if(cleanTitle.contains(ngramfeat)){							
							ngramcount += StringUtils.countMatches(cleanTitle, ngramfeat);							
						}
						
						//adding weight to current ngram count 
						if(Boolean.valueOf(pathVars.USE_WEIGHTED_NGRAM)){							
							ngramcount = applyWeight(ngramcount, Integer.parseInt(pathVars.WEIGHT));							
						}
					}				

					vectorArff.append(ngramcount).append(",");					
				}
				////handles the case that only the title (but not abstract) has current ngram
				else if (!(cleanText.contains(ngramfeat))){
					//in case only the title has the ngram, add to count
					if(cleanTitle.contains(ngramfeat)){
						ngramcount = StringUtils.countMatches(cleanTitle, ngramfeat);
						
						//adding weight to ngram count
						if(Boolean.valueOf(pathVars.USE_WEIGHTED_NGRAM)){
							ngramcount = applyWeight(ngramcount, Integer.parseInt(pathVars.WEIGHT));							
						}
					}
					vectorArff.append(ngramcount).append(",");
				}
				else{
					vectorArff.append("0,");					
				}
			}	
		}
		
		
		//if(exp == 0){
			if (classTriage.contains("positive")){ 
				vectorArff.append("positive");
				//vectorArff.append("?");	
			}
			else {
				vectorArff.append("negative");
				//vectorArff.append("?");
			}
		//}

		/*else if (exp == 1){
			vectorArff.append("?");				
		}	*/	
		
	return vectorArff.toString();
	}
	
	/**
	 * Cleans a given String from special characters
	 *  
	 * @param str String to be cleaned 
	 * @return String without special characters
	 */
	
	public String removeSpecialChar(String str){
		str = str.replace("}", "");
		str = str.replace("{", "");
		str = str.replace("]", "");
		str = str.replace("[", "");
		str = str.replace("#", "");
		str = str.replace("*", "");
		str = str.replace("&gt", "");
		str = str.replace("&quot", "");
		str = str.replace("&apos", "");
		str = str.replace("&amp", "");
		str = str.replace("%", "");
		str = str.replace("/", "");
		str = str.replace("\\", "");		
		str = str.replace("&", "");
		str = str.replace("=", "");
		str = str.replace("?", "");
		str = str.replace(",", "");
		str = str.replace(":", "");
		str = str.replace(";", "");
		str = str.replace(".", "");
		str = str.replace(")", "");
		str = str.replace("(", "");		
		str = str.replace("\t\t", "\t");
		str = str.replace("-", "");
		str = str.replace("  ", "");
		
		return str;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public String removeTags(String str){
		String[] remove = StringUtils.split(str,"");
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < remove.length; i++){
			
			if(remove[i].equalsIgnoreCase("<")){
				do{
					i++;
				}
				while(!(remove[i].equalsIgnoreCase(">")));
			}
			else sb.append(remove[i]);
		}
				
		return sb.toString();	
	}
	
	public int applyWeight(int count, int weight){
		
		if(weight > 0){
			count = count * weight;
		}
		return count;
	}
	
	
	public String informFeatures(ConfigConstants pathVars){
		String value = "";
		if(Boolean.valueOf(pathVars.USE_KEYWORD_FEATURE))
			value = value + "_keywords";
		if(Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_FEATURE))
			value = value + "_sharekeywords";
		if(Boolean.valueOf(pathVars.USE_JOURNAL_TITLE_FEATURE))
			value = value + "_journal";
		if(Boolean.valueOf(pathVars.USE_KEYWORD_TITLE_FEATURE) 
				|| Boolean.valueOf(pathVars.USE_SHARE_KEYWORD_TITLE_FEATURE) 
				|| Boolean.valueOf(pathVars.USE_TITLE_NGRAMS))
			value = value + "_title";
		if(Boolean.valueOf(pathVars.USE_MESH_FEATURE))
			value = value + "_mesh";
		if(Boolean.valueOf(pathVars.USE_NGRAM_FEATURE))
			value = value + "_ngrams_size"+ pathVars.NGRAM_SIZE;
		if(Boolean.valueOf(pathVars.USE_NGRAM_FEATURE) && Boolean.valueOf(pathVars.NGRAM_STOP))
			value = value + "_stopwords";
		if(Boolean.valueOf(pathVars.USE_WEIGHTED_NGRAM))
			value = value + "_weight"+ pathVars.WEIGHT;
		
		return value;
	}

	
}
