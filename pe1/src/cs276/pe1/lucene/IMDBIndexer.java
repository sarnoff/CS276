package cs276.pe1.lucene;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Version;

import cs276.pe1.lucene.IMDBParser.MoviePlotRecord;

public class IMDBIndexer {
	
	public static void main(String[] argv) throws Exception {
		// where to store the index: you can choose your own location,
		// but be sure it is not in your pe1 folder (it should not be
		// submitted along with your code).  the lines below create
		// a new folder cs276-index in your home directory.
		File indexPath = new File(new File(System.getProperty("user.home")),"cs276-index");
		indexPath.mkdir();
		
		@SuppressWarnings("deprecation")
		IndexWriter writer = new IndexWriter(indexPath, new StandardAnalyzer(Version.LUCENE_CURRENT), true, IndexWriter.MaxFieldLength.LIMITED);

		for (MoviePlotRecord rec : IMDBParser.readRecords()) {
			// TODO do indexing here
			// See IMDBParser.MoviePlotRecord for info on its fields
			// Be sure to add all the fields to the index.
		}
		
		writer.close();
		System.err.println("Done");
	}
}
