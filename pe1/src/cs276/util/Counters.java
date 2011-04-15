//Stanford JavaNLP support classes
//Copyright (c) 2001-2008 The Board of Trustees of
//The Leland Stanford Junior University. All Rights Reserved.
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//For more information, bug reports, fixes, contact:
// Christopher Manning
// Dept of Computer Science, Gates 1A
// Stanford CA 94305-9010
// USA
// java-nlp-support@lists.stanford.edu
// http://nlp.stanford.edu/software/
package cs276.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Static methods for operating on {@link Counter}s.
 * <p>
 * All methods that change their arguments change the <i>first</i> argument
 * (only), and have "InPlace" in their name.
 * This class also provides access
 * to Comparators that can be used to sort the keys or entries of this Counter
 * by the counts, in either ascending or descending order.
 * 
 * This class is excerpted from JavaNLP.
 *
 * @author Galen Andrew (galand@cs.stanford.edu)
 * @author Jeff Michels (jmichels@stanford.edu)
 * @author dramage
 * @author cer
 * @author Christopher Manning
 */
public class Counters {

	private Counters() {} // only static methods

	//
	// Construction operations
	//
	
	public static <E> Counter<E> count(Iterable<E> elements) {
		Counter<E> counter = new Counter<E>();
		for (E element : elements) {
			counter.incrementCount(element);
		}
		return counter;
	}


	//
	// Query operations
	//


	/**
	 * Returns the value of the maximum entry in this counter.
	 * This is also the Linfinity norm.
	 * An empty counter is given a max value of Double.NEGATIVE_INFINITY.
	 *
	 * @param c The Counter to find the max of
	 * @return The maximum value of the Counter
	 */
	public static <E> double max(Counter<E> c) {
		double max = Double.NEGATIVE_INFINITY;
		for (double v : c.values()) {
			max = Math.max(max, v);
		}
		return max;
	}

	/**
	 * Takes in a Collection of something and makes a counter, incrementing once
	 * for each object in the collection.
	 *
	 * @param c The Collection to turn into a counter
	 * @return The counter made out of the collection
	 */
	public static <E> Counter<E> asCounter(Collection<E> c) {
		Counter<E> count = new Counter<E>();
		for(E elem: c){
			count.incrementCount(elem);
		}
		return count;
	}


	/**
	 * Returns the value of the smallest entry in this counter.
	 *
	 * @param c The Counter (not modified)
	 * @return The minimum value in the Counter
	 */
	public static <E> double min(Counter<E> c) {
		double min = Double.POSITIVE_INFINITY;
		for (double v : c.values()) {
			min = Math.min(min, v);
		}
		return min;
	}

	/**
	 * Finds and returns the key in the Counter with the largest count.
	 * Returning null if count is empty.
	 *
	 * @param c The Counter
	 * @return The key in the Counter with the largest count.
	 */
	public static <E> E argmax(Counter<E> c) {
		double max = Double.NEGATIVE_INFINITY;
		E argmax = null;
		for (E key : c.keySet()) {
			double count = c.getCount(key);
			if (argmax == null || count > max) {// || (count == max && tieBreaker.compare(key, argmax) < 0)) {
				max = count;
				argmax = key;
			}
		}
		return argmax;
	}

	/**
	 * Finds and returns the key in this Counter with the smallest count.
	 *
	 * @param c The Counter
	 * @return The key in the Counter with the smallest count.
	 */
	public static <E> E argmin(Counter<E> c) {
		double min = Double.POSITIVE_INFINITY;
		E argmin = null;

		for (E key : c.keySet()) {
			double count = c.getCount(key);
			if (argmin == null || count < min) {// || (count == min && tieBreaker.compare(key, argmin) < 0)) {
				min = count;
				argmin = key;
			}
		}
		return argmin;
	}


	// TODO: Reinstate versions of argmax and argmin with stable tie-breaking.

	/**
	 * Returns the mean of all the counts (totalCount/size).
	 *
	 * @param c The Counter to find the mean of.
	 * @return The mean of all the counts (totalCount/size).
	 */
	public static <E> double mean(Counter<E> c) {
		return c.totalCount() / c.size();
	}

	//
	// In-place arithmetic
	//

