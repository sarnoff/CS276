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
          //System.out.println("num terms "+i+" "+numTerms[i]);
          categoryDocs[i] = messageList[i].size();
      }
      
      for(String term:vocabulary)
      {
          double[] probs = new double[messageList.length];
          for(int i=0;i<messageList.length;i++)
          {
              double alpha = 0.5;
              probs[i] = (counters[i].getCount(term)+1.0)/((double)categoryDocs[i]+numTerms[i]);
              freqs.put(term,probs);
          }
      }
      
      return freqs;
  }
    
    
    
  public static int quickProbCheck( final double[] probability )
  {
      double max = probability[0];
      int maxI = 0;
      for ( int i = 1; i < probability.length; i ++ )
      {
          if ( probability[i] > max)
          {
              maxI = i;
              max = probability[i];
          }
      }
      return maxI;
  }
    
    
  private final static int MESSAGES_TO_CLASSIFY = 20;
  public static void classifyBinomial(ArrayList<MessageFeatures>[] messageList, Map<String,double[]> model)
  {
      //setup
      int totalDocs = sizeOf(messageList);
      double[] probs = new double[messageList.length];
      double numberRight = 0;
      
      //classification
      for(int i = 0; i < messageList.length;i++)
      {
          for(int j = 0; j<MESSAGES_TO_CLASSIFY;j++)
          {
              for(int k = 0; k < messageList.length; k++)
                  probs[k]=Math.log((double)messageList[k].size()/totalDocs);
              
              MessageFeatures mf = messageList[i].get(j);
              Set<String>terms = new HashSet<String>();
              terms.addAll(mf.subject.keySet());
              terms.addAll(mf.body.keySet());
              for(String term:terms)
              {
                  for(int k = 0; k < messageList.length;k++)
                  {
                      double count = mf.subject.getCount(term)+mf.body.getCount(term);
                      if(model.containsKey(term))//it should
                          probs[k]+=count*Math.log(model.get(term)[k]);
                  }
              }
              
              //System.out.println(quickProbCheck(probs));
              numberRight+=(quickProbCheck(probs)==mf.newsgroupNumber)?1:0;
              outputProbability(probs);
          }
      }
      //System.out.println("percent correctly id: "+numberRight/(MESSAGES_TO_CLASSIFY*messageList.length));
  }
    
  public static void doBinomial(MessageIterator mi) {
      ArrayList<MessageFeatures>[] messageList = parseIterator(mi);
      Map<String,double[]> freqs = trainBinomial(messageList);
      classifyBinomial(messageList, freqs);
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