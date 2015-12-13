package uk.ac.manchester.cs.graph;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class SortedSetImpl<T> implements SortedSet<T> {

	protected Set<T> m_set;

	protected List<T> m_items;

	protected Comparator<T> m_comparator;

	public SortedSetImpl() {
		this.m_set = null;
		this.m_items = null;
		this.m_comparator = new SortedSetImpl.ComparatorImpl();
		this.m_set = new HashSet<>();
		this.m_items = new ArrayList<>();
	}

	public SortedSetImpl(Comparator<T> comparator) {
		this();
		this.m_comparator = comparator;
	}

	public SortedSetImpl(int size) {
		this.m_set = null;
		this.m_items = null;
		this.m_comparator = new SortedSetImpl.ComparatorImpl();
		this.m_set = new HashSet<>(size);
		this.m_items = new ArrayList<>(size);
	}

	public SortedSetImpl(int size, Comparator<T> comparator) {
		this(size);
		this.m_comparator = comparator;
	}

	public SortedSetImpl(Collection<? extends T> elems) {
		this.m_set = null;
		this.m_items = null;
		this.m_comparator = new SortedSetImpl.ComparatorImpl();
		this.m_set = new HashSet<>(elems);
		this.m_items = new ArrayList<>(this.m_set);
		Collections.sort(this.m_items, this.m_comparator);
	}

	public SortedSetImpl(Collection<? extends T> elems, Comparator<T> comparator) {
		this.m_set = null;
		this.m_items = null;
		this.m_comparator = new SortedSetImpl.ComparatorImpl();
		this.m_comparator = comparator;
		this.m_set = new HashSet<>(elems);
		this.m_items = new ArrayList<>(this.m_set);
		Collections.sort(this.m_items, this.m_comparator);
	}

	public Comparator<? super T> comparator() {
		return this.m_comparator;
	}

	public T first() {
		return this.m_items.get(0);
	}

	public SortedSet<T> headSet(T arg0) {
		throw new RuntimeException("Not yet implemented");
	}

	public T last() {
		return this.m_items.get(this.m_items.size() - 1);
	}

	public SortedSet<T> subSet(T arg0, T arg1) {
		throw new RuntimeException("Not yet implemented");
	}

	public SortedSet<T> tailSet(T arg0) {
		throw new RuntimeException("Not yet implemented");
	}

	public boolean add(T elem) {
		boolean result = this.m_set.add(elem);
		if (result) {
			int index = Collections.binarySearch(this.m_items, elem, this.m_comparator);
			index = index < 0
			        ? -index - 1
			        : index;
			if (index >= this.m_items.size()) {
				this.m_items.add(elem);
			}
			else {
				this.m_items.add(index, elem);
			}
		}

		return result;
	}

	public boolean addAll(Collection<? extends T> elems) {
		boolean result = false;

		for (T elem : elems) {
			result |= add(elem);
		}

		return result;
	}

	public void clear() {
		this.m_set.clear();
		this.m_items.clear();
	}

	public boolean contains(Object elem) {
		return this.m_set.contains(elem);
	}

	public boolean containsAll(Collection<?> elems) {
		return this.m_set.contains(elems);
	}

	public boolean isEmpty() {
		return this.m_set.isEmpty();
	}

	public Iterator<T> iterator() {
		return this.m_items.iterator();
	}

	public boolean remove(Object elem) {
		boolean result = this.m_set.remove(elem);
		T typedElem = (T) elem;

		if (result) {
			int index = Collections.binarySearch(this.m_items, typedElem, this.m_comparator);
			int remIndex1;

			if ((remIndex1 = this.searchAmidEquals(typedElem, index, 1)) > 0) {
				this.m_items.remove(remIndex1);
			}
			else {
				this.m_items.remove(this.searchAmidEquals(typedElem, index, -1));
			}
		}

		return result;
	}

	private int searchAmidEquals(T elem, int index, int inc) {
		int hashCode = elem.hashCode();

		for (T current; index < this.m_items.size() && this.m_comparator.compare(current = this.m_items.get(index), elem) == 0; index += inc) {
			if (hashCode == current.hashCode()) {
				return index;
			}
		}

		return -1;
	}

	public boolean removeAll(Collection<?> elems) {
		boolean result = false;

		Object elem;
		for (Iterator i$ = elems.iterator(); i$.hasNext(); result |= this.remove(elem)) {
			elem = i$.next();
		}

		return result;
	}

	public boolean retainAll(Collection<?> arg0) {
		throw new RuntimeException("Not yet implemented");
	}

	public int size() {
		return this.m_set.size();
	}

	public String toString() {
		return this.m_items.toString();
	}

	public Object[] toArray() {
		return this.m_items.toArray();
	}

	public <T> T[] toArray(T[] arr) {
		int i = this.size();
		if (arr.length < i) {
			arr = (T[]) (Array.newInstance(arr.getClass().getComponentType(), i));
		}

		Iterator iterator1 = this.iterator();
		Object[] arr1 = arr;

		for (int j = 0; j < i; ++j) {
			arr1[j] = iterator1.next();
		}

		if (arr.length > i) {
			arr[i] = null;
		}

		return arr;
	}

	class ComparatorImpl implements Comparator<T> {

		public int compare(T first, T second) {
			return ((Comparable<T>) first).compareTo(second);
		}
	}
}
