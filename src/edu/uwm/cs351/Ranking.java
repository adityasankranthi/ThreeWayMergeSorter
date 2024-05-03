package edu.uwm.cs351;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

import edu.uwm.cs351.util.AbstractEntry;

public class Ranking<T> extends AbstractMap<Integer,T>
{
	private static Consumer<String> reporter = (s) -> System.out.println("Invariant error: "+ s);
	
	private static boolean report(String error) {
		reporter.accept(error);
		return false;
	}
	
	private static class Node<E> {
		E data;
		int count;
		Node<E> left, right, parent;
		
		Node(E d) {
			data = d;
			count = 1;
		}
		
		@Override // decorate
		public String toString() {
			return super.toString() + "(" + data + "," + count + ")";
		}
	}
	
	// A helper method to hide the messiness of creating the array
	@SuppressWarnings("unchecked")
	protected T[] makeArray(int n) {
		return (T[]) new Object[n];
	}
	
	private Node<T> root;
	private int version;
	
	/**
	 * Return the number of nodes in this subtree.
	 * If the subtree isn't null, we use the stored `count' field rather than
	 * computing the count.
	 * @param r subtree to count nodes for, must not be null
	 * @return count of nodes in this subtree
	 */
	private static int getCount(Node<?> r) {
		if (r == null) return 0;
		return r.count;
	}
	
	private void setCount(Node<T> r) {
		if (r == null) return;
		r.count = getCount(r.left)+ getCount(r.right) + 1; 
	}
	
	/**
	 * Check that a subtree's count and parent fields are correct.
	 * @param r subtree to check
	 * @param p what the parent should be.
	 * @return whether any problems ere found.  If false is returned than 
	 * exactly one problem has been reported.
	 */
	private boolean checkTree(Node<T> r, Node<T> p) {
		if (r == null) return true;
		if (r.parent != p) return report("parent is wrong for " + r); 
		if (r.count != getCount(r.left) + getCount(r.right) + 1) {
			return report("computed count " + r.count + " not consistent with subtrees");
		}
		return checkTree(r.left, r) && checkTree(r.right, r);
	}
	

	private boolean wellFormed() {
		return checkTree(root, null);
	}
	
	/**
	 * Create an empty ranking.
	 */
	public Ranking() {
		root = null;
		assert wellFormed() : "invariant broken in constructor";
	}
	
	private Ranking(boolean ignored) {} // don't change this
	
	/**
	 * Return the k'th (1 based) node in the subtree.
	 * This helper method uses the count fields to avoid an
	 * in-order traversal.
	 * @param r subtree to examine, may be null
	 * @param k index (1 based)
	 * @return k'th node of the tree or null if none such
	 */
	private Node<T> doGet(Node<T> r, int k) {
		if (r == null) return null;
		int l = getCount(r.left);
		if (k <= l) return doGet(r.left, k);
		if (k == l+1) return r;
		return doGet(r.right, k - l - 1);
	}

	@Override // efficiency
	public boolean containsKey(Object key) {
		assert wellFormed() : "Invariant broken in containsKey";
		if (!(key instanceof Integer)) return false;
		Integer rank = (Integer)key;
		return rank > 0 && rank <= size();
	}

	@Override // efficiency
	public T get(Object key) {
		assert wellFormed() : "Invariant broken in get";
		if (!(key instanceof Integer)) return null;
		int k = ((Integer)key).intValue();
		if (k <= 0 || k > getCount(root)) return null;
		return doGet(root, k).data;
	}
	
	private Node<T> doInsert(Node<T> r, int k, T val, Node<T> p) {
		if (r == null) {
			assert k == 1;
			r = new Node<>(val);
			r.parent = p;
		} else {
			int l = getCount(r.left);
			if (k <= l+1) {
				r.left = doInsert(r.left, k, val, r);
			} else {
				r.right = doInsert(r.right, k-l-1, val, r);
			}
			setCount(r);
		}
		return r;
	}
	
