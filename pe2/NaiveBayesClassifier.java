import java.util.*;

public class NaiveBayesClassifier {
  
  //All of these need multiple passes - making an array (indexed by category number) ]
  //for ArrayLists (which contain MessageFeatures of that category)
  public static ArrayList<MessageFeatures>[] parseIterator(MessageIterator mi)
  {
      ArrayList<MessageFeatures>[] messageList = (ArrayList<MessageFeatures>[])new ArrayList[mi.numNewsgroups];
      for(int i = 0; i<mi.numNewsgroups;i++)
          messageList[i] = new ArrayList<MessageFeatures>();
      for(;;)
      {
          try
          {
              MessageFeatures mf = mi.getNextMessage();
              messageList[mf.newsgroupNumber].add(mf);
          }
          catch(Exception e)
          {
              break;
          }
      }
      return messageList;
  }
    
  public static void doBinomial(MessageIterator mi) {
      //
      ArrayList<MessageFeatures>[] messageList = parseIterator(mi);
      //System.out.println(messageList[0]);
  }
  
  public static void doBinomialChi2(MessageIterator mi) {
    // Your code here.
  }
  
  public static void doMultinomial(MessageIterator mi) {
    // Your code here.
  }
  
  public static void doTWCNB(MessageIterator mi) {
    // Your code here.
  }
  
  public static void outputProbability( final double[] probability )
  {
	  for ( int i = 0; i < probability.length; i ++ )
	  {
		  if ( i == 0 )
			  System.out.format( "%1.8g", probability[i] );
		  else
			  System.out.format( "\t%1.8g", probability[i] );
	  }
	  System.out.format( "%n" );
 }
  
  public static void main(String args[]) {
    if (args.length != 2) {
      System.err.println("Usage: NaiveBayesClassifier <mode> <train>");
      System.exit(-1);
    }
    String mode = args[0];
    String train = args[1];
    
    MessageIterator mi = null;
    try {
      mi = new MessageIterator(train);
    } catch (Exception e) {
      System.err.println("Unable to create message iterator from file "+train);
      e.printStackTrace();
      System.exit(-1);
    }
    
    if (mode.equals("binomial")) {
      doBinomial(mi);
    } else if (mode.equals("binomial-chi2")) {
      doBinomialChi2(mi);
    } else if (mode.equals("multinomial")) {
      doMultinomial(mi);
    } else if (mode.equals("twcnb")) {
      doTWCNB(mi);
    } else { 
      // Add other test cases that you want to run here.
      
      System.err.println("Unknown mode "+mode);
      System.exit(-1);
    }
  }
}