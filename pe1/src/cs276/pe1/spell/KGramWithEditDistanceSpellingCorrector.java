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
        
        
        Counters.retainTop(scoredCounter,10);
        Counters.normalize(scoredCounter);
        Counters.normalize(editDistance);
        
        reciprocal(editDistance);
        Counters.multiplyInPlace(scoredCounter,editDistance);
        
        return scoredCounter.topK(10);
        
        /*
         Counters.retainTop(scoredCounter,WL);
        reciprocal(scoredCounter);
        Counters.divideInPlace(scoredCounter,editDistance);
        
        
        if(!scoredCounter.topK(WL).get(0).equals(firstPass.get(0)))
            System.out.println(word+"=>"+scoredCounter.topK(WL).get(0)+"\n"+scoredCounter+"\n\n");
        
        return scoredCounter.topK(WL);
         */
        
        
        /*
        double min = Counters.min(editDistance);
        Set<String> minEdit = Counters.keysAt(editDistance,min);
        
        //if edit distance and jaccard agree
        if(minEdit.contains(firstPass.get(0)))
           return firstPass;
        
        //otherwise print out so I can look:
        System.out.println(word+"\njaccard:"+firstPass+"\neditD:"+minEdit+"\n");
           
        return firstPass;
         */
	}
}
