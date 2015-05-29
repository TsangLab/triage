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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import triage.configure.ConfigConstants;
import triage.filter.NaiveFilter;


/**
 * This class extracts and parses domain 
 * annotation features from XML doc instances.
 *   
 * @author Hayda Almeida
 * @since 2014
 */

public class KeywordExtractor extends Extractor{	
	
	public static void main(String[] args) {
		
		ConfigConstants pathVars = new ConfigConstants();
				
		KeywordExtractor kextrac = new KeywordExtractor();	
		NaiveFilter featFilter = new NaiveFilter();
		File featureDir = new File(pathVars.HOME_DIR + pathVars.FEATURE_DIR);
		String pathToListFile = pathVars.HOME_DIR + pathVars.ID_LIST;
		String previousLevel = pathVars.PREVIOUS_LEVEL;
		String targetLevel = pathVars.TARGET_LEVEL;
		int minLenght = Integer.parseInt(pathVars.FEATURE_MIN_LENGTH);
		
		kextrac.initialize(featureDir, pathVars);		
				
		//store abstract features and count
		HashMap<String,Integer> keyword_count = new HashMap<String,Integer>();	
		
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
					if(filename.contains("included")) 
						
						kextrac.loadKeywords(txt, keyword_count, minLenght);					
				}				
				
				else if(filename.contains(previousLevel)){
					if(filename.contains("fully"))
						
						kextrac.loadKeywords(txt, keyword_count, minLenght);
				}				
			}

		}catch(Exception e){
			throw new RuntimeException(e);

		}		
				
		
		featFilter.considerOccurence(keyword_count, pathVars);
		
		System.out.println("\n===========KEYWORD==EXPORT===============\n");
		kextrac.exportFile(featureDir + "/" + pathVars.SHARE_KEYWORD_FEATURES, keyword_count);
		System.out.println("..."+ keyword_count.size()+" unique keywords saved.");		
		System.out.println("\n=========================================\n");
		
	}
	
	
	
	/**
	 * Loads list of entities from external file
	 * 
	 * @param str list of entities
	 * @param pathVar constants from 
	 * @return
	 */	
	public HashMap<String,String> loadKeywords(File file, HashMap<String,Integer> keyword_count, int minLenght){
		
		String path = file.toString();
			
		HashMap<String,String> values = new HashMap<String,String>();
						
		try{

			BufferedReader reader = new BufferedReader(new FileReader(path));
			String line = "";
			String entryKeywords = "";
			String[] kWords;
			int kCount = 0;
			
			//loading query list
			while((line = reader.readLine()) != null){					

				if(line.length()>1){
					
					if(line.contains("KW  - ")){						
						
						entryKeywords = StringUtils.substringAfter(line,"  - ");
						while (!(line = reader.readLine()).contains("  - ")){
							entryKeywords = entryKeywords + " " + line;							
						}

						kWords = StringUtils.split(removeSpecialChar(entryKeywords));
						
						for(String keyword : kWords){

							if(keyword.length() >= minLenght){
								if(keyword_count.containsKey(keyword)){
									try{
										int cnt = keyword_count.get(keyword);								
										keyword_count.put(keyword, cnt+1);

									}catch(Exception e){
										keyword_count.put(keyword, 1);															
									}
								}					
								else{					
									keyword_count.put(keyword, 1);
								}
							}
						}
						
					}
				}
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
	

	@Override
	public void initialize(File featureDir, ConfigConstants pathVars){
				
		if(!featureDir.exists()){
			featureDir.mkdir();
		}
			try{
				File keywords = new File(featureDir + "/" + pathVars.SHARE_KEYWORD_FEATURES);
				keywords.createNewFile();
				
				File titleKeywords = new File(featureDir + "/" + pathVars.SHARE_KEYWORD_TITLE_FEATURES);
				titleKeywords.createNewFile();
				
			}catch(Exception e){
				System.out.println("Error creating features folder.");
				System.exit(0);
			}		
	}
	
	
	
	
	
	
}