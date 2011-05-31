import java.util.*;
import cs224n.util.*;
import cs224n.util.PriorityQueue;

@SuppressWarnings("unchecked")
public class NaiveBayesClassifier {
	private static int K = 10;//changable for testing
    public static final int MESSAGES_TO_CLASSIFY = 20;
    public static final int FEATURES_PER_NEWSGROUP = 300;
    //should be false when turned in
    private static final boolean DEBUG = false;
    private static final boolean QUICK_PROB_CHECK = false;
    
  
    /**
     *
     * Helper Methods
     *
     **/
    
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
  
  public static ArrayList<MessageFeatures> getMessages(MessageIterator mi) {
	  ArrayList<MessageFeatures> list = new ArrayList<MessageFeatures>();
	  while(true) {
		  try
          {
              MessageFeatures mf = mi.getNextMessage();
              list.add(mf);
          } catch(Exception e) {
        	  return list;
          }
	  }
  }
  
  public static KFold getFolds(ArrayList<MessageFeatures> list, int fold, int num)
  {
      ArrayList<MessageFeatures>[] test = (ArrayList<MessageFeatures>[])new ArrayList[num];
      ArrayList<MessageFeatures>[] train = (ArrayList<MessageFeatures>[])new ArrayList[num];
      
      for(int i = 0; i < num; i++) {
          train[i] = new ArrayList<MessageFeatures>();
          test[i] = new ArrayList<MessageFeatures>();
      }
      
      for(int i = 0; i < list.size(); i++) {
    	  MessageFeatures mf = list.get(i);
    	  if(i % K == fold) {
    		  test[mf.newsgroupNumber].add(mf);
    	  } else {
    		  train[mf.newsgroupNumber].add(mf);
    	  }
      }
      
      KFold folds = new KFold();
      folds.test = test;
      folds.train = train;
      return folds;
  }
    
  public static int sizeOf(ArrayList<MessageFeatures>[] messageList)
  {
      int size = 0;
      for(int i = 0; i < messageList.length;i++)
      {
          size += messageList[i].size();
          //System.err.println("class "+i+":"+messageList[i].size());
      }
      return size;
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
	  
    
    /**
     *
     * Binomial Methods
     *
     **/
    
    public static Counter<String>[] prepBinomial(ArrayList<MessageFeatures>[] messageList)
    {
        //array index category, map of word and (smoothed) probability
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
                categoryCounter.incrementAll(set,1.0);
            }
            counters[i] = categoryCounter;
        }
        
