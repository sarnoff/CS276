

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import java.text.*;
import cs224n.util.Counter;

/**
  The MessageIterator class is meant to be used to
  access individual MessageFeatures objects sequentially 
  from the output of the MessageParser
**/

public class MessageIterator {

  ObjectInputStream in;
  int numNewsgroups;
  
  /**
    Creates a new MessageIterator, pass in the file name used
    as the output file when you ran the MessageParser
  **/
  public MessageIterator(String filename) throws IOException, 
      FileNotFoundException, ClassNotFoundException {
    in = new ObjectInputStream(new BufferedInputStream(
		  new GZIPInputStream(new FileInputStream(filename))));
		Integer i = (Integer)in.readObject();
		numNewsgroups = i.intValue();
  }
  
  public MessageFeatures getNextMessage() throws Exception{
    return (MessageFeatures)in.readObject();
  }
  
  public void close() throws IOException {
    in.close();
  }
  
  
  /**
    Just an example of how to use the class.  Reads in
    a single MessageFeatures object and prints out
    the subject and body word counts.
  **/
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("Usage: java MessageIterator inputFile");
			System.exit(-1);
		}

    MessageIterator mi = new MessageIterator(args[0]);

    // only printing first message
    MessageFeatures mf = mi.getNextMessage();
		System.out.println(mf.fileName+"("+mf.newsgroupNumber+")");
		
		for (String word : mf.subject.keySet()){
		  System.out.println("Count for "+ word +" is "+mf.subject.getCount(word));
		}
		
		for (String word : mf.body.keySet()) {
		  System.out.println("Count for "+ word +" is "+mf.body.getCount(word));
		}
	  
	  mi.close();
	}

}
