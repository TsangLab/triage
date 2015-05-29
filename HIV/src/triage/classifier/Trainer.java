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


package triage.classifier;
import java.util.ArrayList;
import java.util.Random;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.LMT;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Range;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import triage.configure.ConfigConstants;
import triage.filter.InformedFilter;

/**
 * Trains and tests a classifier, 
 * executes k-fold cross validation on train data 
 * and outputs the classification results.
 * 
 * @author Hayda Almeida
 * @since 2014
 *
 */

public class Trainer {
	
	public static int SEED = 1; //the seed for randomizing the data
	public static int FOLDS = 5; //the # of folds to generate
	double[][] ranking;
	String rank;
	
	boolean verbose = false;
	

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
				
		String classifier = "";	
		
		//for(int i = 0; i < args.length; i++){
		try{

			classifier = args[0];

			if(classifier.length() > 1){
				if(classifier.contains("lmt"))
					classifier = "lmt";
				else if(classifier.contains("svm"))
					classifier = "svm";
				else classifier = "nb";				
			}
		}
			catch(Exception e){
				//	else{
				System.out.println("A classifier must be given as argument. Use: \n"
						+ "-lmt -> a LMT classifier; \n "
						+ "-svm -> a SVM classifier; \n"
						+ "-nb  -> a Naive Bayes classifier. ");
				System.exit(0);
			}
	//	}
		
		ConfigConstants pathVars = new ConfigConstants();
		Trainer evaluator = new Trainer();
		InformedFilter filter = new InformedFilter();			
		Classifier cls;
		
		//Creating classifier
		if(classifier.contains("lmt")) 
			cls = (Classifier) new LMT();
		else if (classifier.contains("svm")) 
			cls = (Classifier) new LibSVM();
		else 
			cls = (Classifier) new NaiveBayes();
       						
		//Loading train data
		DataSource sourceTrain = new DataSource(pathVars.HOME_DIR + pathVars.OUTPUT_MODEL + pathVars.ARFF_TRAIN);
		Instances trainData = sourceTrain.getDataSet();
		
		//Flagging the class index on the training data
		trainData.setClassIndex(trainData.numAttributes()-1);		
		System.out.println("Class index set on training data.");
		
		System.out.println("Training data loaded. Number of instances: " + trainData.numInstances() + "\n");	
					
		
		//Loading test data
		DataSource sourceTest = new DataSource(pathVars.HOME_DIR + pathVars.OUTPUT_MODEL + pathVars.ARFF_TEST);
		Instances testData = sourceTest.getDataSet();
		
		//Flagging the class index on the training data
		testData.setClassIndex(trainData.numAttributes()-1);		
		System.out.println("Class index set on testing data.");
		
		System.out.println("Test data loaded. Number of instances: " + testData.numInstances() + "\n");		
		
		
		//filter the file IDs, consider the new training set
		Instances filteredTrainData = evaluator.filteredIDs(trainData);
		Instances filteredTestData = evaluator.filteredIDs(testData);
		
		if(Boolean.valueOf(pathVars.USE_ODDS_RATIO)){
			//Calculate OddsRatio for all instances
			double[] OR = evaluator.loadFeatureFilter(filteredTrainData, filter, 1, Integer.parseInt(pathVars.OR_THRESHOLD));

			//Apply Odds Ratio filtering in instances
			filteredTrainData = evaluator.applyFilter(pathVars.OR_THRESHOLD, OR, filteredTrainData);
			filteredTestData = evaluator.applyFilter(pathVars.OR_THRESHOLD, OR, filteredTestData);
		}
		
		if(Boolean.valueOf(pathVars.USE_IDF)){
			//Calculate idf for all instances
			double[] idf = evaluator.loadFeatureFilter(filteredTrainData, filter, 2, Integer.parseInt(pathVars.IDF_THRESHOLD));
			
			//Apply idf filtering in instances
			filteredTrainData = evaluator.applyFilter(pathVars.IDF_THRESHOLD, idf, filteredTrainData);
			filteredTestData = evaluator.applyFilter(pathVars.IDF_THRESHOLD, idf, filteredTestData);
		}
				
