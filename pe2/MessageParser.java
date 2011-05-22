

/**
  The MessageParser class reads in a bunch of newsgroup messages
  from a list of directories specified on the command line, creates
  a corresponding MessageFeatures object that holds word counts
  (after doing stemming and removing stop words), and then
  writes all of the MessageFeatures objects out to disk in a single
  file.
  
  This is meant to be a pre-processing step to the assignment and 
  should speed things up because you won't need to re-read all 20,000
  files over the network for each part of the assignment.  You are welcome
  to modify the code any way you want, but you don't need to touch this
  file to complete the assignment.
**/

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import java.text.*;
import cs224n.util.Counter;

public class MessageParser {
	
	public static ArrayList<String> trainingDirs = new ArrayList<String>();
	public static HashSet<String> stopWords = new HashSet<String>();
	public static final String stopFile = "english.stop";
  public static final Stemmer stemmer = new Stemmer();
  
  /**
    Reads in stopwords from english.stop and puts them in the HashSet stopWords
  **/
	public static void loadStopWords() throws IOException, FileNotFoundException {
		File stops = new File(stopFile);
		BufferedReader in = new BufferedReader(new FileReader(stops));
		String line;
		while ((line = in.readLine()) != null)
			stopWords.add(stemmer.doStemming(line));
	}

  /**
    Parses all of the files in all of the directories in dir and writes
    the created MessageFeatures objects out to outputFile
  **/
  public static void parseTrainingDirs(String dir, String outputFile) throws IOException, FileNotFoundException {
    ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(
		  new GZIPOutputStream(new FileOutputStream(outputFile))));
		  
		String trainingDirs[] = new File(dir).list();
		
		// first write out the number of classes
		out.writeObject(new Integer(trainingDirs.length));
		
		for(int i=0; i<trainingDirs.length; i++){
      parseNewsgroupDir(i, dir+"/"+trainingDirs[i], out);
    }
    
    out.close();
  }
  
  /**
    Parses all of the files in a single diretory, writing the MessageFeatures objects
    to out.  Could easily be modified to return all of the messages in the directory rather than
    writing them out, if you want to use the given code to do stemming, stopword removal, 
    and word counting, but for some reason don't want to write the results out to disk.
  **/
	public static void parseNewsgroupDir(int dirNum, String dir, ObjectOutputStream out) throws IOException,
			FileNotFoundException {
		//ArrayList<MessageFeatures> messages = new ArrayList<MessageFeatures>();
		File directory = new File(dir);
		if (!directory.exists()) {
			System.err.println("Can't locate directory "+ dir);
			System.exit(-1);
		}
		File[] msgs = directory.listFiles();
		System.err.println("Parsing "+ dir +" with "+ msgs.length +" messages");
		for (File msg : msgs) {
		  MessageFeatures mf = new MessageFeatures(dirNum, msg.getAbsolutePath());
		  mf.parse(stemmer, stopWords);
  		out.writeObject(mf);
  		//messages.add(mf);
		}
		
		//return messages;
	}

  
	public static void main(String[] args) throws IOException,
			FileNotFoundException {
		if (args.length < 2) {
			System.out.println("Usage: java MessageParser directory outputFile");
			System.exit(-1);
		}

		// initialization stuff
		loadStopWords();

		// read in messages
		//Alternatively, your code could work directly with the corpus instead
		//of using the intermediate MessageFeature if you prefer to do so
		parseTrainingDirs(args[0], args[1]);
	}
}