	@Override // implementation
	public T put(Integer key, T value) {
		assert wellFormed() : "Invariant broken in put";
		int k = key.intValue(); // throws NPE if key is null
		if (k <= 0 || k > size()+1) throw new IllegalArgumentException("Cannot use rank " + k);
		Node<T> node = doGet(root, k);
		T old = null;
		if (node == null) {
			assert k == size()+1;
			insert(k, value);
		} else {
			old = node.data;
			node.data = value;
		}
		assert wellFormed() : "Invariant broken by put";
		return old;
	}

	private Node<T> doRemove(Node<T> r, int k) {
		if (r == null) return null;
		int l = getCount(r.left);
		if (k <= l) {
			r.left = doRemove(r.left, k);
		} else if (k == l+1) {
			if (r.left == null && r.right == null) return null;
			if (r.left == null) {
				r.right.parent = r.parent;
				return r.right;
			} else if (r.right == null) {
				r.left.parent = r.parent;
				return r.left;
			}
			Node<T> tmp = r.left;
			while (tmp.right != null) {
				tmp = tmp.right;
			}
			r.data = tmp.data;
			r.left = doRemove(r.left,getCount(r.left));
		} else {
			r.right = doRemove(r.right, k-l-1);
		}
		setCount(r);
		return r;
	}
	
	@Override // efficiency
	public T remove(Object key) {
		assert wellFormed() : "Invariant broken in remove";
		if (!(key instanceof Integer)) return null;
		int k = ((Integer)key).intValue();
		if (k <= 0 || k > size()) return null;
		T result = get(k);
		root = doRemove(root, k);
		++version;
		assert wellFormed() : "Invariant broken by remove";
		return result;
	}

	/**
	 * Insert a new data at the given rank
	 * @param k rank to insert at, must be between 1 and size()+1 inclusive
	 * @param value value to add, may be null
	 * @exception IllegalArgumentException if rank is not valid
	 */
	public void insert(int k, T value) {
		assert wellFormed() : "Invariant broken in insert";
		if (k <= 0 || k > size()+1) throw new IllegalArgumentException("out of range: " + k);
		root = doInsert(root, k, value, null);
		++version;
		assert wellFormed() : "Invariant broken by insert";
	}
	
	private Set<Map.Entry<Integer, T>> entrySet;
	
	@Override // required
	public Set<Entry<Integer, T>> entrySet() {
		assert wellFormed() : "Invariant broken in entrySet";
		if (entrySet == null) entrySet = new EntrySet();
		return entrySet;
	}

	private class EntrySet extends AbstractSet<Map.Entry<Integer,T>> {

		@Override // required
		public int size() {
			assert wellFormed() : "Invariant broken in size";
			return getCount(root);
		}

		@Override // required
		public Iterator<Entry<Integer, T>> iterator() {
			assert wellFormed() : "Invariant broken in iterator";
			return new MyIterator();
		}

		@Override // efficiency
		public boolean contains(Object o) {
			assert wellFormed() : "invariant broken in contains";
			if (!(o instanceof Entry<?,?>)) return false;
			Entry<?,?> e = (Entry<?,?>)o;
			if (!(e.getKey() instanceof Integer)) return false;
			Node<T> node = doGet(root, (Integer)e.getKey());
			if (node == null) return false;
			if (node.data == null) return e.getValue() == null;
			return node.data.equals(e.getValue());
		}

		@Override // efficiency
		public boolean remove(Object o) {
			assert wellFormed() : "invariant broken in remove";
			if (!contains(o)) return false;
			Entry<?,?> e = (Entry<?,?>)o;
			Ranking.this.remove(e.getKey());
			return true;
		}	
	}
	
	/**
	 * Return the next node in in-order traversal.
	 * This method uses the parent pointers.
	 * @param n node to get next for, must not be null
	 * @return next node in in-order traversal
	 */
	private Node<T> getNext(Node<T> n) {
		if (n.right == null) {
			Node<T> p = n.parent;
			while (p != null) {
				if (p.left == n) return p;
				assert n == p.right;
				n = p;
				p = p.parent;
			}
			return null;
		} else {
			n = n.right;
			while (n.left != null) {
				n = n.left;
			}
			return n;
		}
	}
	