		//Training and testing classifier
		evaluator.classify(filteredTrainData, filteredTestData, cls, testData);			
		
	}	
	
	/**
	 * Loads evaluation of attributes according
	 * to feature selection method provided.
	 * 
	 * @param data data instances
	 * @param filter informed filter instance 
	 * @param method identifier for selection method 
	 * @return
	 */
	private double[] loadFeatureFilter(Instances data, InformedFilter filter, int method, int threshold){
		
		double[] values = new double[data.numAttributes()];		
		
		switch(method){
		
		case 1:
			values = filter.oddsRatio(data, threshold);
			break;
		case 2:
			values = filter.idf(data, threshold);
			break;
		}		
		
		return values;		
	}	
	
	/**
	 * Uses evaluation of features according to 
	 * selection method to remove attributes from
	 * the dataset before training phase.
	 * 
	 * @param threshold selection method threshold
	 * @param values evaluation of attributes according to method 
	 * @param data dataset instances
	 * @return filtered dataset instances
	 * @throws Exception
	 */	
	private Instances applyFilter(String threshold, double[] values, Instances data) throws Exception{
		int numberRemoved = 0;
		
		String indexRemove = "";		
		
		for(int i = 0; i < values.length; i++){
			if(values[i] == 0){
				
				int ind = i+1;
				
				if(indexRemove.length()==0) indexRemove = ind + ""; 
				else indexRemove = indexRemove + "," + ind;
				
				numberRemoved++;
			}
		}
		
		try{
			indexRemove = indexRemove.substring(0, indexRemove.length()-1);
			//if(verbose)
			System.out.println("\n = = = = => Filter removed " + numberRemoved +" attributes: " + indexRemove.toString() );
		}
		catch (Exception e){
			System.out.println("\n = = = = => Filter threshold did not remove any attribute.");
			}
		
		Remove remove = new Remove();
		remove.setAttributeIndices(indexRemove);
		remove.setInvertSelection(false);		
		remove.setInputFormat(data);		
		
		Instances dataSubset = Filter.useFilter(data, remove);
		return dataSubset;		
	}
	
	
	/**
	 * Removes the ID attribute (index 1) 
	 * from a given dataset 
	 * 
	 * @param data instances
	 * @return filtered dataset
	 * @throws Exception
	 */
	private Instances filteredIDs(Instances data) throws Exception {
		Remove remove = new Remove();		
		//setting index to be removed
		remove.setAttributeIndices("1");
		remove.setInvertSelection(false);		
		remove.setInputFormat(data);
		
		Instances dataSubset = Filter.useFilter(data, remove);
		return dataSubset;
	}


	/**
	 * Trains and tests a classifier when two separated
	 * datasets are provided.
	 * 
	 * @param train training data to build classifier
	 * @param test  test data to evaluate classifier
	 * @param classif  type of classifier applied
	 * @throws Exception
	 */
	public void classify(Instances filteredTrain, Instances filteredTest, Classifier classif, Instances test) throws Exception{

		StringBuffer sb = new StringBuffer();
		PlainText prediction = new PlainText();
		Range attributesToShow = null;
		prediction.setBuffer(sb);
		prediction.setHeader(test);				
		prediction.setOutputDistribution(true);

		classif.buildClassifier(filteredTrain);

		Evaluation evaluateClassifier = new Evaluation(filteredTrain);		
		evaluateClassifier.evaluateModel(classif, filteredTest, prediction, attributesToShow, true);
		//evaluateClassifier.evaluateModel(classif, filteredTest);	

			stats(evaluateClassifier, classif);

		ArrayList<Prediction> output =  evaluateClassifier.predictions();		

		if(verbose){
		for(int i = 0; i < output.size(); i++){
			double act = output.get(i).actual();
			String actual;
			if(act == 1.0) actual = "negative"; else actual = "positive";

			double pred = output.get(i).predicted();
			String predicted;
			if(pred == 1.0) predicted = "negative"; else predicted = "positive";

			String value = test.instance(i).toString(0);

			System.out.println("PMID: "+ value + "\t" +
					"Actual: " + actual + "\t" +
					"Predicted: " + predicted								
					);	
		}	}			
	}

	
	/**
	 * Outputs classifier results.
	 * 
	 * @param eval  Evaluation model built by a classifier
	 * @param classif  type of classifier applied
	 * @throws Exception 
	 */
	public void stats(Evaluation eval, Classifier classif) throws Exception{		
		System.out.println("Number of attributes: " + eval.getHeader().numAttributes());
		System.out.println(eval.toSummaryString("\n======== RESULTS ========\n", false));
		System.out.println(eval.toClassDetailsString("\n\n======== Detailed accuracy by class ========\n"));
		System.out.println(eval.toMatrixString("\n\n======== Confusion Matrix ========\n"));		
	}
}