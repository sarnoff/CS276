package cs276.pe1.spell;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import cs276.util.IOUtils;
import cs276.util.StringUtils;
import cs276.util.Counter;

public class KGramSpellingCorrector implements SpellingCorrector {
    private static int k = 2; //start with bigrams, then extend out
	/** Initializes spelling corrector by indexing kgrams in words from a file */
    Map<String,Set<String>> kgram;
	public KGramSpellingCorrector() {
        //File path = new File("/afs/ir/class/cs276/pe1-2011/big.txt.gz");
        File path = new File("datasources/big.txt.gz");
        
        
        kgram = new HashMap<String,Set<String>>();
        for (String line : IOUtils.readLines(IOUtils.openFile(path))) {
            for (String word : StringUtils.tokenize(line)) {
            	String key = "$"+word+"$";//$ to signal beginning/end of word
                if(key.length() <= k)
                    addWord(key,word);
                for(int i = 0;i<(key.length()-k+1);i++)
                    addWord(key.substring(i,i+k),word);
            }
        }
	}
    
    /*
     * Checks key in kgram.  If it exists, add value to the set.
     * Otherwise it creates a new set and add its value to it.
     * Set prevents duplicate values
     */
    private void addWord(String key, String value)
    {
        Set<String> set;
        if(!kgram.containsKey(key))
        {
            set = new HashSet<String>();
        }
        else
        {
            set = kgram.get(key);
        }
        set.add(value);
        kgram.put(key,set);
    }

	public List<String> corrections(String word) {
		Counter<String> wordCounts = new Counter<String>();
        String key = "$"+word+"$";//$ to signal beginning/end of word
        Set<String> set;
        if(key.length() <= k)
        {
            if(kgram.containsKey(key))
            {
                set = kgram.get(key);
                for(String w : set)
                    wordCounts.incrementCount(w);
            }
        }
        for(int i = 0;i<(key.length()-k+1);i++)
        {
            if(kgram.containsKey(key.substring(i,i+k)))
            {
                set = kgram.get(key.substring(i,i+k));
                for(String w : set)
                    wordCounts.incrementCount(w);
            }
        }
        //System.out.println(word+" "+wordCounts.topK(10));
		return wordCounts.topK(10);//assignment said start with 10 output values
	}
}