	/**
	 * Wrapper class for nodes to be entries
	 */
	private class MyEntry extends AbstractEntry<Integer,T> {
		private final int rank;
		private final Node<T> cur;
		
		MyEntry(int r, Node<T> n) {
			rank = r;
			cur = n;
		}
		
		@Override // required
		public Integer getKey() {
			return rank;
		}

		@Override // required
		public T getValue() {
			return cur.data;
		}

		@Override // implementation
		public T setValue(T v) {
			assert wellFormed() : "invariant broken in setValue";
			T oldValue = cur.data;
			cur.data = v;
			assert wellFormed() : "invariant broken by setValue";
			return oldValue;
		}
	}
	
	private class MyIterator implements Iterator<Map.Entry<Integer,T>>
	{
		// Fields:
		private int rank;
		private Node<T> cur, next;
		private int colVersion;
	
		private boolean wellFormed() {
			if (!Ranking.this.wellFormed()) return false;
			if (version != colVersion) return true;
			if (next != doGet(root, rank+1)) return report("next is wrong node: " + next + " for rank " + rank);
			if (cur != next) {
				if (cur != doGet(root, rank)) return report("cur is wrong node: " + cur + " for rank " + rank);
			}
			return true;
		}
		
		MyIterator() {
			rank = 0;
			next = root;
			if (next != null) {
				while (next.left != null) {
					next = next.left;
				}
			}
			cur = next;
			colVersion = version;
			assert wellFormed() : "Invariant broken by MyIterator constructor";
		}
		
		MyIterator(boolean ignored) {} // do not change this
		
		private void checkVersion() {
			if (version != colVersion) throw new ConcurrentModificationException("stale!");
		}
		
		@Override // required
		public boolean hasNext() {
			assert wellFormed() : "invariant broken in hasNext";
			checkVersion();
			return next != null;
		}

		@Override // required
		public Entry<Integer, T> next() {
			assert wellFormed() : "Invariant broken in next";
			if (!hasNext()) throw new NoSuchElementException("no more");
			cur = next;
			++rank;
			next = getNext(next);
			assert wellFormed() : "Invariant broken by next";
			return new MyEntry(rank,cur);
		}

		@Override // implementation
		public void remove() {
			assert wellFormed() : "Invariant broken in iterator remove";
			checkVersion();
			if (cur == next) throw new IllegalStateException("cannot remove right now");
			cur = next;
			root = doRemove(root,rank);
			--rank;
			colVersion = ++version;
			assert wellFormed() : "Invariant broken by iterator remove";
		}
	}

	/**
	 * Check whether a sequence in an array is sorted.
	 * @param comp comparator to use
	 * @param array array to examine elements of
	 * @param lo inclusive lower bound
	 * @param hi exclusive upper bound
	 * @return whether the sequence is empty or in non-decreasing order
	 * @throws ArrayIndexOutOfBoundsException if an elements in the range is outside the bounds of the array
	 */
	private boolean isSorted(Comparator<T> comp, T[] array, int lo, int hi) {
	    if (lo >= hi) {
	        return true; 
	    }
	    T prev = array[lo];
	    for (int i = lo + 1; i < hi; i++) {
	        T curr = array[i];
	        if (comp.compare(prev, curr) > 0) {
	            return false; 
	        }
	        prev = curr;
	    }
	    return true; 
	}
	
