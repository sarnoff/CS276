package cs276.pe1.spell;

import java.util.List;
import java.util.Set;

import cs276.util.StringUtils;
import cs276.util.Counter;
import cs276.util.Counters;

public class KGramWithEditDistanceSpellingCorrector extends KGramSpellingCorrector {
	public KGramWithEditDistanceSpellingCorrector() {
        super();
	}

    protected Counter<String> editDistance(String word, List<String> potentialWords)
    {
        Counter<String> editDistance = new Counter<String>();
        for(String s:potentialWords)
            editDistance.incrementCount(s,StringUtils.levenshtein(word,s));
        return editDistance;
    }
    
    protected void reciprocal(Counter<String> target) {
		for (String key : target.keySet()) {
			target.setCount(key, 1.0/((double)target.getCount(key)));
		}
	}
    
	public List<String> corrections(String word) {
		Counter<String> scoredCounter = jaccardScore(word);
        List<String> firstPass = scoredCounter.topK(WL);
        
        Counter<String>editDistance = editDistance(word,firstPass);
        
        
        Counters.retainTop(scoredCounter,WL);
        Counters.normalize(scoredCounter);
        Counters.normalize(editDistance);
        
        reciprocal(editDistance);
        Counters.multiplyInPlace(scoredCounter,editDistance);
        
        return scoredCounter.topK(WL);
	}
}
