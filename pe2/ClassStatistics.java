import java.util.HashMap;
import java.util.HashSet;

public class ClassStatistics {
	public int numDocs = 0;
	public int numWords = 0;
	private HashMap<String, Integer> wordCounts = new HashMap<String, Integer>();
	
	public void addDoc() {
		this.numDocs++;
	}
	
	public void addNumWords(int num) {
		this.numWords += num;
	}
	
	public void addWords(HashSet<String> words) {
		for(String word : words)
			addWord(word);
	}
	
	public int getCount(String word) {
		if(!wordCounts.containsKey(word))
			return 0;
		return wordCounts.get(word);
	}
	
	private void addWord(String word) {
		int count = 0;
		if(wordCounts.containsKey(word))
			count = wordCounts.get(word);
		wordCounts.put(word, count + 1);
	}
}