	/**
	 * Sets each value of target to be target[k]+scale*arg[k] for
	 * all keys k in target.
	 *
	 * @param target A Counter that is modified
	 * @param arg The Counter whose contents are added to target
	 * @param scale How the arg Counter is scaled before being added
	 */
	// TODO: Rewrite to use arg.entrySet()
	public static <E> void addInPlace(Counter<E> target, Counter<E> arg, double scale) {
		for (E key : arg.keySet()) {
			target.incrementCount(key, scale * arg.getCount(key));
		}
	}

	/**
	 * Sets each value of target to be target[k]+arg[k] for all keys k in target.
	 */
	public static <E> void addInPlace(Counter<E> target, Counter<E> arg) {
		for (E key : arg.keySet()) {
			target.incrementCount(key, arg.getCount(key));
		}
	}

	/**
	 * Sets each value of target to be target[k]-arg[k] for all keys k in target.
	 */
	public static <E> void subtractInPlace(Counter<E> target, Counter<E> arg) {
		for (E key : arg.keySet()) {
			target.decrementCount(key, arg.getCount(key));
		}
	}

	/**
	 * Divides every non-zero count in target by the corresponding value in
	 * the denominator Counter.  Beware that this can give NaN values for zero
	 * counts in the denominator counter!
	 */
	public static <E> void divideInPlace(Counter<E> target, Counter<E> denominator) {
		for (E key : target.keySet()) {
			target.setCount(key, target.getCount(key) / denominator.getCount(key));
		}
	}

	/**
	 * Multiplies every count in target by the corresponding value in
	 * the term Counter.
	 */
	public static <E> void multiplyInPlace(Counter<E> target, Counter<E> term) {
		for (E key : target.keySet()) {
			target.setCount(key, target.getCount(key) * term.getCount(key));
		}
	}

	/**
	 * Divides each value in target by the given divisor, in place.
	 *
	 * @param target The values in this Counter will be changed throught by
	 *     the multiplier
	 * @param divisor The number by which to change each number in the
	 *     Counter
	 * @return The target Counter is returned (for easier method chaining)
	 */
	public static <E> Counter<E> divideInPlace(Counter<E> target, double divisor) {
		for (Entry<E, Double> entry : target.entrySet()) {
			target.setCount(entry.getKey(), entry.getValue() / divisor);
		}
		return target;
	}


	/**
	 * Multiplies each value in target by the given multiplier, in place.
	 *
	 * @param target The values in this Counter will be changed throught by
	 *     the multiplier
	 * @param multiplier The number by which to change each number in the
	 *     Counter
	 */
	public static <E> Counter<E> multiplyInPlace(Counter<E> target, double multiplier) {
		for (Entry<E, Double> entry : target.entrySet()) {
			target.setCount(entry.getKey(), entry.getValue() * multiplier);
		}
		return target;
	}


	/**
	 * Normalizes the target counter in-place, so the sum of the
	 * resulting values equals 1.
	 * @param <E>
	 * @param target
	 */
	public static <E> void normalize(Counter<E> target) {
		multiplyInPlace(target, 1.0 / target.totalCount());
	}

	public static <E> void logInPlace(Counter<E> target) {
		for(E key : target.keySet()) {
			target.setCount(key,Math.log(target.getCount(key)));
		}
	}

	//
	// Selection Operators
	//

	/**
	 * Removes all entries from c except for the top <code>num</code>
	 */
	public static <E> void retainTop(Counter<E> c, int num) {
		int numToPurge = c.size()-num;
		if (numToPurge <=0) {
			return;
		}

		List<E> l = Counters.toSortedList(c);
		Collections.reverse(l);
		for (int i=0; i<numToPurge; i++) {
			c.remove(l.get(i));
		}
	}

	/**
	 * Removes all entries with 0 count in the counter, returning the
	 * set of removed entries.
	 */
	public static <E> Set<E> retainNonZeros(Counter<E> counter) {
		Set<E> removed = new HashSet<E>();
		for (E key : counter.keySet()) {
			if (counter.getCount(key) == 0.0) {
				removed.add(key);
			}
		}
		for (E key : removed) {
			counter.remove(key);
		}
		return removed;
	}