	/**
	 * Merge three sequences of an array into one at the same indices
	 * of a second array. The first and third sequences are arbitrary in length,
	 * but the second sequence always consists of a single element.
	 * The merging is stable, the relative order of equivalent elements in the array
	 * is preserved.
	 * The in array should not be modified and the out array
	 * should only be modified at the indices where the elements lie.
	 * @param comp comparator to use, must not be null
	 * @param start1 starting index of the first sequence
	 * @param start2 starting index of the second sequence.
	 * The third sequence starts immediately afterwards
	 * @param end3 the index <em>after</em> the third sequence
	 * @param out array to place the sorted elements
	 * @param in array from which to read elements.
	 * The input array cannot be same as the output array.
	 * @throws IllegalArgumentException if one of the sequences is not sorted
	 * @throws ArrayIndexOutOfBoundsException if the ranges are not contained in the arrays
	 */
	private void merge3(Comparator<T> comp, int start1, int start2, int end3, T[] out, T[] in) {
	    if (in == out) {
	        throw new IllegalArgumentException("Input and output arrays cannot be the same");
	    }

	    if (!isSorted(comp, in, start1, start2)) {
	        throw new IllegalArgumentException("First sequence not sorted");
	    }

	    if (!isSorted(comp, in, start2, start2 + 1)) {
	        throw new IllegalArgumentException("Second sequence not sorted");
	    }

	    if (!isSorted(comp, in, start2 + 1, end3)) {
	        throw new IllegalArgumentException("Third sequence not sorted");
	    }

	    int i = start1, j = start2, k = start2 + 1;
	    for (int l = start1; l < end3; l++) {
	        if (i < start2) {
	            if (j < start2 + 1) {
	                if (k < end3) {
	                    out[l] = smallest(comp, in[i], in[j], in[k]);
	                    if (out[l] == in[i]) i++;
	                    else if (out[l] == in[j]) j++;
	                    else k++;
	                } else {
	                    out[l] = smallest(comp, in[i], in[j]);
	                    if (out[l] == in[i]) i++;
	                    else j++;
	                }
	            } else {
	                if (k < end3) {
	                    out[l] = smallest(comp, in[i], in[k]);
	                    if (out[l] == in[i]) i++;
	                    else k++;
	                } else {
	                    System.arraycopy(in, i, out, l, end3 - l);
	                    break;
	                }
	            }
	        } else {
	            if (j < start2 + 1) {
	                if (k < end3) {
	                    out[l] = smallest(comp, in[j], in[k]);
	                    if (out[l] == in[j]) j++;
	                    else k++;
	                } else {
	                    out[l] = in[j];
	                    j++;
	                }
	            } else {
	                System.arraycopy(in, k, out, l, end3 - l);
	                break;
	            }
	        }
	    }
	}

	private T smallest(Comparator<T> comp, T a, T b, T c) {
	    T min = a;
	    if (comp.compare(b, min) < 0) min = b;
	    if (comp.compare(c, min) < 0) min = c;
	    return min;
	}

	private T smallest(Comparator<T> comp, T a, T b) {
	    return comp.compare(a, b) <= 0 ? a : b;
	}
	
	/**
	 * mergesort the elements in the subtree 
	 * @param r subtree to use, may be null
	 * @param comp comparator, must not be null
	 * @param start starting index to write elements into the array
	 * @param out array to write sorted elements to
	 * @param tmp temporary array that may be used t same indices
	 */
	private void doMergesort(Node<T> r, Comparator<T> comp, int start, T[] out, T[] tmp) {
	    if (r == null) return;

	    int leftCount = getCount(r.left);
	    int rightCount = getCount(r.right);

	    doMergesort(r.left, comp, start, out, tmp);
	    doMergesort(r.right, comp, start + leftCount + 1, out, tmp);

	    for (int i = start; i < start + leftCount; i++) {
	        tmp[i] = out[i];
	    }
	    tmp[start + leftCount] = r.data;
	    for (int i = start + leftCount + 1; i < start + leftCount + rightCount + 1; i++) {
	        tmp[i] = out[i];
	    }

	    merge3(comp, start, start + leftCount, start + leftCount + rightCount + 1, out, tmp);
	}
	
