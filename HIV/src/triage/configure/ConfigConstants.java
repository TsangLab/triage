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
***
*                   
* This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
*/

package triage.configure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Variables used by the software 
 * 
 * @authors Hayda Almeida, Marie-Jean Meurs
 * @since 2014
 *  
 */
public class ConfigConstants {	

	public static String CONFIG_FILE = "config.cfg";
	
	/**
	 * Default constructor
	 */
	public ConfigConstants() {
		initVars();
	}

	/**
	 * Constructor with custom parameter file.
	 * @param configfile
	 */
//	public ConfigConstants(String configfile) {
//		CONFIG_FILE = configfile;
//		initVars();
//	}
	
	public HashMap<String, String> CONFIG_MAP = new HashMap<String, String>();

	//Input files
	public String HOME_DIR;
	public String CORPUS_DIR;
	public String SOURCE_DIR;
	public String DUP_DIR;
	public String POS_DIR;
	public String NEG_DIR; 
	public String TRAIN_DIR; 
	public String TEST_DIR;
	public String FEATURE_DIR;	
	public String OUTPUT_MODEL;
	
	public String ID_LIST;
	public String PREVIOUS_LEVEL;
	public String TARGET_LEVEL;
	public String TRAINING_FILE;
	public String TEST_FILE;
	public String ARFF_TRAIN;
	public String ARFF_TEST;
	public String STOP_LIST;
	
	//corpus sampling
	public String SAMPLE_TRAIN;
	public String SAMPLE_TEST;
	public String PERCT_TEST; 
	public String PERCT_POS_TRAIN;
	public String PERCT_POS_TEST;
	
	//Output files
	public String JOURNAL_TITLE_FEATURES;
	public String MESH_FEATURES;
	public String KEYWORD_FEATURES;
	public String SHARE_KEYWORD_FEATURES;
	public String KEYWORD_TITLE_FEATURES;
	public String SHARE_KEYWORD_TITLE_FEATURES;
	public String NGRAM_FEATURES;
	public String TITLE_NGRAMS;
	public String DOC_IDS;
	
	//Feature setup
	public String USE_TEXT_SIZE;
	public String USE_JOURNAL_TITLE_FEATURE;
	public String USE_MESH_FEATURE;
	public String FEATURE_MIN_FREQ;
	public String FEATURE_MIN_LENGTH;
	
	//Feature setup - Keywords
	public String USE_KEYWORD_FEATURE;
	public String USE_SHARE_KEYWORD_FEATURE;
	public String USE_KEYWORD_TITLE_FEATURE;
	public String USE_SHARE_KEYWORD_TITLE_FEATURE;
	public String USE_DOC_ID;
		
	//Feature setup - Ngrams
	public String USE_NGRAM_FEATURE;
	public String USE_TITLE_NGRAMS;
	public String NGRAM_STOP;
	public String NGRAM_SIZE;
	public String USE_WEIGHTED_NGRAM;
	public String WEIGHT;

	//Feature filtering
	public String USE_ODDS_RATIO;
	public String OR_THRESHOLD;
	public String USE_IDF;
	public String IDF_THRESHOLD;

	//Task setup
	public String EXP_TYPE;	
	public String NB_PARAMS;		
	
