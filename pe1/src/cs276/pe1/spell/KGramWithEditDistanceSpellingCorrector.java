package cs276.pe1.spell;

import java.util.List;
import java.util.Collections;

import cs276.util.StringUtils;
import cs276.util.Counter;

public class KGramWithEditDistanceSpellingCorrector extends KGramSpellingCorrector {
	public KGramWithEditDistanceSpellingCorrector() {
        super();
	}

	public List<String> corrections(String word) {
		List<String> firstPass = super.corrections(word);
        //use a counter to keep track of distance
        Counter<String> editDistance = new Counter<String>();
        for(String s:firstPass)
            editDistance.incrementCount(s,StringUtils.levenshtein(word,s));
        
        List<String> distances = editDistance.topK(WL);
        //reverse since topK is sorted by largest edit distance
        Collections.reverse(distances);
        return distances;
	}
}
