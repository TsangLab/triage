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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
/**
 * Implements common tools to feature extractors
 * classes used on XML doc instances 
 * 
 * @author Hayda Almeida
 * @since 2014
 *
 */
public class Extractor {
	
	//relevant paper text fields 
	String openAbst;
	String abstractLabel;
	String openEC;
	String classTag;
	String openTitle;
	String openJournal;
	String copyR;
	String closeCopyR;
	String id;
	String endId;
	String openFile;
	String endFile;
	
	//String closeAbst;
	//String closeJournal;
	//String closeTitle;
	//String closeEC;
	//String pathFile;
	
	
	/**
	 * Removes special characters for 
	 * tokenization process
	 * 
	 * @param str text to be cleaned
	 * @return cleaned string
	 */
	public String removeSpecialChar(String str){
		str = str.replace("}", "");
		str = str.replace("{", "");
		str = str.replace("]", "");
		str = str.replace("[", "");
		str = str.replace("#", "");
		str = str.replace("*", "");
		str = str.replace("&gt", "");
		str = str.replace("&apos", "");
		str = str.replace("%", "");
		str = str.replace("&quot", "");
		str = str.replace("&", "");
		str = str.replace("=", "");
		str = str.replace("?", "");
		str = str.replace(";", "");
		str = str.replace(":", "");
		str = str.replace(",", "");
		str = str.replace(".", "");
		str = str.replace(")", "");
		str = str.replace("(", "");
		str = str.replace("\t\t", "\t");
		str = str.replace("-", "");
		str = str.replace("  ", "");
		
		return str;
	}
	
	/**
	 * Handles external tags (and multiple abstract 
	 * text tags) present in a single paper
	 * @param str abstract content
	 * @return string without external tags 
	 */	
	public String processAbstract(String str){
		str = str.replace("  ", "");
		
		if(str.contains("Copyright") && !(str.contains(".</"))) str = str.replace("</", ".</");
		
		String[] remove = str.split("");
		StringBuilder sb = new StringBuilder();
		String temp = "";
		String abstrac = "";
		
		for(int i = 0; i < remove.length; i++){
			temp = temp + remove[i];
			
			//Handling inner/multiple abstract tags in the abstract text
			if(temp.contains("<AbstractText ")){
				temp = "";				
				do{
					i++;
				} while(!(remove[i].equalsIgnoreCase(">")));
			}
			//Handling the word "Copyright" before the end of abstract
			if(temp.contains("Copyright ")){							
				temp = "";
				do{
					i++;
					//an exception here can mean that a copyright information
					//tag content did not ended with a period
				}while(!(remove[i]).equalsIgnoreCase("."));
			}
			else sb.append(remove[i]);		
		}
		
		 abstrac = sb.toString();
		 abstrac = removeAbstractTags(abstrac);
				 
		 return abstrac;
	}
	

