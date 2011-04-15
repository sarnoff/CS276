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

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A specialized kind of hash table (or map) for storing numeric counts for
 * objects. It works like a Map,
 * but with different methods for easily getting/setting/incrementing counts
 * for objects and computing various functions with the counts.
 * The Counter constructor
 * and <tt>addAll</tt> method can be used to copy another Counter's contents
 * over.
 * <p/>
 * <i>Implementation notes:</i>
 * You shouldn't casually add further methods to
 * this interface. Rather, they should be added to the {@link Counters} class.
 * Note that this class stores a
 * <code>totalCount</code> field as well as the map.  This makes certain
 * operations much more efficient, but means that any methods that change the
 * map must also update <code>totalCount</code> appropriately. If you use the
 * <code>setCount</code> method, then you cannot go wrong.
 * This class is not threadsafe: If multiple threads are accessing the same
 * counter, then access should be synchronized externally to this class.
 * 
 * This class is excerpted from JavaNLP.
 *
 * @author Dan Klein (klein@cs.stanford.edu)
 * @author Joseph Smarr (jsmarr@stanford.edu)
 * @author Teg Grenager
 * @author Galen Andrew
 * @author Christopher Manning
 * @author Kayur Patel (kdpatel@cs)
 * @author Daniel Ramage
 */
public class Counter<E> implements Serializable, Iterable<E> {

	/** Underlying map implementation */
	private Map<E, MutableDouble> map = new HashMap<E,MutableDouble>();
	
	private double totalCount = 0.0;
	private double defaultValue = 0.0;

	private static final long serialVersionUID = 5L;

	// for more efficient speed/memory usage
	private transient MutableDouble tempMDouble = null;


	// CONSTRUCTORS

	/**
	 * Constructs a new (empty) Counter backed by a HashMap.
	 */
	public Counter() {
	}

	/**
	 * Constructs a new Counter with the contents of the given Counter.
	 * <i>Implementation note:</i> A new Counter is allocated with its
	 * own counts, but keys will be shared and should be an immutable class.
	 *
	 * @param c The Counter which will be copied.
	 */
	public Counter(Counter<E> c) {
		this();
		Counters.addInPlace(this, c);
		setDefaultReturnValue(c.defaultReturnValue());
	}

	/**
	 * Constructs a new Counter by counting the elements in the given Collection.
	 * The Counter is backed by a HashMap.
	 *
	 * @param collection Each item in the Collection is made a key in the
	 *     Counter with count being its multiplicity in the Collection.
	 */
	public Counter(Collection<E> collection) {
		this();
		for (E key : collection) {
			incrementCount(key);
		}
	}

	// METHODS NEEDED BY THE Counter INTERFACE

	/** {@inheritDoc} */
	public final void setDefaultReturnValue(double rv) { defaultValue = rv; }

	/** {@inheritDoc} */
	public double defaultReturnValue() { return defaultValue; }


	/** {@inheritDoc} */
	public double getCount(E key) {
		Number count = map.get(key);
		if (count == null) {
			return defaultValue; // haven't seen this object before -> default count
		}
		return count.doubleValue();
	}

	/** {@inheritDoc} */
	public void setCount(E key, double count) {
		if (tempMDouble == null) {
			tempMDouble = new MutableDouble();
		}
		
		tempMDouble.set(count);
		tempMDouble = map.put(key, tempMDouble);

		totalCount += count;
		if (tempMDouble != null) {
			totalCount -= tempMDouble.doubleValue();
		}
	}


	/** {@inheritDoc} */
	public double incrementCount(E key, double count) {
		if (tempMDouble == null) {
			tempMDouble = new MutableDouble();
		}
		
		MutableDouble oldMDouble = map.put(key, tempMDouble);
		totalCount += count;
		if (oldMDouble != null) {
			count += oldMDouble.doubleValue();
		}
		
		tempMDouble.set(count);
		tempMDouble = oldMDouble;

		return count;
	}

	/** {@inheritDoc} */
	public final double incrementCount(E key) {
		return incrementCount(key, 1.0);
	}

	/** {@inheritDoc} */
	public double decrementCount(E key, double count) {
		return incrementCount(key, -count);
	}

	/** {@inheritDoc} */
	public double decrementCount(E key) {
		return incrementCount(key, -1.0);
	}

	/** {@inheritDoc} */
	public void addAll(Counter<E> counter) {
		Counters.addInPlace(this, counter);
	}

	/** {@inheritDoc} */
	public double remove(E key) {
		MutableDouble d = mutableRemove(key); // this also updates totalCount
		if(d != null) {
			return d.doubleValue();
		}
		return Double.NaN;
	}

