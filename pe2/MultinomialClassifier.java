import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class MultinomialClassifier {
	private HashMap<Integer, ClassStatistics> classStatistics = new HashMap<Integer, ClassStatistics>();
	private int numDocs = 0;
	private int numWords = 0;
	public static double ALPHA = 0.5;
	
	public MultinomialClassifier(ArrayList<MessageFeatures>[] messageList) {
		trainClassifier(messageList);
	}
	
	private void trainClassifier(ArrayList<MessageFeatures>[] messageList) {
		for(int i = 0; i < messageList.length; i++) {
			ArrayList<MessageFeatures> list = messageList[i];
			for(MessageFeatures mf : list) {
				trainFeature(mf);
			}
		}
	}
	
	private void trainFeature(MessageFeatures mf) {
		int klass = mf.newsgroupNumber;
		
		//Get the statistics holder for the message's class
		ClassStatistics stats = new ClassStatistics();
		if(classStatistics.containsKey(klass)) stats = classStatistics.get(klass);
		
		//Find all words in the message
		HashSet<String> words = new HashSet<String>();
		words.addAll(mf.body.keySet());
		words.addAll(mf.subject.keySet());
		
		//Update stats with the new information
		stats.addDoc();
		stats.addNumWords(words.size());
		for(String word : words) {
			double num = mf.body.getCount(word) + mf.subject.getCount(word);
			for(int i = 0; i < num; i++) { stats.addWord(word); }
		}
		
		//Update instance variables
		numDocs++;
		numWords += words.size();
		classStatistics.put(klass, stats);
	}
	
	public double[] classifyCNBFeature(MessageFeatures mf) {
		int numClasses = classStatistics.size();
		double[] scores = new double[numClasses];
		for(int klass = 0; klass < numClasses; klass++) {
			scores[klass] = scoreCNBFeature(klass, mf);
		}
		return scores;
	}
	
	private double scoreCNBFeature(int klass, MessageFeatures mf) {
		HashSet<String> words = new HashSet<String>();
		words.addAll(mf.body.keySet());
		words.addAll(mf.subject.keySet());
		
		double score = 0;
		for(String word : words) {
			double count = mf.body.getCount(word) + mf.subject.getCount(word);
			for(int i = 0; i < count; i++) {
				score += scoreCNBWord(klass, word);
			}
		}
		return score;
	}
	
	private double scoreCNBWord(int klass, String word) {
		double numOccurrencesWordInDocsOfOtherClass = 0;
		double numOfWordsInDocsOfOtherClass = 0;
		
		for(Integer key : classStatistics.keySet()) {
			if(key == klass) continue;
			ClassStatistics stats = classStatistics.get(key);
			numOccurrencesWordInDocsOfOtherClass += stats.getCount(word);
			numOfWordsInDocsOfOtherClass += stats.numWords;
		}
		
		double probabilityOfWordInClassNumerator = numOccurrencesWordInDocsOfOtherClass + ALPHA;
		double probabilityOfWordInClassDenominator = numOfWordsInDocsOfOtherClass + this.numWords * ALPHA;
		double probabilityOfWordInClass = probabilityOfWordInClassNumerator / probabilityOfWordInClassDenominator;
		
		return -1 * Math.log(probabilityOfWordInClass);
	}
	
	public double[] classifyFeature(MessageFeatures mf) {
		int numClasses = classStatistics.size();
		double[] scores = new double[numClasses];
		for(int klass = 0; klass < numClasses; klass++) {
			scores[klass] = scoreFeature(klass, mf);
		}
		return scores;
	}
	
	private double scoreFeature(int klass, MessageFeatures mf) {
		HashSet<String> words = new HashSet<String>();
		words.addAll(mf.body.keySet());
		words.addAll(mf.subject.keySet());
		
		double score = 0;
		for(String word : words) {
			double count = mf.body.getCount(word) + mf.subject.getCount(word);
			for(int i = 0; i < count; i++) {
				score += scoreWord(klass, word);
			}
		}
		return score;
	}
	
	private double scoreWord(int klass, String word) {
		ClassStatistics stats = classStatistics.get(klass);
	
		double numOccurrencesWordInDocsOfSameClass = stats.getCount(word);
		double numOfWordsInDocsOfSameClass = stats.numWords;
		double probabilityOfWordInClassNumerator = numOccurrencesWordInDocsOfSameClass + ALPHA;
		double probabilityOfWordInClassDenominator = numOfWordsInDocsOfSameClass + this.numWords * ALPHA;
		double probabilityOfWordInClass = probabilityOfWordInClassNumerator / probabilityOfWordInClassDenominator;
		
		return Math.log(probabilityOfWordInClass);
	}
}
