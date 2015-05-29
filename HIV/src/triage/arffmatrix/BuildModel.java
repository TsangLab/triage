package triage.arffmatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import triage.analyse.Extractor;
import triage.arffvector.CreateVector;
import triage.configure.ConfigConstants;

/**
 * This class reads the corpus instances and uses
 * the CreateVector class to generate a model file (ARFF) *  
 *
 * @author Hayda Almeida, Marie-Jean Meurs
 * @since 2014
 *
 */

public class BuildModel {
	
	private static String configfile =  null;
	
	public static void main(String[] args) {
		
		//-----------------------------------
		// instantiate classes of constants
		// and configuration file.
		//-----------------------------------

		ConfigConstants pathVars = new ConfigConstants();

		Extractor model = new Extractor();
		File outputDir = new File(pathVars.HOME_DIR + pathVars.OUTPUT_MODEL);
		
		model.initialize(outputDir, pathVars);
		
		CreateVector vectorgenerator = new CreateVector(pathVars);
		String attributes = vectorgenerator.informFeatures(pathVars);
		System.out.println("Features loaded ...");
		
		// name output ARFF files
		String timeStamp = new SimpleDateFormat("yyyyMMdd_hh:mm").format(new Date());
		String arffFileName = "triage" + pathVars.EXP_TYPE + "_"+ pathVars.PERCT_POS_TRAIN + attributes +"_"+ timeStamp + ".arff";
				
		try 
	    {		
			//by default
			String sortarffFileName = pathVars.HOME_DIR + pathVars.OUTPUT_MODEL + arffFileName; // default
			
			// create file			
			BufferedWriter out = new BufferedWriter(new FileWriter(sortarffFileName));
			 
			// load ARFF header and write it
			String outHeaderArff = vectorgenerator.genArffHeader(pathVars,Integer.parseInt(pathVars.EXP_TYPE));
			//System.out.println(outHeaderArff); // verbose
			out.write(outHeaderArff + "\n");			

			// reader for corpus
			BufferedReader reader = null;
			//train corpus
			if(Integer.parseInt(pathVars.EXP_TYPE) == 0)
				reader = new BufferedReader(new FileReader(pathVars.HOME_DIR + pathVars.CORPUS_DIR + "L" + pathVars.TARGET_LEVEL + "/"+ pathVars.TRAINING_FILE));	
			//test corpus
			else if(Integer.parseInt(pathVars.EXP_TYPE) ==1)
				reader = new BufferedReader(new FileReader(pathVars.HOME_DIR + pathVars.CORPUS_DIR + "L" + pathVars.TARGET_LEVEL + "/"+ pathVars.TEST_FILE));
						
	        //--------------------------------------------
	        // repeat until all lines have been read
	        // from the file
	        //--------------------------------------------
			String text = null;
			String content = null;
			
			String abstracttext = "";
			String journaltitle = "";
			String title = "";
			String meshterms = "";
			String classtriage = "";
			int hasText = 0;
			int journaltitlecount = 0;
			int abstracttitlecount = 0;
			int abstracttextcount = 0;
			int positivecount = 0;
			int negativecount = 0;
			
			
	        while ((text = reader.readLine()) != null) { 		        	
	        	
	        	// detect a PubMed abstract
	        	if (text.contains("<PMID Version=\"1\">")){
	        		
	        		// Reinitialize journal title 
	        		 journaltitle = "";
	        		 
	        		// Reinitialize abstract title 
	        		 title = ""; 
	        		
	        		 // Reinitialize abstract text 
	        		 abstracttext = ""; 
	        		 
	        		 // Reinitialize hasText to false
	        		hasText = 0;
	        		
	        		String pmid = text.replaceFirst("<PMID Version=\"1\">", "");
	        		pmid = pmid.replace("</PMID>", "").trim();
	        		
	        		
	        		// continue to read
	        		content = reader.readLine();
	        		content = content.replaceAll("\t", "");
	        		content = content.replaceFirst("\\s+", "");	        		
	        		
	        		while ( ! content.contentEquals("</PubmedArticle>") ) {
	        			
	        			if (content.contains("<Title>")){
	        				
	        				journaltitlecount++;
	        				System.out.println("#: " + journaltitlecount + "\t PMID : " + pmid);
	        				
	        				content = content.replace("<Title>", "");
	        				content = content.replace("</Title>", "");
	        				journaltitle = content;
	        				//	System.out.println("Journal title : " + content);
	        			}
	        			
	        			if (content.contains("<ArticleTitle>")){
	        				
	        				abstracttitlecount++;
	        				
	        				content = content.replace("<ArticleTitle>", "");
	        				content = content.replace("</ArticleTitle>", "");
	        				title = content;
	        				//	System.out.println("Paper title : " + content);
	        			}
	        			
	        			        			
	        			if (content.contains("<AbstractText>")){

	        				abstracttextcount++;
	        				hasText = 1; // use it to indicate if the abstract has some text or not 

	        				content = content.replace("<AbstractText>", "");
	        				
	        				//checks if there are empty lines after AbstractText tag
	        				//and keeps reading until finds the abstract content
	        				while(content.isEmpty()){
	        						content = reader.readLine();     					
	        				}	        				
	        					abstracttext = abstracttext + content; 	        					
	        					// clean
	        					abstracttext = model.removeAbstractTags(abstracttext);        					
	        				

	        				content = reader.readLine();
	        				// converting toLowerCase is not relevant in bio context
	        				// because it introduces ambiguities (ie Gene name / Enzyme alias)
	        				// abstracttext = abstracttext.toLowerCase();
	        			}

	        			if (content.contains("<AbstractText ")){       				        				
	        				
	        				String temp = "";
							String newAbs = "<AbstractText>";
							
							if(content.contains("</Abstract>")){
								temp = temp + model.processAbstract(content);
							}
							else{
								do{							
									temp = temp + model.processAbstract(content);								
									content = reader.readLine();							
								}while(!(content.contains("</Abstract>")));
							}
							
							newAbs = newAbs + temp;
							content = newAbs + "</AbstractText>"; 
							
							abstracttext = content;
							abstracttext = model.removeAbstractTags(abstracttext);
							
							content = reader.readLine();
								        				
	        			}	        			
	        			
	        			if (content.contains("<DescriptorName") || content.contains("<QualifierName")){
	        				content = content.replaceFirst("(?s)<DescriptorName.*?>", "");	        				
							content = content.replace("</DescriptorName>", "");							
							meshterms = meshterms + " " + content;								
							
	        			}
	        			
	        			if (content.contains("<QualifierName")){	        				
	        				content = content.replaceFirst("(?s)<QualifierName.*?>", "");							
							content = content.replace("</QualifierName>", "");
							meshterms = meshterms + " " + content;								
							
	        			}
	        			
	        			if (content.contains("<TRIAGE>")){
        				
        				content = content.replace("<TRIAGE>", "");
        				content = content.replace("</TRIAGE>", "");
        				classtriage = content;
        				if(content.contains("positive")){
        					positivecount++;
        				}
        				if(content.contains("negative")){
        					negativecount++;
        				}
        				//	System.out.println("Triage classification : " + content);
        			}
	        			
	        			content = reader.readLine();
	        			content = content.replaceAll("\t", "");
	        			content = content.replaceFirst("\\s+", "");
	        		}
	        		
	        		//System.out.println("Abstract : " + abstracttext.toString() + "\n\n");

	        		// end of if: collect data and write ARFF
	        		String Arffline = vectorgenerator.getArffLine(pmid,
	        				journaltitle, 
	        				title, 
	        				abstracttext,
	        				meshterms,
	        				classtriage,	        				
	        				Integer.parseInt(pathVars.EXP_TYPE)
	        				);
	        		
	        		Arffline = Arffline + "\n";
	        		// write line on disc
	        		out.write(Arffline);	        		
	        		// out.write(id + " " + Arffline + "\n"); // 	        		
	        	}      	
	        	
	        }
	        
	        System.out.println(
	        		"Abstracts processed: " + abstracttitlecount
	        		+ "\t with text content: " + abstracttextcount
	        		+ "\t from " + journaltitlecount + " journals"
	        		+ "\nTotal of: \n" + positivecount + " positive"
	        		+ "\t and " + negativecount + " negative documents");
	        out.write("\n");
	        out.close();
	        
	        reader.close();
	      
	        
	    }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }		
		
	}	
	
}