	/** {@inheritDoc} */
	public boolean containsKey(E key) {
		return map.containsKey(key);
	}

	/** {@inheritDoc} */
	public Set<E> keySet() {
		return map.keySet();
	}

	/** {@inheritDoc} */
	public Collection<Double> values() {
		return new AbstractCollection<Double>() {
			@Override
			public Iterator<Double> iterator() {
				return new Iterator<Double>() {
					Iterator<MutableDouble> inner = map.values().iterator();

					public boolean hasNext() {
						return inner.hasNext();
					}

					public Double next() {
						// copy so as to give safety to mutable internal representation
						return Double.valueOf(inner.next().doubleValue());
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}

			@Override
			public int size() {
				return map.size();
			}

			@Override
			public boolean contains(Object v) {
				return v instanceof Double && map.values().contains(new MutableDouble((Double) v));
			}

		};
	}

	/** {@inheritDoc} */
	public Set<Map.Entry<E,Double>> entrySet() {
		return new AbstractSet<Map.Entry<E,Double>>() {
			@Override
			public Iterator<Entry<E, Double>> iterator() {
				return new Iterator<Entry<E,Double>>() {
					final Iterator<Entry<E,MutableDouble>> inner = map.entrySet().iterator();

					public boolean hasNext() {
						return inner.hasNext();
					}

					public Entry<E, Double> next() {
						return new Entry<E,Double>() {
							final Entry<E,MutableDouble> e = inner.next();

							public double getDoubleValue() {
								return e.getValue().doubleValue();
							}

							public double setValue(double value) {
								final double old = e.getValue().doubleValue();
								e.getValue().set(value);
								totalCount = totalCount - old + value;
								return old;
							}

							public E getKey() {
								return e.getKey();
							}

							public Double getValue() {
								return getDoubleValue();
							}

							public Double setValue(Double value) {
								return setValue(value.doubleValue());
							}
						};
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}

			@Override
			public int size() {
				return map.size();
			}
		};
	}

	/** {@inheritDoc} */
	public void clear() {
		map.clear();
		totalCount = 0.0;
	}

	/** {@inheritDoc} */
	public int size() {
		return map.size();
	}

	/** {@inheritDoc} */
	public double totalCount() {
		return totalCount;
	}


	// ADDITIONAL MAP LIKE OPERATIONS (NOT IN Counter INTERFACE)
	// THEIR USE IS DISCOURAGED, BUT THEY HAVEN'T (YET) BEEN REMOVED.

	/** This is a shorthand for keySet.iterator(). It's not really clear that
	 *  this method should be here, as the Map interface has no such shortcut,
	 *  but it's used in a number of places, and I've left it in for now.
	 *  Use is discouraged.
	 *
	 *  @return An Iterator over the keys in the Counter.
	 */
	public Iterator<E> iterator() {
		return keySet().iterator();
	}

	/** This is used internally to the class for getting back a
	 *  MutableDouble in a remove operation.  Not for public use.
	 *
	 *  @param key The key to remove
	 *  @return Its value as a MutableDouble
	 */
	private MutableDouble mutableRemove(E key) {
		MutableDouble md = map.remove(key);
		if (md != null) {
			totalCount -= md.doubleValue();
		}
		return md;
	}


	/**
	 * Removes all the given keys from this Counter.
	 * Keys may be included that are not actually in the
	 * Counter - no action is taken in response to those
	 * keys.  This behavior should be retained in future
	 * revisions of Counter (matches HashMap).
	 *
	 * @param keys The keys to remove from the Counter. Their values are
	 *     subtracted from the total count mass of the Counter.
	 */
	public void removeAll(Collection<E> keys) {
		for (E key : keys) {
			mutableRemove(key);
		}
	}

	/** Returns whether a Counter has no keys in it.
	 *
	 *  @return true iff a Counter has no keys in it.
	 */
	public boolean isEmpty() {
		return size() == 0;
	}


	// OBJECT STUFF

	// NOTE: Using @inheritdoc to get back to Object's javadoc doesn't work
	// on a class that implements an interface in 1.6.  Weird, but there you go.

	/** Equality is defined over all Counter implementations.
	 *  Two Counters are equal if they have the same keys explicitly stored
	 *  with the same values.
	 *  <p>
	 *  Note that a Counter with a key with value defaultReturnValue will not
	 *  be judged equal to a Counter that is lacking that key. In order for
	 *  two Counters to be correctly judged equal in such cases, you should
	 *  call Counters.retainNonDefaultValues() on both Counters first.
	 *
	 *  @param o Object to compare for equality
	 *  @return Whether this is equal to o
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if ( ! (o instanceof Counter)) {
			return false;
		}

		final Counter<E> counter = (Counter<E>) o;
		return totalCount == counter.totalCount && map.equals(counter.map);
	}


	/** Returns a hashCode which is the underlying Map's hashCode.
	 *
	 *  @return A hashCode.
	 */
	@Override
	public int hashCode() {
		return map.hashCode();
	}

	/**
	 * Returns the top k values in this counter
	 */
	public List<E> topK(int k) {
		LinkedList<E> top = new LinkedList<E>();
		Comparator<E> comparator = Counters.toComparatorDescending(this);
		for (Map.Entry<E,Double> entry : entrySet()) {
			if (top.size() < k || getCount(top.getLast()) < entry.getValue()) {
				top.add(entry.getKey());
				if (top.size() >= k) {
					Collections.sort(top, comparator);
					while (top.size() > k) {
						top.removeLast();
					}
				}
			}
		}
		
		// one final sort just in case we didn't need to remove
		Collections.sort(top, comparator);
		return top;
	}
	
	/** Returns a String representation of the Counter, as formatted by
	 *  the underlying Map.
	 *
	 *  @return A String representation of the Counter.
	 */
	@Override
	public String toString() {
		return map.toString();
	}


	// EXTRA I/O METHODS

	/**
	 * Converts from the format printed by the toString method back into
	 * a Counter&lt;String&gt;.  The toString() doesn't escape, so this only
	 * works providing the keys of the Counter do not have commas or equals signs
	 * in them.
	 *
	 * @param s A String representation of a Counter
	 * @return The Counter
	 */
	public static Counter<String> fromString(String s) {
		Counter<String> result = new Counter<String>();
		if (!s.startsWith("{") || !s.endsWith("}")) {
			throw new RuntimeException("invalid format: ||"+s+"||");
		}
		s = s.substring(1, s.length()-1);
		String[] lines = s.split(", ");
		for (String line : lines) {
			String[] fields = line.split("=");
			if (fields.length!=2) throw new RuntimeException("Got unsplittable line: \"" + line + '\"');
			result.setCount(fields[0], Double.parseDouble(fields[1]));
		}
		return result;
	}

	/**
	 * A class for Double objects that you can change.
	 *
	 * @author Dan Klein
	 */
	private static class MutableDouble extends Number implements Comparable<MutableDouble> {

		private double d;

		// Mutable
		public void set(double d) {
			this.d = d;
		}

		@Override
		public int hashCode() {
			long bits = Double.doubleToLongBits(d);
			return (int) (bits ^ (bits >>> 32));
		}

		/**
		 * Compares this object to the specified object.  The result is
		 * <code>true</code> if and only if the argument is not
		 * <code>null</code> and is an <code>MutableDouble</code> object that
		 * contains the same <code>double</code> value as this object.
		 * Note that a MutableDouble isn't and can't be equal to an Double.
		 *
		 * @param obj the object to compare with.
		 * @return <code>true</code> if the objects are the same;
		 *         <code>false</code> otherwise.
		 */
		@Override
		public boolean equals(Object obj) {
			return obj instanceof MutableDouble && d == ((MutableDouble) obj).d;
		}

		@Override
		public String toString() {
			return Double.toString(d);
		}

		// Comparable interface

		/**
		 * Compares two <code>MutableDouble</code> objects numerically.
		 *
		 * @param anotherMutableDouble the <code>MutableDouble</code> to be
		 *                             compared.
		 * @return Tthe value <code>0</code> if this <code>MutableDouble</code> is
		 *         equal to the argument <code>MutableDouble</code>; a value less than
		 *         <code>0</code> if this <code>MutableDouble</code> is numerically less
		 *         than the argument <code>MutableDouble</code>; and a value greater
		 *         than <code>0</code> if this <code>MutableDouble</code> is numerically
		 *         greater than the argument <code>MutableDouble</code> (signed
		 *         comparison).
		 */
		public int compareTo(MutableDouble anotherMutableDouble) {
			double thisVal = this.d;
			double anotherVal = anotherMutableDouble.d;
			return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
		}

		// Number interface
		@Override
		public int intValue() {
			return (int) d;
		}

		@Override
		public long longValue() {
			return (long) d;
		}

		@Override
		public short shortValue() {
			return (short) d;
		}

		@Override
		public byte byteValue() {
			return (byte) d;
		}

		@Override
		public float floatValue() {
			return (float) d;
		}

		@Override
		public double doubleValue() {
			return d;
		}

		public MutableDouble() {
			this(0.0);
		}

		public MutableDouble(double d) {
			this.d = d;
		}

		public MutableDouble(Number num) {
			this.d = num.doubleValue();
		}

		private static final long serialVersionUID = 624465615824626762L;
	}

}
