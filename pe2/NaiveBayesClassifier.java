import java.util.*;
import cs224n.util.*;

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
    
  public static int sizeOf(ArrayList<MessageFeatures>[] messageList)
  {
      int size = 0;
      for(int i = 0; i < messageList.length;i++)
          size += messageList[i].size();
      return size;
  }
    
  public static Map<String,double[]> trainBinomial(ArrayList<MessageFeatures>[] messageList)
  {
      int totalDocs = sizeOf(messageList);
      //array index category, map of word and (smoothed) probability
      Map<String,double[]> freqs = new HashMap<String,double[]>();
      Set<String>vocabulary = new HashSet<String>();
      Counter<String>[] counters = (Counter<String>[])new Counter[messageList.length];
      for(int i = 0; i < messageList.length;i++)
      {
          Counter<String> categoryCounter = new Counter<String>();
          //prior not needed in training - can be gotten later
          //double prior = categoryDocs/totalDocs;
          for(MessageFeatures mf:messageList[i])
          {
              //do the same thing for subject and body (for now)
              Set<String> set = new HashSet<String>();
              set.addAll(mf.subject.keySet());
              set.addAll(mf.body.keySet());
              vocabulary.addAll(set);
              categoryCounter.incrementAll(set,1.0);
          }
          counters[i] = categoryCounter;
      }
      int[] numTerms = new int[messageList.length];
      int[] categoryDocs = new int[messageList.length];
      for(int i=0;i<messageList.length;i++)
      {
          numTerms[i] = counters[i].size();
          categoryDocs[i] = messageList[i].size();
      }
      
      for(String term:vocabulary)
      {
          double[] probs = new double[messageList.length];
          for(int i=0;i<messageList.length;i++)
          {
              probs[i] = (1.0+counters[i].getCount(term))/((double)categoryDocs[i]+numTerms[i]);
              freqs.put(term,probs);
          }
      }
      
      return freqs;
  }
    
    
  private final static int MESSAGES_TO_CLASSIFY = 20;
  public static void classifyBinomial(Map<String,double[]> messageList)
  {
      int totalDocs = sizeOf(messageList);
      for(int i = 0; i < messageList.length;i++)
      {
          for(int j = 0; j<MESSAGES_TO_CLASSIFY;j++)
          {
              MessageFeatures mf = messageList[i].get(j);
          }
      }
  }
    
  public static void doBinomial(MessageIterator mi) {
      ArrayList<MessageFeatures>[] messageList = parseIterator(mi);
      Map<String,double[]> freqs = trainBinomial(messageList);
      
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