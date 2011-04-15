package cs276.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

/**
 * IO utilities to help work with Java's extremely verbose yet
 * surprisingly non-useful IO builtins.  These methods are
 * excerpted from ResearchAssistant.
 * 
 * @author dramage
 */
public class IOUtils {
	
	/**
	 * Quietly opens a File.  If the file ends with a ".gz" extension,
	 * automatically opens a GZIPInputStream to wrap the constructed
	 * FileInputStream.
	 */
	public static InputStream openFile(File file) throws QuietIOException {
		try {
			InputStream is = new FileInputStream(file);
			if (file.getName().endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
			return is;
		} catch (Exception e) {
			throw new QuietIOException(e);
		}
	}
	
	/**
	 * Returns a single-use Iterable over lines in the given stream.
	 */
	public static Iterable<String> readLines(InputStream stream) throws QuietIOException {
		return new OnePassIterable<String>(
				new LineReaderIterator(new BufferedReader(new InputStreamReader(stream))));
	}
	
	/**
	 * Iterates through lines in a Reader.
	 */
	private static class LineReaderIterator implements Iterator<String> {

		private BufferedReader reader;
		private String next;
		
		public LineReaderIterator(BufferedReader reader) {
			this.reader = reader;
			next();
		}

		public boolean hasNext() {
			return next != null;
		}

		public String next() {
			String current = next;
			try {
				next = reader.readLine();
				if (next == null) {
					reader.close();
				}
			} catch (IOException e) {
				throw new QuietIOException(e);
			}
			return current;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	/**
	 * One-pass iterable returning a single iterator only once.
	 */
	private static class OnePassIterable<E> implements Iterable<E> {
		private final Iterator<E> iterator;
		private boolean requested = false;

		public OnePassIterable(Iterator<E> iterator) {
			this.iterator = iterator;
		}
		
		public synchronized Iterator<E> iterator() {
			if (requested) {
				throw new QuietIOException("Attempt to read a one-pass Iterable more than once");
			}
			requested = true;
			return iterator;
		}
	}
	
	/**
	 * Runtime wrapper of IOException.
	 * 
	 * @author dramage
	 */
	public static class QuietIOException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		public QuietIOException(String msg, Exception cause) {
			super(msg, cause);
		}
		
		public QuietIOException(String msg) {
			super(msg);
		}
		
		public QuietIOException(Exception e) {
			super(e);
		}
	}

}
