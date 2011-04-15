package cs276.pe1.spell;

import java.io.File;
import java.util.List;

import cs276.util.IOUtils;
import cs276.util.StringUtils;

public class KGramSpellingCorrector implements SpellingCorrector {
	/** Initializes spelling corrector by indexing kgrams in words from a file */
	public KGramSpellingCorrector() {
        File path = new File("/afs/ir/class/cs276/pe1-2011/big.txt.gz");
        for (String line : IOUtils.readLines(IOUtils.openFile(path))) {
            for (String word : StringUtils.tokenize(line)) {
            	// TODO do kgram indexing
            }
        }
	}

	public List<String> corrections(String word) {
		// TODO fill in method here
		return null;
	}
}
