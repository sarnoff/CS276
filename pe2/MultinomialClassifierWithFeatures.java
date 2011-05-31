import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import cs224n.util.*;

@SuppressWarnings("unchecked")
public class MultinomialClassifierWithFeatures extends MultinomialClassifier {
	
    ArrayList<String>[] featureSet;
    
	public MultinomialClassifierWithFeatures(ArrayList<MessageFeatures>[] messageList) {
		super();
        createFeatureSet(messageList);
        trainClassifier(messageList);
	}
    
    public void createFeatureSet(ArrayList<MessageFeatures>[] messageList)
    {
        Counter<String>[] counters = (Counter<String>[])new Counter[messageList.length];
        for(int i = 0; i < messageList.length;i++)
        {
            Counter<String> categoryCounter = new Counter<String>();
            for(MessageFeatures mf:messageList[i])
            {
                for(String s:mf.subject.keySet())
                    categoryCounter.incrementCount(s,mf.subject.getCount(s));
                    
                for(String s:mf.body.keySet())
                    categoryCounter.incrementCount(s,mf.body.getCount(s));
                
            }
            counters[i] = categoryCounter;
        }
        
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
        
        featureSet = (ArrayList<String>[])new ArrayList[features.length];
        for(int i = 0;i<features.length;i++)
        {
            PriorityQueue<String> pq = features[i].asPriorityQueue();
            featureSet[i] = new ArrayList<String>();
            for(int j = 0; j < NaiveBayesClassifier.FEATURES_PER_NEWSGROUP&&pq.hasNext();j++)
                featureSet[i].add(pq.next());
        }
    }
	
    protected void trainFeature(MessageFeatures mf) {
		int klass = mf.newsgroupNumber;
		
		//Get the statistics holder for the message's class
		ClassStatistics stats = new ClassStatistics();
		if(classStatistics.containsKey(klass)) stats = classStatistics.get(klass);
		
		//Find all words in the message
		HashSet<String> words = new HashSet<String>();
		words.addAll(mf.body.keySet());
		words.addAll(mf.subject.keySet());
		vocabulary.addAll(words);
		
		//Update stats with the new information
		stats.addDoc();
		for(String word : words) {
            if(featureSet[klass].contains(word))
            {
                double num = mf.body.getCount(word) + mf.subject.getCount(word);
                numWords += num;
                for(int i = 0; i < num; i++) { stats.addWord(word); }
            }
		}
		
		//Update instance variables
		numDocs++;
		classStatistics.put(klass, stats);
	}
}