	/**
	 * Return a sorted array of the elements in the ranking.
	 * The sorting is "stable" in that two elements equivalent
	 * according to the comparator will stay in teh same order as they 
	 * are in the ranking.  In particular, if a non-discriminating comparator
	 * is used, the array will duplicate the ranking order.
	 * @param array array to use (if it is big enough).  It must not be null.
	 * If the array is not big enough, a new array is created of the minimum size
	 * using the argument as a template.
	 * @param comp comparator to use.  If null, then the natural comparator is used.
	 * @throws ClassCastException if the parameter is null but the elements are not comparable,
	 * and there is at least one.
	 * @throws NullPointerException if null is present and cannot be handled by the comparator
	 * @see Comparator#naturalOrder()
	 * @return array of sorted elements, using the given array if space is sufficient.
	 */
	@SuppressWarnings("unchecked")
	public T[] toSortedArray(T[] array, Comparator<T> comp) {
		assert wellFormed() : "invariant broken in toSortedArray";
		// TODO
		return array;
	}
	
	/**
	 * Testing class.  Do not change.
	 * @param T type of elements
	 */
	public static class Spy<T> {
		/**
		 * A public version of the data structure's internal node class.
		 * This class is only used for testing.
		 */
		public static class Node<U> extends Ranking.Node<U> {
			/**
			 * Create a node with default values for all fields.
			 */
			public Node() {
				this(null, 0, null, null, null);
			}
			/**
			 * Create a node with the given values
			 * @param d data for new node, may be null
			 * @param c count for new node,
			 * @param l left for new node, may be null
			 * @param r right for new node, may be null
			 * @param p parent for new node, may be null
			 */
			public Node(U d, int c, Node<U> l, Node<U> r, Node<U> p) {
				super(null);
				this.count = c;
				this.data = d;
				this.parent = p;
				this.left = l;
				this.right = r;
			}
		}
		
		private static final Node<?> SPY_PH = new Node<>();
		
		/**
		 * Return the spy placeholder that will be substituted
		 * wit the real one.
		 * @return place holder.
		 */
		@SuppressWarnings("unchecked")
		public Node<T> ph() {
			return (Node<T>)SPY_PH;
		}
		
		/**
		 * Create a node for testing.
			 * @param d data for new node, may be null
			 * @param c count for new node,
			 * @param l left for new node, may be null
			 * @param r right for new node, may be null
			 * @param p parent for new node, may be null
		 * @return newly created test node
		 */
		public Node<T> newNode(T d, int c, Node<T> l, Node<T> r, Node<T> p) {
			return new Node<T>(d, c, l, r, p);
		}
		
		/**
		 * Create a node with all default fields for testing.
		 * @return newly created test node
		 */
		public Node<T> newNode() {
			return new Node<T>();
		}
		
		/**
		 * Change a node's left field
		 * @param n1 node to change, must not be null
		 * @param n2 node to point to, may be null
		 */
		public void setLeft(Node<T> n1, Node<T> n2) {
			n1.left = n2;
		}
		
		/**
		 * Change a node's right field
		 * @param n1 node to change, must not be null
		 * @param n2 node to point to, may be null
		 */
		public void setRight(Node<T> n1, Node<T> n2) {
			n1.right = n2;
		}
		
		/**
		 * Change a node's parent field
		 * @param n1 node to change, must not be null
		 * @param n2 node to point to, may be null
		 */
		public void setParent(Node<T> n1, Node<T> n2) {
			n1.parent = n2;
		}

		/**
		 * Return the sink for invariant error messages
		 * @return current reporter
		 */
		public Consumer<String> getReporter() {
			return reporter;
		}

		/**
		 * Change the sink for invariant error messages.
		 * @param r where to send invariant error messages.
		 */
		public void setReporter(Consumer<String> r) {
			reporter = r;
		}

		
		/**
		 * Create a testing instance of a ranking with the given
		 * data structure.
		 * @param r the root
		 * @param v the version
		 * @return a new testing ranking with this data structure.
		 */
		public Ranking<T> newInstance(Node<T> r, int v) {
			Ranking<T> result = new Ranking<>(false);
			result.root = r;
			result.version = v;
			return result;
		}
			
