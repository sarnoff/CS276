package cs276.pe1.lucene;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

@SuppressWarnings("deprecation")
public class IMDBSearcher {
//	public static final int MAX_RESULTS = Integer.MAX_VALUE;
	public static final int MAX_RESULTS = 20;
	
	public static void main(String[] args) throws Exception {
		Directory index = FSDirectory.getDirectory(new File(new File(System.getProperty("user.home")),"cs276-index"));
		IndexSearcher searcher = new IndexSearcher(index);
		Analyzer analyzer = new StandardAnalyzer();
		
		search211(searcher, analyzer);
		search212(searcher, analyzer);	
		search213(searcher, analyzer);
		search22(searcher, analyzer);
		
		index.close();
		searcher.close();
	}
	
	public static void search211(IndexSearcher searcher, Analyzer analyzer) throws Exception {
		QueryParser parser = new QueryParser("author",analyzer);
		Query query = parser.parse("Rob");
		Hits hits = searcher.search(query);
		System.out.println("Results for 2.1.1: "+hits.length());
		int num = Math.min(MAX_RESULTS, hits.length());
		for(int i = 0; i < num; i++) {
			Document hit = hits.doc(i);
			System.out.println("  "+hit.get("title"));
			System.out.println("      "+hit.get("author"));
		}
	}
	
	public static void search212(IndexSearcher searcher, Analyzer analyzer) throws Exception {
		QueryParser parser = new QueryParser("plot", analyzer);
		Query query = parser.parse("\"murdered eighteen\"~10");
		Hits hits = searcher.search(query);
		System.out.println();
		System.out.println("Results for 2.1.2: "+hits.length());
		int num = Math.min(MAX_RESULTS, hits.length());
		for(int i = 0; i < num; i++) {
			Document hit = hits.doc(i);
			System.out.println("  "+hit.get("title"));
			System.out.println("      "+hit.get("plot"));
		}
	}
	
	public static void search213(IndexSearcher searcher, Analyzer analyzer) throws Exception {
		QueryParser parser = new QueryParser("title", analyzer);
		Query query = parser.parse("10 items or less \\(2006\\)");
		Hits hits = searcher.search(query);
		System.out.println();
		System.out.println("Results for 2.1.3: "+hits.length());
		int num = Math.min(MAX_RESULTS, hits.length());
		for(int i = 0; i < num; i++) {
			Document hit = hits.doc(i);
			System.out.println("  "+hit.get("title"));
		}
		System.out.println();
	}
	
	public static void search22(IndexSearcher searcher, Analyzer analyzer) throws Exception {
		QueryParser parser = new QueryParser("author", analyzer);
		Query query = parser.parse("(Hart*)^0.01 OR (Rob* AND Hart*)^50");
		Hits hits = searcher.search(query);
		System.out.println();
		System.out.println("Results for 2.2: "+hits.length());
		int num = Math.min(MAX_RESULTS, hits.length());
		for(int i = 0; i < num; i++) {
			Document hit = hits.doc(i);
			System.out.println("  "+hit.get("title"));
			System.out.println("        "+hit.get("author"));
		}
		
	}
}