	/**
	 * Returns the set of keys whose counts are at or above the given threshold.
	 * This set may have 0 elements but will not be null.
	 *
	 * @param c The Counter to examine
	 * @param countThreshold Items equal to or above this number are kept
	 * @return A (non-null) Set of keys whose counts are at or above the given
	 *     threshold.
	 */
	public static <E> Set<E> keysAbove(Counter<E> c, double countThreshold) {
		Set<E> keys = new HashSet<E>();
		for (E key : c.keySet()) {
			if (c.getCount(key) >= countThreshold) {
				keys.add(key);
			}
		}
		return (keys);
	}

	/**
	 * Returns the set of keys whose counts are at or below the given threshold.
	 * This set may have 0 elements but will not be null.
	 */
	public static <E> Set<E> keysBelow(Counter<E> c, double countThreshold) {
		Set<E> keys = new HashSet<E>();
		for (E key : c.keySet()) {
			if (c.getCount(key) <= countThreshold) {
				keys.add(key);
			}
		}
		return (keys);
	}

	/**
	 * Returns the set of keys that have exactly the given count.
	 * This set may have 0 elements but will not be null.
	 */
	public static <E> Set<E> keysAt(Counter<E> c, double count) {
		Set<E> keys = new HashSet<E>();
		for (E key : c.keySet()) {
			if (c.getCount(key) == count) {
				keys.add(key);
			}
		}
		return (keys);
	}


	//
	// Conversion to other types
	//

	/**
	 * Returns a comparator backed by this counter: two objects are compared
	 * by their associated values stored in the counter.
	 * This comparator returns keys by ascending numeric value.
	 * Note that this ordering
	 * is not fixed, but depends on the mutable values stored in the Counter.
	 * Doing this comparison does not depend on the type of the key, since it
	 * uses the numeric value, which is always Comparable.
	 *
	 * @param counter The Counter whose values are used for ordering the keys
	 * @return A Comparator using this ordering
	 */
	public static <E> Comparator<E> toComparator(final Counter<E> counter) {
		return new Comparator<E>() {
			public int compare(E o1, E o2) {
				return Double.compare(counter.getCount(o1), counter.getCount(o2));
			}
		};
	}

	/**
	 * Returns a comparator backed by this counter: two objects are compared
	 * by their associated values stored in the counter.
	 * This comparator returns keys by descending numeric value.
	 * Note that this ordering
	 * is not fixed, but depends on the mutable values stored in the Counter.
	 * Doing this comparison does not depend on the type of the key, since it
	 * uses the numeric value, which is always Comparable.
	 *
	 * @param counter The Counter whose values are used for ordering the keys
	 * @return A Comparator using this ordering
	 */
	public static <E> Comparator<E> toComparatorDescending(final Counter<E> counter) {
		return new Comparator<E>() {
			public int compare(E o1, E o2) {
				return Double.compare(counter.getCount(o2), counter.getCount(o1));
			}
		};
	}

	/**
	 * Returns a comparator suitable for sorting this Counter's keys or entries
	 * by their respective value or magnitude (by absolute value).
	 * If <tt>ascending</tt> is true, smaller magnitudes will
	 * be returned first, otherwise higher magnitudes will be returned first.
	 * <p/>
	 * Sample usage:
	 * <pre>
	 * Counter c = new Counter();
	 * // add to the counter...
	 * List biggestKeys = new ArrayList(c.keySet());
	 * Collections.sort(biggestAbsKeys, Counters.comparator(c, false, true));
	 * List smallestEntries = new ArrayList(c.entrySet());
	 * Collections.sort(smallestEntries, Counters.comparator(c, true, false));
	 * </pre>
	 */
	public static <E> Comparator<E> toComparator(final Counter<E> counter,
			final boolean ascending,
			final boolean useMagnitude) {
		return new Comparator<E>() {
			public int compare(E o1, E o2) {
				if (ascending) {
					if (useMagnitude) {
						return Double.compare(Math.abs(counter.getCount(o1)), Math.abs(counter.getCount(o2)));
					} else {
						return Double.compare(counter.getCount(o1), counter.getCount(o2));
					}
				} else {
					// Descending
					if (useMagnitude) {
						return Double.compare(Math.abs(counter.getCount(o2)), Math.abs(counter.getCount(o1)));
					} else {
						return Double.compare(counter.getCount(o2), counter.getCount(o1));
					}
				}
			}
		};
	}