		/**
		 * Creates a testing instance of an iterator
		 * @param outer the collection attached to the iterator
		 * @param r the rank
		 * @param c the current node
		 * @param n the next node
		 * @param cv the colVersion
		 */
		public Iterator<Entry<Integer,T>> newIterator(Ranking<T> outer, int r, Node<T> c, Node<T> n, int cv) {
			Ranking<T>.MyIterator result = outer.new MyIterator(false);
			result.rank = r;
			result.cur = c;
			result.next = n;
			result.colVersion = cv;
			return result;
		}
			
		/**
		 * Check the invariant on the given dynamic array robot.
		 * @param r ranking to check, must not be null
		 * @return whether the invariant is computed as true
		 */
		public boolean wellFormed(Ranking<?> r) {
			return r.wellFormed();
		}
			
		/**
		 * Check the invariant on the given Iterator.
		 * @param it iterator to check, must not be null
		 * @return whether the invariant is computed as true
		 */
		public boolean wellFormed(Iterator<Entry<Integer,T>> it) {
			Ranking<T>.MyIterator myIt = (Ranking<T>.MyIterator)it;
			return myIt.wellFormed();
		}
		
		/**
		 * Call getCount helper method with these arguments
		 * @param rank ranking to use, must not be null
		 * @param r node to get count of
		 * @return the int count of the node passed
		 */
		public int getCount(Node<T> r) {
			return Ranking.getCount(r);
		}
		
		/**
		 * Call doGet helper method with these arguments
		 * @param rank ranking to use, must not be null
		 * @param r subtree to use
		 * @param k rank to look for
		 * @return the node within the subtree associated with the given rank or null
		 */
		public Node<T> doGet(Ranking<T> rank, Node<T> r, int k) {
			return (Node<T>) rank.doGet(r, k);
		}
		
		/**
		 * Call checkTree helper method with these arguments
		 * @param rank ranking to use, must not be null
		 * @param r subtree to examine
		 * @param p parent node of the subtree being used, may be null
		 * @return whether any problems were found within the specified subtree
		 */
		public boolean checkTree(Ranking<T> rank, Node<T> r, Node<T> p) {
			return rank.checkTree(r, p);
		}
		
		/**
		 * Call merge3 helper method with these arguments
		 * @param comp comparator to use, must not be null
		 * @param start1 starting index of the first sequence
		 * @param start2 starting index of the second sequence.
		 * The third sequence starts immediately afterwards
		 * @param end3 the index <em>after</em> the third sequence
		 * @param a array to place the sorted elements
		 * @param b array from which to read elements.
		 * The input array cannot be same as the output array.
		 */
		public void merge3(Ranking<T> rank, Comparator<T> comp, int start1, int start2, int end3, T[] a, T[] b) {
			rank.merge3(comp, start1, start2, end3, a, b);
		}
		
		/**
		 * Call doMergesort helper method with these arguments
		 * @param r subtree to use, may be null
		 * @param comp comparator, must not be null
		 * @param start starting index to write elements into the array
		 * @param out array to write sorted elements to
		 * @param tmp temporary array that may be used t same indices
		 */
		public void doMergesort(Ranking<T> rank, Node<T> r, Comparator<T> comp, int start, T[] out, T[] tmp) {
			rank.doMergesort(r, comp, start, out, tmp);
		}
		
		/**
		 * Call isSorted helper method with these arguments
		 * @param comp comparator to use
		 * @param array array to examine elements of
		 * @param lo inclusive lower bound
		 * @param hi exclusive upper bound
		 * @return whether the sequence is empty or in non-decreasing order
		 * @throws ArrayIndexOutOfBoundsException if an elements in the range is outside the bounds of the array
		 */
		public boolean isSorted(Ranking<T> rank, Comparator<T> comp, T[] array, int lo, int hi) {
			return rank.isSorted(comp, array, lo, hi);
		}
	}

}
