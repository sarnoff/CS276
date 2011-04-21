package cs276.pe1.lucene;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;

@SuppressWarnings("deprecation")
public class IMDBSpelling {
	public static final String words = "\\\\afs\\ir\\class\\cs276\\pe1-2011\\spelltest.txt";
	
	public static void main(String[] args) throws Exception {
		Directory index = FSDirectory.getDirectory(new File(new File(System.getProperty("user.home")),"cs276-index"));
		IndexSearcher searcher = new IndexSearcher(index);
		Analyzer analyzer = new StandardAnalyzer();
		
		Directory spellIndex = FSDirectory.getDirectory(new File(new File(System.getProperty("user.home")),"cs276-spell"));
		SpellChecker spell = new SpellChecker(spellIndex);
		IndexReader reader = IndexReader.open(index);
		Dictionary dictionary = new LuceneDictionary(reader, "title");
		spell.indexDictionary(dictionary);
		
		String[] suggest = spell.suggestSimilar("Trmmy", 10);
		for(String s : suggest) System.out.println(s);
		System.out.println("done");
		
//		QueryParser parser = new QueryParser("title",analyzer);
//		Query query = parser.parse("Trmmy");
//		Hits hits = searcher.search(query);
//		System.out.println("Results for Timmy: "+hits.length());
//		int num = Math.min(Integer.MAX_VALUE, hits.length());
//		for(int i = 0; i < num; i++) {
//			Document hit = hits.doc(i);
//			System.out.println("  "+hit.get("title"));
//		}
		index.close();
		searcher.close();
	}
}