	/**
	 *  A List of the keys in c, sorted from highest count to lowest.
	 *
	 * @param c
	 * @return A List of the keys in c, sorted from highest count to lowest.
	 */
	public static <E> List<E> toSortedList(Counter<E> c) {
		List<E> l = new ArrayList<E>(c.keySet());
		Collections.sort(l, toComparatorDescending(c));
		return l;
	}

	/**
	 * Calculates the entropy of the given counter (in bits). This method internally
	 * uses normalized counts (so they sum to one), but the value returned is
	 * meaningless if some of the counts are negative.
	 *
	 * @return The entropy of the given counter (in bits)
	 */
	public static <E> double entropy(Counter<E> c) {
		double entropy = 0.0;
		double total = c.totalCount();
		for (E key : c.keySet()) {
			double count = c.getCount(key);
			if (count == 0) {
				continue; // 0.0 doesn't add entropy but may cause -Inf
			}
			count /= total; // use normalized count
			entropy -= count * (Math.log(count) / Math.log(2.0));
		}
		return entropy;
	}

	/**
	 * Note that this implementation doesn't normalize the "from" Counter.
	 * It does, however, normalize the "to" Counter.
	 * Result is meaningless if any of the counts are negative.
	 *
	 * @return The cross entropy of H(from, to)
	 */
	public static <E> double crossEntropy(Counter<E> from, Counter<E> to) {
		double tot2 = to.totalCount();
		double result = 0.0;
		double log2 = Math.log(2.0);
		for (E key : from.keySet()) {
			double count1 = from.getCount(key);
			if (count1 == 0.0) {
				continue;
			}
			double count2 = to.getCount(key);
			double logFract = Math.log(count2 / tot2);
			if (logFract == Double.NEGATIVE_INFINITY) {
				return Double.NEGATIVE_INFINITY; // can't recover
			}
			result += count1 * (logFract / log2); // express it in log base 2
		}
		return result;
	}

	/**
	 * Calculates the KL divergence between the two counters.
	 * That is, it calculates KL(from || to). This method internally
	 * uses normalized counts (so they sum to one), but the value returned is
	 * meaningless if any of the counts are negative.
	 * In other words, how well can c1 be represented by c2.
	 * if there is some value in c1 that gets zero prob in c2, then return positive infinity.
	 *
	 * @param from
	 * @param to
	 * @return The KL divergence between the distributions
	 */
	public static <E> double klDivergence(Counter<E> from, Counter<E> to) {
		double result = 0.0;
		double tot = (from.totalCount());
		double tot2 = (to.totalCount());
		// System.out.println("tot is " + tot + " tot2 is " + tot2);
		double log2 = Math.log(2.0);
		for (E key : from.keySet()) {
			double num = (from.getCount(key));
			if (num == 0) {
				continue;
			}
			num /= tot;
			double num2 = (to.getCount(key));
			num2 /= tot2;
			// System.out.println("num is " + num + " num2 is " + num2);
			double logFract = Math.log(num / num2);
			if (logFract == Double.NEGATIVE_INFINITY) {
				return Double.NEGATIVE_INFINITY; // can't recover
			}
			result += num * (logFract / log2); // express it in log base 2
		}
		return result;
	}

	/**
	 * Calculates the Jensen-Shannon divergence between the two counters.
	 * That is, it calculates 1/2 [KL(c1 || avg(c1,c2)) + KL(c2 || avg(c1,c2))] .
	 *
	 * @param c1
	 * @param c2
	 * @return The Jensen-Shannon divergence between the distributions
	 */
	public static <E> double jensenShannonDivergence(Counter<E> c1, Counter<E> c2) {
		Counter<E> average = average(c1, c2);
		double kl1 = klDivergence(c1, average);
		double kl2 = klDivergence(c2, average);
		return (kl1 + kl2) / 2.0;
	}

	/**
	 * Return the l2 norm (Euclidean vector length) of a Counter.
	 * <i>Implementation note:</i> The method name favors legibility of the
	 * L over the convention of using lowercase names for methods.
	 *
	 * @param c The Counter
	 * @return Its length
	 */
	public static <E, C extends Counter<E>> double L2Norm(C c) {
		double lenSq = 0.0;
		for (E key : c.keySet()) {
			double count = c.getCount(key);
			if (count != 0.0) {
				lenSq += (count * count);
			}
		}
		return Math.sqrt(lenSq);
	}


