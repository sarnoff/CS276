package cs276.pe1.lucene;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Version;

import cs276.pe1.lucene.IMDBParser.MoviePlotRecord;

public class IMDBIndexer {	
	public static void main(String[] argv) throws Exception {
		File indexPath = new File(new File(System.getProperty("user.home")),"cs276-index");
		indexPath.mkdir();
		
		@SuppressWarnings("deprecation")
		IndexWriter writer = new IndexWriter(indexPath, new StandardAnalyzer(Version.LUCENE_CURRENT), true, IndexWriter.MaxFieldLength.LIMITED);

		for (MoviePlotRecord rec : IMDBParser.readRecords()) {
			Document doc = new Document();
			doc.add(new Field("author", rec.authors.trim(), Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("title", rec.title.trim(), Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("plot", rec.plots.trim(), Field.Store.YES, Field.Index.ANALYZED));
			writer.addDocument(doc);
		}
		
		writer.optimize();		
		writer.close();
		System.err.println("Done");
	}
}
