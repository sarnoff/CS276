package cs276.pe1.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

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

import cs276.pe1.spell.KGramSpellingCorrector;
import cs276.pe1.spell.KGramWithEditDistanceSpellingCorrector;

@SuppressWarnings("deprecation")
public class IMDBSpelling {	
	public static void main(String[] args) throws Exception {
		//Read search query
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		String search = reader.readLine();
		
		//Spelling correct query
		KGramWithEditDistanceSpellingCorrector k = new KGramWithEditDistanceSpellingCorrector();
		List<String> suggest = k.corrections(search);
		for(String s : suggest) search += " " + s;
		
		//Run Lucene search
		Directory index = FSDirectory.getDirectory(new File(new File(System.getProperty("user.home")),"cs276-index"));
		IndexSearcher searcher = new IndexSearcher(index);
		Analyzer analyzer = new StandardAnalyzer();		
		QueryParser parser = new QueryParser("title",analyzer);
		Query query = parser.parse(search);
		Hits hits = searcher.search(query);
		
		//Display results
		System.out.println("Results: "+hits.length());
		int num = Math.min(20, hits.length());
		for(int i = 0; i < num; i++) {
			Document hit = hits.doc(i);
			System.out.println("  "+hit.get("title"));
		}
		index.close();
		searcher.close();
	}
}