	/** L2 normalize a counter.
	 *
	 * @param c The {@link Counter} to be L2 normalized.  This counter is
	 *   not modified.
	 * @return A new l2-normalized Counter based on c.
	 */
	public static <E> Counter<E> L2Normalize(Counter<E> c) {
		return scale(c, 1.0/ L2Norm(c));
	}

	public static <E> double cosine(Counter<E> c1, Counter<E> c2) {
		double dotProd = 0.0;
		double lsq1 = 0.0;
		double lsq2 = 0.0;
		for (E key : c1.keySet()) {
			double count1 = c1.getCount(key);
			if (count1 != 0.0) {
				lsq1 += (count1 * count1);
				double count2 = c2.getCount(key);
				if (count2 != 0.0) {
					// this is the inner product
					dotProd += (count1 * count2);
				}
			}
		}
		for (E key : c2.keySet()) {
			double count2 = c2.getCount(key);
			if (count2 != 0.0) {
				lsq2 += (count2 * count2);
			}
		}
		if (lsq1 != 0.0 && lsq2 != 0.0) {
			double denom = (Math.sqrt(lsq1) * Math.sqrt(lsq2));
			return dotProd / denom;
		}
		return 0.0;
	}

	/**
	 * Returns a new Counter with counts averaged from the two given Counters.
	 * The average Counter will contain the union of keys in both
	 * source Counters, and each count will be the average of the two source
	 * counts for that key, where as usual a missing count in one Counter
	 * is treated as count 0.
	 *
	 * @return A new counter with counts that are the mean of the resp. counts
	 *         in the given counters.
	 */
	public static <E> Counter<E> average(Counter<E> c1, Counter<E> c2) {
		Counter<E> average = new Counter<E>();
		Set<E> allKeys = new HashSet<E>(c1.keySet());
		allKeys.addAll(c2.keySet());
		for (E key : allKeys) {
			average.setCount(key, (c1.getCount(key) + c2.getCount(key)) * 0.5);
		}
		return average;
	}

	public static <E> Counter<Double> getCountCounts(Counter<E> c) {
		Counter<Double> result = new Counter<Double>();
		for (double v : c.values()) {
			result.incrementCount(v);
		}
		return result;
	}

	/**
	 * Returns a new Counter which is scaled by the given scale factor.
	 */
	public static <E> Counter<E> scale(Counter<E> c, double s) {
		Counter<E> scaled = new Counter<E>();
		for (E key : c.keySet()) {
			scaled.setCount(key, c.getCount(key) * s);
		}
		return scaled;
	}

	/**
	 * Loads a Counter from a text file. File must have the format of one key/count pair per line,
	 * separated by whitespace.
	 *
	 * @param filename the path to the file to load the Counter from
	 * @param c        the Class to instantiate each member of the set. Must have a String constructor.
	 * @return The counter loaded from the file.
	 */
	public static <E> Counter<E> loadCounter(String filename, Class<E> c) throws RuntimeException {
		Counter<E> counter = new Counter<E>();
		loadIntoCounter(filename, c, counter);
		return counter;
	}