        return counters;
    }
    
    public static Map<String,double[]> trainBinomial(ArrayList<MessageFeatures>[] messageList, Counter<String>[] counters)
    {
        return trainBinomial(messageList, counters, null);
    }
    
    public static Map<String,double[]> trainBinomial(ArrayList<MessageFeatures>[] messageList, Counter<String>[] counters, ArrayList<String>[] featureSet)
    {
        Map<String,double[]> freqs = new HashMap<String,double[]>();
        //int[] numTerms = new int[messageList.length];
        int[] categoryDocs = new int[messageList.length];
        for(int i=0;i<messageList.length;i++)
        {
            /*
             if(featureSet!=null)
                numTerms[i] = featureSet[i].size();
            else
                numTerms[i] = counters[i].size();
             */
            //System.err.println("num terms "+i+" "+numTerms[i]);
            categoryDocs[i] = messageList[i].size();
        }
        
        Set<String> vocab = new HashSet<String>();
        for(Counter<String> c:counters)
            vocab.addAll(c.keySet());
        
        if(featureSet!=null)
        {
            Set<String> features = new HashSet<String>();
            for(ArrayList<String> a:featureSet)
                features.addAll(a);
            vocab.retainAll(features);
        }
        
        for(String term:vocab)
        {
            double[] probs = new double[messageList.length];
            for(int i=0;i<messageList.length;i++)
            {
                probs[i] = (counters[i].getCount(term)+1.0)/((double)categoryDocs[i]+vocab.size());//numTerms[i]);
                freqs.put(term,probs);
            }
        }
        
        return freqs;
    }

    public static double classifyBinomial(ArrayList<MessageFeatures>[] messageList, Map<String,double[]> model)
    {
        return classifyBinomial(messageList, model, null);
    }

  public static double classifyBinomial(ArrayList<MessageFeatures>[] messageList, Map<String,double[]> model, ArrayList<String>[] featureSet)
  {
      //setup
      int totalDocs = sizeOf(messageList);
      double[] probs = new double[messageList.length];
      double numberRight = 0;
      
      //features
      Set<String> features=new HashSet<String>();;
      if(featureSet!=null)
          for(ArrayList<String> f:featureSet)
              features.addAll(f);
      
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
              if(featureSet!=null)
                  terms.retainAll(features);
              
              for(String term:terms)
              {
                  for(int k = 0; k < messageList.length;k++)
                  {
                      double count = mf.subject.getCount(term)+mf.body.getCount(term);
                      if(model.containsKey(term))
                          probs[k]+=Math.log(model.get(term)[k]);
                  }
              }
              if(QUICK_PROB_CHECK)
                  System.err.println(quickProbCheck(probs));
              else
                  outputProbability(probs);
              numberRight+=(max(probs)==mf.newsgroupNumber)?1:0;
          }
      }
      if(DEBUG)
          System.err.println("percent correctly id: "+numberRight/(MESSAGES_TO_CLASSIFY*messageList.length));
      return (numberRight * 100.0) /(MESSAGES_TO_CLASSIFY * messageList.length);
  }
    
  public static void doBinomial(MessageIterator mi) {
      ArrayList<MessageFeatures>[] messageList = parseIterator(mi);
      Map<String,double[]> freqs = trainBinomial(messageList,prepBinomial(messageList));
      classifyBinomial(messageList, freqs);
  }
    
    /**
     *
     * Chi Squared Methods
     *
     **/
    
    public static ArrayList<String>[] getFeatureSet(ArrayList<MessageFeatures>[] messageList, Counter<String>[] counters)
    {
        Counter<String>[] features = (Counter<String>[])new Counter[messageList.length];
        double N = 0;
        for(Counter<String> c:counters)
            N+=c.totalCount();
        for(int i = 0; i <counters.length;i++)
        {
            features[i]=new Counter<String>();
            Counter<String> c = counters[i];
            double count = c.totalCount();
            for(String term:c.keySet())
            {
                double A = c.getCount(term);
                double C = count-A;
                //for term in doc not in class
                double B = -A;
                for(Counter<String> c2:counters)
                    B+=c2.getCount(term);
                double D = N-A-B-C;
                double chi2 = N*(A*D-C*B)*(A*D-C*B)/((A+C)*(B+D)*(A+B)*(C+D));
                features[i].setCount(term,chi2);
            }
        }
        
        ArrayList<String>[] featureSet = (ArrayList<String>[])new ArrayList[features.length];
        for(int i = 0;i<features.length;i++)
        {
            PriorityQueue<String> pq = features[i].asPriorityQueue();
            featureSet[i] = new ArrayList<String>();
            for(int j = 0; j < FEATURES_PER_NEWSGROUP&&pq.hasNext();j++)
                featureSet[i].add(pq.next());
        }
        return featureSet;
    }
  
    private final static int TOP_WORDS_TO_PRINT = 20;
  public static void doBinomialChi2(MessageIterator mi) {
    //outputting 20 best words for each newsgroup
      ArrayList<MessageFeatures>[] messageList = parseIterator(mi);
      Counter<String>[] counters = prepBinomial(messageList);
      ArrayList<String>[] featureSet = getFeatureSet(messageList,counters);
      for(List<String> l:featureSet)
      {
          if(TOP_WORDS_TO_PRINT>0)
              System.out.print(l.get(0));
          for(int i = 1; i < TOP_WORDS_TO_PRINT&&i<l.size(); i++)
              System.out.print("\t"+l.get(i));
          if(TOP_WORDS_TO_PRINT>0)
              System.out.println("");
      }
      Map<String,double[]> freqs = trainBinomial(messageList,counters,featureSet);
      classifyBinomial(messageList,freqs, featureSet);
  }
    
    /**
     *
     * Multinomial Methods
     *
     */
  
  public static void doMultinomial(MessageIterator mi) {
	  ArrayList<MessageFeatures>[] messageList = parseIterator(mi);
	  MultinomialClassifier mc = new MultinomialClassifier(messageList);
	  classifyMultinomial(mc, messageList);
  }
    
    public static double classifyMultinomial(MultinomialClassifier mc, ArrayList<MessageFeatures>[] messageList) {
        int numClasses = messageList.length;
        double accurate = 0;
        for(int klass = 0; klass < numClasses; klass++) {
            for(int feature = 0; feature < MESSAGES_TO_CLASSIFY; feature++) {
                MessageFeatures mf = messageList[klass].get(feature);
                double[] score = mc.classifyFeature(mf);
                int mostLikelyNewsgroup = max(score);
                if(mostLikelyNewsgroup == klass) accurate++;
                System.out.print(mostLikelyNewsgroup + "" + '\t');
            }
            System.out.print('\n');
        }	
        
        double per = accurate / (numClasses * MESSAGES_TO_CLASSIFY);
        if(DEBUG)
        {
        	  System.err.println("Accurate: "+accurate);
        	  System.err.println("Out of: "+(numClasses * MESSAGES_TO_CLASSIFY));
        	  System.err.println("Accuracy: "+(per * 100) + "%");
        }
        return per * 100;
    }
    
    /**
     *
     * Multinomial feature selected
     *
     **/
    
    public static void doFeatureMultinomial(MessageIterator mi) {
        ArrayList<MessageFeatures>[] messageList = parseIterator(mi);
        MultinomialClassifierWithFeatures mc = new MultinomialClassifierWithFeatures(messageList);
        classifyMultinomial(mc, messageList);
    }
    
    /**
     *
     * K-Fold Methods
     *
     **/
  
  public static void doKFoldUpweighted(MessageIterator mi) {
	  ArrayList<MessageFeatures> list = getMessages(mi);
	  int avg = 0;
	  for(int fold = 0; fold < K; fold++) {
		  KFold folds = getFolds(list, fold, mi.numNewsgroups);
		  MultinomialClassifier mc = new MultinomialClassifier(folds.train);
//		  mc.upweightSubjects(folds.train);
		  mc.trainWCNB();
		  avg += classifyTWCNB(mc, folds.test);
	  }
	  System.err.println("Average accuracy: "+(avg/10) + "%");
	  System.err.println();
  }
  
  public static double doKFoldMultinomial(MessageIterator mi) {
	  ArrayList<MessageFeatures> list = getMessages(mi);
	  int avg = 0;
	  for(int fold = 0; fold < K; fold++) {
		  KFold folds = getFolds(list, fold, mi.numNewsgroups);
		  MultinomialClassifier mc = new MultinomialClassifier(folds.train);
		  avg += classifyMultinomial(mc, folds.test);
	  }
	  System.err.println("Average accuracy: "+(avg/10) + "%");
	  System.err.println();
      return (avg/10);
  }
    
    public static double doKFoldFeatureMultinomial(MessageIterator mi) {
        ArrayList<MessageFeatures> list = getMessages(mi);
        int avg = 0;
        for(int fold = 0; fold < K; fold++) {
            KFold folds = getFolds(list, fold, mi.numNewsgroups);
            MultinomialClassifierWithFeatures mc = new MultinomialClassifierWithFeatures(folds.train);
            avg += classifyMultinomial(mc, folds.test);
        }
        
        System.err.println("Average accuracy: "+(avg/10) + "%");
        System.err.println();
        return (avg/10);
    }
  
    public static double doKFoldBinomial(MessageIterator mi) {
        ArrayList<MessageFeatures> list = getMessages(mi);
        int avg = 0;
        for(int fold = 0; fold < K; fold++) {
            KFold folds = getFolds(list, fold, mi.numNewsgroups);
            Map<String,double[]> freqs = trainBinomial(folds.train, prepBinomial(folds.train));
            avg += classifyBinomial(folds.test, freqs);
        }
        
        System.err.println("Average accuracy: "+(avg/10) + "%");
        System.err.println();
        return (avg/10);
    }
    
    public static double doKFoldChi2(MessageIterator mi) {
        ArrayList<MessageFeatures> list = getMessages(mi);
        int avg = 0;
        for(int fold = 0; fold < K; fold++) {
            KFold folds = getFolds(list, fold, mi.numNewsgroups);
            Counter<String>[] counters = prepBinomial(folds.train);
            ArrayList<String>[] featureSet = getFeatureSet(folds.train,counters);
            Map<String,double[]> freqs = trainBinomial(folds.train, counters, featureSet);
            avg += classifyBinomial(folds.test, freqs, featureSet);
        }
        
        System.err.println("Average accuracy: "+(avg/10) + "%");
        System.err.println();
        return (avg/10);
    }
  
  private static int max(double[] score) {
	  int klass = 0;
	  double max = score[0];
	  for(int i = 1; i < score.length; i++) {
		  if(score[i] > max) {
			  klass = i;
			  max = score[i];
		  }
	  }
	  return klass;
  }
  
  public static void doTWCNB(MessageIterator mi) {
	  ArrayList<MessageFeatures>[] messageList = parseIterator(mi);
	  MultinomialClassifier mc = new MultinomialClassifier(messageList);
	  mc.trainWCNB();
	  classifyTWCNB(mc, messageList);
  }
  
  private static double classifyTWCNB(MultinomialClassifier mc, ArrayList<MessageFeatures>[] messageList) {
	  int numClasses = messageList.length;
	  double accurate = 0;
	  int classified = 0;
	  for(int klass = 0; klass < numClasses; klass++) {
		  for(int feature = 0; feature < MESSAGES_TO_CLASSIFY; feature++) {
			  if(feature >= messageList[klass].size()) continue;
			  classified++;
			  MessageFeatures mf = messageList[klass].get(feature);
			  double[] score = mc.classifyTWCNBFeature(mf);
			  int mostLikelyNewsgroup = max(score);
			  if(mostLikelyNewsgroup == klass) accurate++;
//			  System.err.print(mostLikelyNewsgroup + "" + '\t');
		  }
//		  System.err.print('\n');
	  }	

	  double per = accurate / (classified);
//	  System.err.println("Accurate: "+accurate);
//	  System.err.println("Out of: "+(classified));
//	  System.err.println("Accuracy: "+(per * 100) + "%");
	  return per * 100;
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
    
    public static MessageIterator makeMI(String train)
    {
        MessageIterator mi = null;
        try {
            mi = new MessageIterator(train);
        } catch (Exception e) {
            System.err.println("Unable to create message iterator from file "+train);
            e.printStackTrace();
            System.exit(-1);
        }
        return mi;
    }
    
    public static void doKFoldAll(String train)
    {
        int [] kValues = {2,3,4,5,10,20,30};
        double [][] percents = new double [kValues.length][4];
        for(int i = 0; i < kValues.length;i++)
        {
            K=kValues[i];
            percents[i][0] = doKFoldBinomial(makeMI(train));
            percents[i][1] = doKFoldChi2(makeMI(train));
            percents[i][2] = doKFoldMultinomial(makeMI(train));
            percents[i][3] = doKFoldFeatureMultinomial(makeMI(train));
            System.err.println("\n\n\n\n\n"+K+"\n\n\n\n\n");
        }
        String s = "";
        for(int i = 0; i < kValues.length; i++)
        {
            s+=kValues[i];
            for(int j = 0; j<4;j++)
            {
                s+="\t"+(100-percents[i][j]);
            }
            s+="\n";
        }
        System.err.println("\n\n\n\n\n"+s);
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
    } else if (mode.equals("multinomial-chi2")) {
        doFeatureMultinomial(mi);
    } 
    else if (mode.equals("twcnb")) {
      doTWCNB(mi);
    } else if (mode.equals("kfold-multinomial")) {
    	doKFoldMultinomial(mi);
    } else if (mode.equals("kfold-multinomial-chi2")) {
    	doKFoldFeatureMultinomial(mi);
    } else if (mode.equals("kfold-binomial")) {
    	doKFoldBinomial(mi);
    } else if (mode.equals("kfold-chi2")) {
    	doKFoldChi2(mi);
    } else if (mode.equals("upweighted")) {
    	doKFoldUpweighted(mi);
    } else if (mode.equals("kfold-test")) {
    	doKFoldAll(train);
    } 
    else { 
      // Add other test cases that you want to run here.
      System.err.println("Unknown mode "+mode);
      System.exit(-1);
    }
  }
}