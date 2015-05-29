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

import triage.configure.ConfigConstants;
import triage.filter.NaiveFilter;


/**
 * This class extracts and parses domain 
 * annotation features from XML doc instances.
 *   
 * @author Hayda Almeida
 * @since 2014
 */

public class AnnotationExtractor extends Extractor{	
	
	public AnnotationExtractor(){				

		this.id = "PMID";
		this.openAbst = "AbstractText";
		this.abstractLabel = "AbstractText ";		
		this.classTag = "TRIAGE";
		this.openJournal = "Title";
		this.openTitle = "ArticleTitle";				
	}
		
	
	public static void main(String[] args) {
		
		ConfigConstants pathVars = new ConfigConstants();
		
		String AnCorpus = pathVars.HOME_DIR + pathVars.CORPUS_DIR + "L" + pathVars.TARGET_LEVEL + "/" + pathVars.TRAINING_FILE;
		AnnotationExtractor fextrac = new AnnotationExtractor();	
		NaiveFilter featFilter = new NaiveFilter();
		File featureDir = new File(pathVars.HOME_DIR + pathVars.FEATURE_DIR);
		
		fextrac.initialize(featureDir, pathVars);		
				
		//store abstract features and count
		HashMap<Map<String,String>,Integer> abstract_count = new HashMap<Map<String,String>,Integer>();		
		//store MeSH terms
		HashMap<Map<String,String>,Integer> mesh_count = new HashMap<Map<String,String>,Integer>();		
		//store title features and count
		HashMap<Map<String,String>, Integer> title_count = new HashMap<Map<String,String>, Integer>();		
		//store PMID - label
		HashMap<String,String> PMIDs = new HashMap<String,String>();				
		//store journal titles - label
		HashMap<String,String> journal_title = new HashMap<String,String>();
				
		int jTitle = 0;
				
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
				Elements journalTitle = paper.getElementsByTag(fextrac.getOpenJournal());
				Elements title = paper.getElementsByTag(fextrac.getOpenTitle());
				Elements abstractC = paper.getElementsByTag(fextrac.getopenAbst());			
				Elements classDoc = paper.getElementsByTag(fextrac.getClassTag());
				Elements meshTerms = paper.getElementsByTag("MeshHeadingList");

				String journal = "";
				String docID = "";
				String label = "";
				ArrayList<String> tempList = new ArrayList<String>();
				StringBuffer sb = new StringBuffer();

				//fetching the paper ID - 
				//for all items in a paper, retrieve only PMIDs 
				for(Element e : paper.select(fextrac.getId())){
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

				if(journalTitle.hasText()){

					jTitle++;				
					journal = journalTitle.toString();
					journal = fextrac.removeSpecialChar(journal);				
					journal = fextrac.removeTags(journal);
					
					journal_title.put(journal, label);
				}

				String title_annotation = "";
				if(title.hasText()){
					title_annotation = title.toString();
					title_annotation = fextrac.removeSpecialChar(title_annotation);

					//tempList.addAll(fextrac.annotations(title_annotation, title_count, featFilter, pathVars));
							
				}

				String abstrac = "";
				if(abstractC.hasText()){
					abstrac = abstractC.toString();
					abstrac = fextrac.removeSpecialChar(abstrac);
					abstrac = fextrac.removeAbstractTags(abstrac);

					//tempList.addAll(fextrac.annotations(abstrac, abstract_count, featFilter, pathVars));				
				}			

				String meshT = "";
				if(meshTerms.hasText()){
					meshT = meshTerms.toString();
					//meshT = fextrac.removeSpecialChar(meshT);

					tempList.addAll(fextrac.meshAnnotations(meshT, mesh_count, pathVars));
				}

				String triage = "";
				if(classDoc.hasText()){
					triage = classDoc.toString();
					triage = fextrac.removeSpecialChar(triage);
					triage = fextrac.removeTags(triage);

				//	fextrac.addClass(triage, abstract_type);							
				//	fextrac.addClass(triage, title_type);					
				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//Use for sample output
		//System.out.println("\n===========TITLE==ANNOTATIONS=============");
		//fextrac.displayList(title_count);		
		//fextrac.displayList(title_type);
		//fextrac.displayList(title_content);
		//System.out.println("\n========ABSTRACT==ANNOTATIONS=============");
		//fextrac.displayList(abstract_count);		
		//fextrac.displayList(abstract_type);		
		
		//Before exporting, take into account the 
		//Occurrence of all extracted features 
		featFilter.considerAnnotationOccurence(abstract_count, pathVars);
		featFilter.considerAnnotationOccurence(mesh_count, pathVars);
		featFilter.considerAnnotationOccurence(title_count, pathVars);
					
		
		System.out.println("\n===========FEATURE==EXPORT===============\n");
		fextrac.exportFile(featureDir + "/" + pathVars.DOC_IDS, PMIDs);
		System.out.println("..."+ PMIDs.size()+" document IDs listed.");
		fextrac.exportFile(featureDir + "/" + pathVars.MESH_FEATURES, mesh_count);
		System.out.println("..."+ mesh_count.size()+" unique MeSH terms saved.");		
		fextrac.exportFile(featureDir + "/" + pathVars.JOURNAL_TITLE_FEATURES, journal_title);
		System.out.println("..."+jTitle+" Journal titles saved.");
//		fextrac.exportFile(featureDir + "/" + pathVars.KEYWORD_FEATURES, abstract_count);
//		System.out.println("..."+ abstract_count.size()+" unique Abstract annotations saved.");
//		fextrac.exportFile(featureDir + "/" + pathVars.KEYWORD_TITLE_FEATURES, title_count);
//		System.out.println("..."+ title_count.size() +" unique Title annotations saved.");
		System.out.println("\n=========================================\n");
		
	}		

	/**
	 * Identifies the classification on doc
	 * 
	 * @param clas text containing classification (after char removal)
	 * @return classification of doc
	 * @deprecated
	 */	
	private String getClassif(String clas) {
		
		//parsing the not edited text into HTML using Jsoup
		Document doc = Jsoup.parseBodyFragment(clas);
		//saving the text as an Jsoup element, with a main tag (the HTML body), 
		//attributes and child nodes (TRIAGE tags)
		Element text = doc.body();
		
		Elements classification = text.getElementsByTag("TRIAGE");
				
		return classification.text();		
	}
	
	/**
	 * Inserts the classification 
	 * on the list of features
	 * 
	 * @param class information to insert on list
	 * @param list list of features used
	 */	
	private void addClass(String element, HashMap<Map<String,String>, String> list){
		//going over list to insert
		//classif on document instances		
		Iterator<Map<String, String>>it = list.keySet().iterator();
		
		while(it.hasNext()){		
			Map<String,String> str = it.next();
								
			if(list.get(str).contains("positive") || list.get(str).contains("negative")){
					
			}
			else list.put(str, element);
		}
	}
		
	
	/**
	 * Extract the MeSH terms annotations and
	 * inserts it in a list with counts and types
	 * @param annot text field containing mesh terms
	 * @param count list that holds mesh term, its type and its count
	 * @param type list that holds mesh term, its type and its classification
	 */	
	private ArrayList<String> meshAnnotations(String annot, HashMap<Map<String, String>, Integer> count, ConfigConstants pathVars){ 
		//NgramExtractor nextrac = new NgramExtractor();
		ArrayList<String> meshContent = new ArrayList<String>();
		
		//parsing the not edited text into HTML using Jsoup
		Document doc = Jsoup.parseBodyFragment(annot);
		//saving the text as an Jsoup element, with a main tag (the HTML body), 
		//attributes and child nodes (annotation tags)
		Element annotations = doc.body();
		
		Elements annots = annotations.getElementsByTag("MeshHeading");
		
		//iterating over meshHeadings in the list
		for(Element an : annots){			
			//iterating over each line in the meshHeading
			for(Element child : an.children()){	
				meshContent.add(child.text());
				ArrayList<String> content = (ArrayList<String>) meshContent.clone();
				insertAnnotation(content, child.nodeName(), count, pathVars);				
			}			
		}
		return meshContent;
	}
	
	
	/**
	 * Extract a generic list of annotations in a determined 
	 * text field in the document and list them.
	 * 
	 * @param annotat text field containing annotations
	 * @param count list that holds annotation, its type and its count
	 * @param filter naive filter to perform normalizations
	 * @param paathVars configure vars instance
	 */	
	private ArrayList<String> annotations(String annot, HashMap<Map<String, String>, Integer> count, NaiveFilter filter, ConfigConstants pathVars) {		
		HashMap<String,String> features = loadAnnotationEntities();
		
		ConfigConstants pathVar = new ConfigConstants(); 
		NgramExtractor nextrac = new NgramExtractor();
		ArrayList<String> content = new ArrayList<String>();

		//parsing the not edited text into HTML using Jsoup
		Document doc = Jsoup.parseBodyFragment(annot);
		//saving the text as an Jsoup element, with a main tag (the HTML body), 
		//attributes and child nodes (annotation tags)
		Element annotations = doc.body();

		//iterating over list of entities
		for(Map.Entry<String,String> value : features.entrySet()){

			String an_type = value.getKey();
			String an_level = value.getValue();

			//for each entity, find the annotations on abstract
			Elements annots = annotations.getElementsByTag(an_type);			

			//for each annotation found, 
			for(Element an : annots){

				//grabbing annotation content:
				//if the annotation is made on the sentence level:
				if(an_level.contains("sentence")){

					//checking if sentence contains inner annotations
					if(an.childNodeSize() != 0){

						//going over list of inner annotations
						for(Element child : an.children()){

							//if child is sentence (sentence inside of sentence),  
							//then add annotations as ngrams on this
							if(features.get(child.nodeName()).contains("sentence")) {
								content.addAll(nextrac.nGrams(child.text(), filter,  pathVar));
								insertAnnotation(content, an.nodeName(), count, pathVars);
							}
							//adding annotations on sentence as they are - no ngrams on this
							else {
								content.add(child.text());	
								insertAnnotation(content, an.nodeName(), count, pathVars);
							}
						}
						
						//removing inner annotations from sentence, they are already added
						Element tempAnnot = an.clone();
						tempAnnot.children().remove();

						//splitting content in ngrams to whats left on the sentence
						content.addAll(nextrac.nGrams(tempAnnot.text(), filter, pathVar));
						insertAnnotation(content, an.nodeName(), count, pathVars);
					}			

				}
				else {
					//keeping original annotation content for other cases					
					content.add(an.text()); 
					insertAnnotation(content, an.nodeName(), count, pathVars);
				}
			}

		}
		
		return content;

	}	
	
	
	/**
	 * Insert annotation (or ngram list of annotation) 
	 * on lists, used on @annotations method 
	 * @param content content of annotation
	 * @param an_type type extracted from text (entity)
	 * @param count list of annotations and their count 
	 */	
	private void insertAnnotation(ArrayList<String> content, String an_type, HashMap<Map<String, String>, Integer> count, ConfigConstants pathVars){
		
		//iterating over list of annotations
		for(int i = 0; i < content.size(); i++){

			if(content.get(i).length() >= Integer.parseInt(pathVars.FEATURE_MIN_LENGTH)){

				//creating the list key as: content - type mapping
				Map<String, String> an_content = new HashMap<String, String>();				
				an_content.put(content.get(i), an_type);

				//for each annotation (or ngram on annotation)
				//insert content and related type
				if(count.containsKey(an_content)){						
					try{
						int cnt = count.get(an_content);								
						count.put(an_content, cnt+1);

					}catch(Exception e){
						count.put(an_content, 1);															
					}
				}					
				else{					
					count.put(an_content, 1);					
				}				
			}
		}
		
		content.clear();		
	}

	
	/**
	 * Inserts the text (e.g.title) content into   
	 * a list of features (e.g.title features)
	 *  
	 * @param annot text with the annotations to be handled
	 * @param wContent whole field to be added on the list of features
	 * @param list features used
	 * 
	 */	
	private void addContent(String annot, String wContent, HashMap<Map<String,String>,String> list, NaiveFilter filter) {

		HashMap<String,String> features = loadAnnotationEntities();
		ArrayList<String> content = new ArrayList<String>();
		NgramExtractor nextrac = new NgramExtractor();
		ConfigConstants pathVar = new ConfigConstants();

		//parsing not edited text into HTML using Jsoup
		Document doc = Jsoup.parseBodyFragment(annot);
		//saving the text as an Jsoup element, with a main tag (the HTML body), 
		//attributes and child nodes (annotation tags)
		Element annotations = doc.body();

		//iterating over annotation types
		for(Map.Entry<String,String> value : features.entrySet()){

			String an_type = value.getKey();
			String an_level = value.getValue();

			//for each annotation type, find all related annotations on the abstract
			Elements annots = annotations.getElementsByTag(an_type);			

			//for each annotation type, 
			for(Element an : annots){

				//grab annotation content								
				if(an_level.contains("sentence"))
					//splitting in ngrams for sentence level annotations
					content = nextrac.nGrams(an.text(), filter, pathVar);
				else 
					//keeping original annotation for other cases
					content.add(an.text());

				//iterating over list of annotations
				for(int i = 0; i < content.size(); i++){
					
					Map<String,String> an_content = new HashMap<String,String>();
					an_content.put(content.get(i), wContent);
					
					//populating list of feature_an_types, with:
					//feature--an_type--class
					list.put(an_content, "");									
				}
				content.clear();
			}
		}
	}

	
	/**
	 * Loads list of entities from external file
	 * 
	 * @param str list of entities
	 * @param pathVar constants from 
	 * @return
	 */	
	public HashMap<String,String> loadAnnotationEntities(){
		
		String pathEntities = "entities.txt";		
		HashMap<String,String> values = new HashMap<String,String>();
						
		try{			
			BufferedReader reader = new BufferedReader(new FileReader(pathEntities));
			
			String line = null;	
			
			while((line = reader.readLine()) != null){				
                
				String[] value = StringUtils.split(line, " ");
				values.put(value[0].toLowerCase(), value[1].toLowerCase());				
			}
			
			reader.close();
			
		}catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }		
		//String[] entities = values.toArray(new String[values.size()]);
		
		return values;
	}

	
	/**
	 * Handles the content of annotations; when
	 * there is multiple elements, they are 
	 * concatenated after extracted 
	 * 
	 * @param str list of annotation elements
	 * @return single string with all elements
	 */	
	public String contentToString(String[] str){
		String cont = "";
		
		for(int i = 0; i < str.length; i++){
				if(cont.contentEquals("")){
					cont = cont + str[i];	
				}
				else cont = cont+" "+ str[i];
				
			}		
		
		return cont;
	}
	
	@Override
	public void initialize(File featureDir, ConfigConstants pathVars){
				
			try{
				featureDir.mkdir();				
				File meshterms = new File(featureDir + "/" + pathVars.MESH_FEATURES);
				meshterms.createNewFile();
								
				File journaltitles = new File(featureDir + "/" + pathVars.JOURNAL_TITLE_FEATURES);
				journaltitles.createNewFile();
				
			}catch(Exception e){
				System.out.println("Error creating features folder.");
				System.exit(0);
			}		
	}
	
	

}
