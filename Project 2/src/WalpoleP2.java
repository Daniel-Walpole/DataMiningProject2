import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.J48;
import weka.associations.Apriori;

public class WalpoleP2 {

    @SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
    	if(args.length >= 2) {
    		//need to output parameters of classifier and accuracy 
    		if(args[0].equals("1")) {
    	        //setting up taking in a .csv file for processing
    			DataSource source = new DataSource(args[1]);
    	        Instances data = source.getDataSet();
    	        data.setClassIndex(data.numAttributes() - 1);
    	        //converting it to be able to be processed by J48 algorithm
    	        NumericToNominal convert = new NumericToNominal();
    	        convert.setInputFormat(data);
    	        String [] options = new String[2];
    	        options[0] = "-R";
    	        options[1] = "last";
    	        convert.setOptions(options);
    	        data = Filter.useFilter(data, convert);
    	        //building the tree 
    	        J48 tree = new J48();
    	        tree.setUnpruned(true);
    	        tree.buildClassifier(data); 
    	        //having values to compare to for evaluation 
    	        Evaluation e = new Evaluation(data);
    	        e.crossValidateModel(tree, data, 10, new Random(0));
    	        //writing to file 
    	        BufferedWriter writer = new BufferedWriter(new FileWriter("classifyPreform.txt"));
    	        StringBuffer stuff = new StringBuffer();
    	        for(int i = 0; i < tree.getOptions().length; i++) {
    	        	stuff.append(tree.getOptions()[i]);
    	        }
    	        writer.write("Parameters: "+stuff + "\n");
    	        writer.write("Accuracy: " + String.valueOf(e.pctCorrect()) + "%");
    	        writer.close();
    		}
    		if(args[0].equals("2")) {
    			//taking in itemized .csv file
    			DataSource source = new DataSource(args[1]);
    	        Instances data = source.getDataSet();
    	        //converting to values that can be run by Apriori
    	        NumericToNominal convert = new NumericToNominal();
    	        convert.setInputFormat(data);
    	        String [] options = new String[2];
    	        options[0] = "-R";
    	        options[1] = "first-last";
    	        convert.setOptions(options);
    	        data = Filter.useFilter(data, convert);
    	        int classldx = data.numAttributes() - 1;
    	        data.setClassIndex(classldx);
    	        //Building the Apriori
    			Apriori apple = new Apriori();
    			apple.buildAssociations(data);
    			//Writing the results to the .csv file
    	        BufferedWriter writer = new BufferedWriter(new FileWriter("top10EPs.csv"));
    	        writer.write(apple.toString());
    	        writer.close();
    		}
    		if(args[0].equals("3")) {
    			//getting the patterns 
    			String patterns = args[2];
    			String [] patternSplit = patterns.split(",");
    			String [] item = new String[patternSplit.length];
    			String [] evaluator = new String[patternSplit.length];
    			String [] values = new String[patternSplit.length];
    			for(int i=0; i < patternSplit.length; i++) {
    				int less = patternSplit[i].indexOf('<');
    				int great = patternSplit[i].indexOf('>');
    				if(less > 0) {
        				item[i] = patternSplit[i].substring(0,less);
        				evaluator[i] = patternSplit[i].substring(less,less + 1);
        				values[i] = patternSplit[i].substring(less + 1);
    				}else if(great > 0) {
        				item[i] = patternSplit[i].substring(0,great);
        				evaluator[i] = patternSplit[i].substring(great,great + 1);
        				values[i] = patternSplit[i].substring(great + 1);
    				}
    			}
    			//getting information from file
    			BufferedReader csvFile = new BufferedReader(new FileReader(new File(args[1])));
    			int numCol = 0;
    			int numRows = 0;
    			String line;
				boolean numeric = true;
    			boolean firstline = true;
    			while((line = csvFile.readLine()) != null) {
    				if(firstline) {
        				String [] numbers = line.split(",");
        				numCol = numbers.length;
        				firstline = false;
    				}else {
    					String [] tmp = line.split(",");
    					try {
    						Double.parseDouble(tmp[numCol - 1]);
    					}catch (NumberFormatException e) {
    						numeric = false;
    					}
    				}
    				numRows = numRows + 1;
    			}
    			Double [][] newTable = new Double[numRows - 1][numCol];
    			csvFile.close();
    			csvFile = new BufferedReader(new FileReader(new File(args[1])));
    			String headers = csvFile.readLine();
    			String topRow = headers;
    			Scanner token;
    			int counter = 0;
    			while((headers = csvFile.readLine()) != null) {
    				token = new Scanner(headers);
    				token.useDelimiter(",");
    				for(int i = 0; i < numCol; i++) {
    					String value = token.next().toString();
    					newTable[counter][i] = Double.parseDouble(value);
    				}
    				counter++;
    			}
    			csvFile.close();
    			BufferedWriter newCSV = new BufferedWriter(new FileWriter(new File("./newCSV.csv")));
    			newCSV.write(topRow);
    			newCSV.write("\n");
    			boolean addToNewCSV = true;
				for(int x=0; x<counter;x++) {
					for(int y=0;y<patternSplit.length;y++) {
						if(evaluator[y].equals("<")) {
							if(!(newTable[x][Integer.parseInt(item[y].substring(1)) - 1] < (Double.parseDouble(values[y])))) {
								addToNewCSV = false;
							}
						}else if(evaluator[y].equals(">")){
							if(!(newTable[x][Integer.parseInt(item[y].substring(1)) - 1] > (Double.parseDouble(values[y])))) {
								addToNewCSV = false;
							}
						}
						System.out.println(addToNewCSV + "\t" + newTable[x][Integer.parseInt(item[y].substring(1))-1] + "\t" + item[y]+evaluator[y]+values[y]);
					}
					if(addToNewCSV) {
						StringBuilder s = new StringBuilder();
						for(int z=0; z<numCol;z++) {
							s.append(newTable[x][z]);
							if(z != 11) {
								s.append(",");
							}
						}
						//System.out.println(s.toString() + " Row: " + (x+1));
						s.append("\n");
						newCSV.write(s.toString());
						s.delete(0, s.length());
					}
					addToNewCSV = true;
				}
				newCSV.close();
    			DataSource source = new DataSource("./newCSV.csv");
    	        Instances data = source.getDataSet();
    	        data.setClassIndex(data.numAttributes() - 1);
    	        //converting it to be able to be processed by J48 algorithm
    	        NumericToNominal convert = new NumericToNominal();
    	        convert.setInputFormat(data);
    	        String [] options = new String[2];
    	        options[0] = "-R";
    	        options[1] = "last";
    	        convert.setOptions(options);
    	        data = Filter.useFilter(data, convert);
    	        //building the tree 
    	        J48 tree = new J48();
    	        tree.setUnpruned(true);
    	        tree.buildClassifier(data);
    	        //having values to compare to for evaluation 
    	        Evaluation e = new Evaluation(data);
    	        e.crossValidateModel(tree, data, 10, new Random(0));
    	        //writing to file 
    	        BufferedWriter writer = new BufferedWriter(new FileWriter("classifyXPreform.txt"));
    	        writer.write("Accuracy: " + String.valueOf(e.pctCorrect()) + "%\n");
    	        StringBuffer stuff = new StringBuffer();
    	        for(int i = 0; i < tree.getOptions().length; i++) {
    	        	stuff.append(tree.getOptions()[i]);
    	        }
    	        writer.write("Parameters: "+stuff + "\n");
    	        writer.write("Number of instances: " + tree.measureTreeSize());
    	        writer.close();
    		}
    	}
    }
}