	/**
	 * Removes specific tags encountered on Abstract texts.
	 * This is used to clean the abstract text before 
	 * processing the feature count on the model. 
	 * @param str
	 * @return
	 */	
	public String removeAbstractTags(String str){		
		//this order of removing tags matters to 
		//exclude the first tag from the abstracts.
		
		str = str.replace("<AbstractText>", "");
		str = str.replace("<abstracttext>", "");
		str = str.replace("</abstracttext>", "");
		str = str.replace("<AbstractText", "");
		str = str.replace("<CopyrightInformation>", "");
		str = str.replace("</CopyrightInformation>", "");
		str = str.replace("Copyright", "");		
		str = str.replace("</AbstractText>", "");
		str = str.replace("<Abstract>", "");
		str = str.replace("</Abstract>", "");
		str = str.replace("<AbstractText.*?>", "");		
		
		return str;
	}
	
	
	/**
	 * Removes markup annotations of a
	 * text field, keeping its content
	 * 
	 * @param str text containing markups
	 * @return string with cleaned text 
	 */	
	public String removeTags(String str) {
		String[] remove = str.split("");
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < remove.length; i++){
			
			//iterating over the text until finding opening tag
			if(remove[i].equalsIgnoreCase("<")){
				do{
					i++;
				}
				//skipping the content until finding closing tag
				while(!(remove[i].equalsIgnoreCase(">")));
			}
			else sb.append(remove[i]);
		}				
		return sb.toString();
	}
	
	
	/**
	 * Displays the keys and values of the
	 * maps created.
	 * 
	 * @param hash  HashMap containing list,
	 * values, counts
	 */
	public void displayList(HashMap hash){
		Iterator<Object> itr = hash.keySet().iterator();
		int sum = 0;
		while(itr.hasNext()){
			Object str = itr.next();
			System.out.println("key: "+str+"\t value: "+hash.get(str));			
		}		
	}
	
	
	/**
	 * Exports hashmap of values extracted  
	 * from dataset to an external file
	 * 
	 * @param location folder, file name and file extension
	 * @param list values to be exported
	 */	
	public void exportFile(String location, HashMap list){

		String SEPARATOR = "\t";
		StringBuffer line = new StringBuffer();
		Iterator<Object> itr = list.keySet().iterator();
		
			try{
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(location), "UTF-8"));

				while(itr.hasNext()){
					Object str = itr.next();
					if(str != null){
						line.append(str).append(SEPARATOR).append(list.get(str));
						if(line.toString().contains("="))
							line.replace(line.indexOf("="), line.indexOf("=")+1,SEPARATOR);	
						//handling specificities from title content extraction
						if(line.toString().contains(","))
							line.replace(line.indexOf(","), line.indexOf(",")+1,SEPARATOR);						
					}
					if(itr.hasNext()){
						line.append(System.getProperty("line.separator"));					
					}
					writer.write(removeSpecialChar(line.toString()));					
					line.replace(0, line.length(), "");					
				}
				writer.flush();
				writer.close();
			}
			catch(UnsupportedEncodingException e){
				e.printStackTrace();
			}
			catch(FileNotFoundException e){
				e.printStackTrace();
			}
			catch(IOException e){
				e.printStackTrace();
			}		
			
	}
	
	
	/**
	 * Exports list of values extracted  
	 * from dataset to a string variable
	 * 
	 * @param list list of values to be exported
	 * @return string containing values on list
	 * @deprecated
	 */		
	public String exportContent(HashMap list){
		String SEPARATOR = "\t";
		Iterator<String> itr = list.keySet().iterator();
		StringBuffer export = new StringBuffer();		
		//try{
		while(itr.hasNext()){
			String str = itr.next();
			if(str != null){
				export.append(str).append(SEPARATOR).append(list.get(str));
				
				if(export.toString().contains("="))
					export.replace(export.indexOf("="), export.indexOf("=")+1,SEPARATOR);				
			}			
			if(itr.hasNext()){
				export.append("\n");
			}
		}		
		return removeSpecialChar(export.toString());			
	}
	
	
	/**
	 * Exports list of values extracted  
	 * from dataset to external file
	 * 
	 * @param location folder, file name and file extension
	 * @param list list of values to be exported
	 *
	 */	
	public void exportList(String location, ArrayList<String> list){

		String SEPARATOR = "\n";
		StringBuffer line = new StringBuffer();		

		try{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(location), "UTF-8"));

			for(int i = 0; i < list.size(); i++){
				String str = list.get(i);
				if(str != null){
					line.append(str).append(SEPARATOR);											
				}
			}
			writer.write(removeSpecialChar(line.toString()));

			writer.flush();
			writer.close();
		}
		catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}	

	}	
	
	public void initialize(){
				
	} 
	
		
	/**
	 * Accessors and mutators methods
	 * for Extractor class variables. 
	 * @return
	 */
	
	public String getId() {
		return id;
	}
	public String getEndId() {
		return endId;
	}
	public String getOpenFile() {
		return openFile;
	}
	public String getEndFile() {
		return endFile;
	}
	public String getopenAbst() {
		return openAbst;
	}
	public void setopenAbst(String openAbst) {
		this.openAbst = openAbst;
	}
	public String getOpenEC() {
		return openEC;
	}
	public void setOpenEC(String openEC) {
		this.openEC = openEC;
	}
	public String getAbstractLabel() {
		return abstractLabel;
	}
	public void setAbstractLabel(String abstractLabel) {
		this.abstractLabel = abstractLabel;
	}	
	public String getClassTag() {
		return classTag;
	}
	public void setClassTag(String classTag) {
		this.classTag = classTag;
	}
	public String getOpenTitle() {
		return openTitle;
	}
	public void setOpenTitle(String titleTag) {
		this.openTitle = titleTag;
	}
	public String getOpenJournal() {
		return openJournal;
	}
	public void setOpenJournal(String openJournal) {
		this.openJournal = openJournal;
	}

}