	/**
	 * Loads a file into an GenericCounter.
	 */
	private static <E> void loadIntoCounter(String filename, Class<E> c, Counter<E> counter) throws RuntimeException {
		try {
			Constructor<E> m = c.getConstructor(String.class);
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String line = in.readLine();
			while (line != null && line.length() > 0) {
				int endPos = Math.max(line.lastIndexOf(' '), line.lastIndexOf('\t'));

				counter.setCount(
						m.newInstance(line.substring(0, endPos).trim()),
						Double.parseDouble(line.substring(endPos, line.length()).trim()));

				line = in.readLine();
			}
			in.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Saves a Counter as one key/count pair per line separated by white space
	 * to the given OutputStream.  Does not close the stream.
	 */
	public static <E> void saveCounter(Counter<E> c, OutputStream stream) {
		PrintStream out = new PrintStream(stream);
		for (E key : c.keySet()) {
			out.println(key + " " + c.getCount(key));
		}
	}

	/**
	 * Saves a Counter to a text file. Counter written as one key/count pair per line,
	 * separated by whitespace.
	 */
	public static <E> void saveCounter(Counter<E> c, String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		saveCounter(c, fos);
		fos.close();
	}

	/**
	 * @return Returns the maximum element of c that is within the restriction Collection
	 */
	public static <E> E restrictedArgMax(Counter<E> c, Collection<E> restriction) {
		E maxKey = null;
		double max = Double.NEGATIVE_INFINITY;
		for (E key : restriction) {
			double count = c.getCount(key);
			if (count > max) {
				max = count;
				maxKey = key;
			}
		}
		return maxKey;
	}

	/**
	 * Does not assumes c is normalized.
	 * @param c
	 * @param rand
	 * @return A sample from c
	 */
	public static <T extends Comparable<T>> T sample(Counter<T> c, Random rand) {
		Iterable<T> objects;
		Set<T> keySet = c.keySet();
		objects = c.keySet();
		if (rand == null) {
			rand = new Random();
		} else { //TODO: Seems like there should be a way to directly check if T is comparable
			if (!keySet.isEmpty() && keySet.iterator().next() instanceof Comparable) {
				List<T> l = new ArrayList<T>(keySet);
				Collections.sort(l);
				objects = l;
			}
		}
		double r = rand.nextDouble() * c.totalCount();
		double total = 0.0;

		for (T t : objects) { // arbitrary ordering
			total += c.getCount(t);
			if (total>=r) return t;
		}
		// only chance of reaching here is if c isn't properly normalized, or if double math makes total<1.0
		return c.keySet().iterator().next();
	}


	/**
	 * Does not assumes c is normalized.
	 * @param c
	 * @return A sample from c
	 */

	public static <T extends Comparable<T>> T sample(Counter<T> c) {
		return sample(c, null);
	}

	/**
	 * Returns a counter where each element corresponds to the normalized
	 * count of the corresponding element in c raised to the given power.
	 */
	public static <E> Counter<E> powNormalized(Counter<E> c, double temp) {
		Counter<E> d = new Counter<E>();
		double total = c.totalCount();
		for (E e : c.keySet()) {
			d.setCount(e, Math.pow(c.getCount(e)/total, temp));
		}
		return d;
	}

	public static <T> Counter<T> pow(Counter<T> c, double temp) {
		Counter<T> d = new Counter<T>();
		for (T t : c.keySet()) {
			d.setCount(t, Math.pow(c.getCount(t), temp));
		}
		return d;
	}

	public static <T> void powInPlace(Counter<T> c, double temp) {
		for (T t : c.keySet()) {
			c.setCount(t, Math.pow(c.getCount(t), temp));
		}
	}

	public static <T> Counter<T> exp(Counter<T> c) {
		Counter<T> d = new Counter<T>();
		for (T t : c.keySet()) {
			d.setCount(t, Math.exp(c.getCount(t)));
		}
		return d;
	}

	public static <T> void expInPlace(Counter<T> c) {
		for (T t : c.keySet()) {
			c.setCount(t, Math.exp(c.getCount(t)));
		}
	}

	/**
	 * Default equality comparison for two counters potentially backed
	 * by alternative implementations.
	 */
	public static <E> boolean equals(Counter<E> o1, Counter<E> o2) {
		if (o1 == o2) { return true; }

		if (o1.totalCount() != o2.totalCount()) { return false;  }

		if (!o1.keySet().equals(o2.keySet())) { return false; }

		for (E key : o1.keySet()) {
			if (o1.getCount(key) != o2.getCount(key)) { return false; }
		}

		return true;
	}

	/**
	 * Returns a map view of the given counter.
	 */
	public static <E> Map<E,Double> asMap(final Counter<E> counter) {
		return new AbstractMap<E,Double>() {
			@Override
			public int size() {
				return counter.size();
			}

			@Override
			public Set<Entry<E, Double>> entrySet() {
				return counter.entrySet();
			}

			@Override
			@SuppressWarnings("unchecked")
			public boolean containsKey(Object key) {
				return counter.containsKey((E)key);
			}

			@Override
			@SuppressWarnings("unchecked")
			public Double get(Object key) {
				return counter.getCount((E)key);
			}

			@Override
			public Double put(E key, Double value) {
				double last = counter.getCount(key);
				counter.setCount(key, value);
				return last;
			}

			@Override
			@SuppressWarnings("unchecked")
			public Double remove(Object key) {
				return counter.remove((E)key);
			}

			@Override
			public Set<E> keySet() {
				return counter.keySet();
			}
		};
	}

}
