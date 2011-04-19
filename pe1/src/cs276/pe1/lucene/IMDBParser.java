package cs276.pe1.lucene;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cs276.util.IOUtils;

/**
 * Utility class to parse records out of the IMDB plot summaries list.
 *  
 * @author dramage
 */
public class IMDBParser {
	
	/** A record that contains movie plot information read from the IMDB plots file */
	public static class MoviePlotRecord {
		/** Title of the move */
		public final String title;
		
		/** A concatenation of plot synopses */
		public final String plots;
		
		/** A concatenation of authors of the plot synopses */
		public final String authors;
		
		public MoviePlotRecord(String title, String plots, String authors) {
			this.title  = title;
			this.plots  = plots;
			this.authors = authors;
		}
		
		@Override
		public String toString() {
			return "---\n" +
					"MV: "+title+"\n"+
					"PL: "+plots+"\n"+
					"By: "+authors;
		}
	}
	
	/** Helper class for reading movie records from a file */
	private static class MovieReader implements Iterable<MoviePlotRecord> {
		public final Iterable<String> lineReader;
		
		public MovieReader(Iterable<String> lineReader) {
			this.lineReader = lineReader;
		}

		public Iterator<MoviePlotRecord> iterator() {
			return new Iterator<MoviePlotRecord>() {
				Iterator<String> lines = lineReader.iterator();
				List<String> record = firstRecord();
				
				/** Skip header and return the first real record */
				private List<String> firstRecord() {
					while (lines.hasNext()) {
						if (lines.next().startsWith("===")) {
							break;
						}
					}
					return nextRecord();
				}
				
				/** Get all lines before the next separator "---" */
				private List<String> nextRecord() {
					List<String> record = new LinkedList<String>();
					while (lines.hasNext()) {
						String line = lines.next();
						if (line.startsWith("---")) {
							break;
						}
						record.add(line);
					}
					
					return record;
				}
				
				/** Return true if we have a record waiting to be parsed */
				public boolean hasNext() {
					return !record.isEmpty();
				}

				/** Parse and return the waiting record, pre-reading more rows as necessary */
				public MoviePlotRecord next() {
					String title = null;
					StringBuilder plots = new StringBuilder();
					StringBuilder authors = new StringBuilder();
					
					for (String line : record) {
						if (line.startsWith("MV: ")) {
							if (title != null) {
								throw new RuntimeException("IMDB parse error: two plot titles");
							}
							title = line.substring(4);
						} else if (line.startsWith("PL: ")) {
							plots.append(line.substring(4)).append(" ");
						} else if (line.startsWith("BY: ")) {
							authors.append(line.substring(4)).append(" ");
						}
					}
					
					// read next record
					record = nextRecord();
					
					return new MoviePlotRecord(title, plots.toString(), authors.toString());
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	}
	
	/**
	 * Returns an Iterable of MoviePlotRecords as parsed from the given IMDB movie file.
	 */
	public static Iterable<MoviePlotRecord> readRecords(File file) {
		return new MovieReader(IOUtils.readLines(IOUtils.openFile(file)));
	}
	
	/**
	 * Returns an Iterable of MoviePlotRecords as parsed from the IMDB movie file stored in AFS.
	 */
	public static Iterable<MoviePlotRecord> readRecords() {
		return readRecords(imdbPath);
	}
	
	/** Default path to imdb plot records */
//	public static final File imdbPath = new File("/afs/ir/class/cs276/pe1-2009/imdb-plots-20081003.list.gz");
	public static final File imdbPath = new File("C:/Users/Amanda/Desktop/Everything/Spring Quarter/CS276/pe1/datasources/imdb-plots-20081003.list.gz");
	
	/**
	 * Sample main method prints records from the plots file.
	 */
	public static void main(String[] argv) {
		for (MoviePlotRecord rec : readRecords(imdbPath)) {
			System.out.println(rec.title + " " + rec.plots);
		}
	}

}