	private void initVars() {
		String text = null;

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
			         this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)));
			//BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while ((text = reader.readLine()) != null) {
				if (! text.startsWith("#")) {
					String label = text.split("=")[0];
					String value = text.split("=")[1];
					CONFIG_MAP.put(label, value);
				}
			}
			reader.close();
		} catch (IOException ex) {
			Logger.getLogger(ConfigConstants.class.getName()).log(Level.SEVERE, null, ex);
		}
		HOME_DIR = CONFIG_MAP.get("HOME_DIR");
		CORPUS_DIR = CONFIG_MAP.get("CORPUS_DIR");
		SOURCE_DIR = CONFIG_MAP.get("SOURCE_DIR");
		DUP_DIR = CONFIG_MAP.get("DUP_DIR");
		POS_DIR = CONFIG_MAP.get("POS_DIR");
		NEG_DIR = CONFIG_MAP.get("NEG_DIR"); 
		TRAIN_DIR = CONFIG_MAP.get("TRAIN_DIR"); 
		TEST_DIR = CONFIG_MAP.get("TEST_DIR");
		FEATURE_DIR = CONFIG_MAP.get("FEATURE_DIR");		
		OUTPUT_MODEL = CONFIG_MAP.get("OUTPUT_MODEL");
		
		ID_LIST = CONFIG_MAP.get("ID_LIST");
		PREVIOUS_LEVEL = CONFIG_MAP.get("PREVIOUS_LEVEL");
		TARGET_LEVEL = CONFIG_MAP.get("TARGET_LEVEL");
		TRAINING_FILE = CONFIG_MAP.get("TRAINING_FILE");
		TEST_FILE = CONFIG_MAP.get("TEST_FILE");
		ARFF_TRAIN = CONFIG_MAP.get("ARFF_TRAIN");
		ARFF_TEST = CONFIG_MAP.get("ARFF_TEST");
		STOP_LIST = CONFIG_MAP.get("STOP_LIST");
		
		SAMPLE_TRAIN = CONFIG_MAP.get("SAMPLE_TRAIN");
		SAMPLE_TEST = CONFIG_MAP.get("SAMPLE_TEST"); 
		PERCT_TEST = CONFIG_MAP.get("PERCT_TEST"); 
		PERCT_POS_TRAIN = CONFIG_MAP.get("PERCT_POS_TRAIN");
		PERCT_POS_TEST = CONFIG_MAP.get("PERCT_POS_TEST");
				
		JOURNAL_TITLE_FEATURES = CONFIG_MAP.get("JOURNAL_TITLE_FEATURES");		
		MESH_FEATURES = CONFIG_MAP.get("MESH_FEATURES");
		KEYWORD_FEATURES = CONFIG_MAP.get("KEYWORD_FEATURES");
		SHARE_KEYWORD_FEATURES = CONFIG_MAP.get("SHARE_KEYWORD_FEATURES");
		KEYWORD_TITLE_FEATURES = CONFIG_MAP.get("KEYWORD_TITLE_FEATURES");
		SHARE_KEYWORD_TITLE_FEATURES = CONFIG_MAP.get("SHARE_KEYWORD_TITLE_FEATURES");
		NGRAM_FEATURES = CONFIG_MAP.get("NGRAM_FEATURES");
		TITLE_NGRAMS = CONFIG_MAP.get("TITLE_NGRAMS");
		DOC_IDS = CONFIG_MAP.get("DOC_IDS");
		
		USE_TEXT_SIZE = CONFIG_MAP.get("USE_TEXT_SIZE");
		USE_JOURNAL_TITLE_FEATURE = CONFIG_MAP.get("USE_JOURNAL_TITLE_FEATURE");	
		USE_MESH_FEATURE = CONFIG_MAP.get("USE_MESH_FEATURE");
		FEATURE_MIN_FREQ = CONFIG_MAP.get("FEATURE_MIN_FREQ");
		FEATURE_MIN_LENGTH = CONFIG_MAP.get("FEATURE_MIN_LENGTH");
		
		USE_KEYWORD_FEATURE = CONFIG_MAP.get("USE_KEYWORD_FEATURE");
		USE_SHARE_KEYWORD_FEATURE = CONFIG_MAP.get("USE_SHARE_KEYWORD_FEATURE");
		USE_KEYWORD_TITLE_FEATURE = CONFIG_MAP.get("USE_KEYWORD_TITLE_FEATURE");
		USE_SHARE_KEYWORD_TITLE_FEATURE = CONFIG_MAP.get("USE_SHARE_KEYWORD_TITLE_FEATURE");
		USE_DOC_ID = CONFIG_MAP.get("USE_DOC_ID");
		
		USE_NGRAM_FEATURE = CONFIG_MAP.get("USE_NGRAM_FEATURE");
		USE_TITLE_NGRAMS = CONFIG_MAP.get("USE_TITLE_NGRAMS");
		NGRAM_STOP = CONFIG_MAP.get("NGRAM_STOP");		
		NGRAM_SIZE = CONFIG_MAP.get("NGRAM_SIZE");
		USE_WEIGHTED_NGRAM = CONFIG_MAP.get("USE_WEIGHTED_NGRAM");
		WEIGHT = CONFIG_MAP.get("WEIGHT");
		
		USE_ODDS_RATIO = CONFIG_MAP.get("USE_ODDS_RATIO");
		OR_THRESHOLD = CONFIG_MAP.get("OR_THRESHOLD");
		USE_IDF = CONFIG_MAP.get("USE_IDF");
		IDF_THRESHOLD = CONFIG_MAP.get("IDF_THRESHOLD");
				
		EXP_TYPE = CONFIG_MAP.get("EXP_TYPE");		
		NB_PARAMS = CONFIG_MAP.get("NB_PARAMS");				
	}
}
