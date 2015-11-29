import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 *
 * Copyright (C) 2015 David Brown. Permission is granted to copy, distribute
 * and/or modify this document under the terms of the GNU Free Documentation
 * License, Version 1.3 or any later version published by the Free Software
 * Foundation; with no Invariant Sections, no Front-Cover Texts, and no
 * Back-Cover Texts. A copy of the license is included in the section entitled
 * "GNU Free Documentation License".
 *
 * @author David Brown
 *
 *         Resizable circular array queue implementation of the {@code List}
 *         interface. Implements all optional methods but the
 *         {@code retainAll(Collection c)} method from the List interface.
 *
 *         Provides constant time access for {@code size}, {@code add} (although
 *         this is amortized constant time), {@code remove} operations.
 *
 * @param <T>
 *            Type of object to be stored in the CircularArrayQueue
 */
public class CircularArrayQueue<T> implements Queue<T>, Serializable, Cloneable, Iterable<T> {

	/**
	 *
	 */
	private static final long serialVersionUID = -2675072648272502131L;

	/**
	 * The default capacity of the queue when none is provided by the user.
	 */
	private static final int DEFAULT_CAPACITY = 10;

	/**
	 * Array of elements added to the queue
	 */
	private T[] elements;

	/**
	 * Current capacity of the queue (equal to the size of the elements array)
	 */
	private int capacity;

	/**
	 * Location of the tail pointer
	 */
	private int tail = 0;

	/**
	 * Current location of the head pointer
	 */
	private int head = 0;

	/**
	 * Current size or number of elements in the queue
	 */
	private int size = 0;

	/**
	 * Used to create a queue with an initial capacity, useful if the user knows
	 * roughly what size queue will be required ahead of time to limit the
	 * number of resizing operations
	 *
	 * @param initialCapacity
	 *            The capacity with which to create the queue
	 */
	@SuppressWarnings("unchecked")
	public CircularArrayQueue(int initialCapacity) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException();
		}
		elements = (T[]) new Object[initialCapacity];
		capacity = initialCapacity;
	}

	@SuppressWarnings("unchecked")
	public CircularArrayQueue(Collection<? extends T> c, int initialCapacity) {
		if (c == null) {
			throw new NullPointerException();
		}
		int cs = c.size();
		if (initialCapacity < cs) {
			throw new IllegalArgumentException();
		}
		if ((size = cs) != 0) {
			capacity = initialCapacity;
			elements = (T[]) new Object[capacity];
			System.arraycopy(c.toArray(), 0, elements, 0, size);
			tail = size;
		} else {
			capacity = initialCapacity;
			elements = (T[]) new Object[initialCapacity];
		}
	}

	@SuppressWarnings("unchecked")
	public CircularArrayQueue(Collection<? extends T> c) {
		if (c == null) {
			throw new NullPointerException();
		}
		int cs = c.size();
		capacity = ((cs << 1) + cs) >> 1;
		if (capacity < 0) {
			throw new IllegalStateException();
		}
		if ((size = cs) != 0) {
			elements = (T[]) new Object[capacity];
			System.arraycopy(c.toArray(), 0, elements, 0, size);
			tail = size;
		} else {
			elements = (T[]) new Object[capacity];
		}
	}

	/**
	 * Default constructor used to create a queue when the size it may expand to
	 * is not well known beforehand
	 */
	@SuppressWarnings("unchecked")
	public CircularArrayQueue() {
		elements = (T[]) new Object[DEFAULT_CAPACITY];
		capacity = DEFAULT_CAPACITY;
	}

	/**
	 * Used to access the current capacity (size of element array)
	 *
	 * @return The current capacity of the queue
	 */
	public int capacity() {
		return capacity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		Iterator<T> it = iterator();
		if (o == null) {
			while (it.hasNext()) {
				if (it.next() == null) {
					return true;
				}
			}
		} else {
			while (it.hasNext()) {
				if (o.equals(it.next())) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Class used to implement the iterator for this CircularArrayQueue
	 * collection
	 */
	private class It implements Iterator<T> {
		private int p = head;

		private boolean calledNext = false;

		private int nextCount = 0;

		//@formatter:off
		@Override
		public boolean hasNext() {
			return 	capacity == 0	 ? false 		  :
					size == capacity ? nextCount == 0 :
					p != tail;
		}
		//@formatter:on

		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			calledNext = true;
			T o = elements[p++];
			nextCount++;
			if (p == capacity) {
				p = 0;
			}
			return o;
		}

		@Override
		public void remove() {
			if (!calledNext) {
				throw new IllegalStateException();
			}
			int prev = p - 1;
			if (head <= tail) {
				System.arraycopy(elements, p, elements, prev, tail - prev);
			} else {
				System.arraycopy(elements, p, elements, prev, capacity - prev);
				elements[capacity - 1] = elements[0];
				System.arraycopy(elements, 1, elements, 0, tail);
			}
			tail--;
			p--;
			if (tail < 0) {
				tail = capacity - 1;
			}
			if (p < 0) {
				p = capacity - 1;
			}
			size--;
			calledNext = false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new It();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		Object[] a = new Object[size];
		if (size == 0) {
			return a;
		}
		if (head < tail) {
			System.arraycopy(elements, head, a, 0, size);
		} else {
			System.arraycopy(elements, head, a, 0, capacity - head);
			System.arraycopy(elements, 0, a, capacity - head, tail);
		}
		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(java.lang.Object[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E> E[] toArray(E[] a) {
		if (a.length < size) {
			a = (E[]) new Object[size];
		}
		if (size == 0) {
			return a;
		}
		if (head < tail) {
			System.arraycopy(elements, head, a, 0, size);
		} else {
			System.arraycopy(elements, head, a, 0, capacity - head);
			System.arraycopy(elements, 0, a, capacity - head, tail);
		}
		if (a.length > size) {
			a[size] = null;
		}
		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		if (size == 0) {
			return false;
		}
		Iterator<T> it = iterator();
		if (o == null) {
			while (it.hasNext()) {
				if (it.next() == null) {
					it.remove();
					return true;
				}
			}
		} else {
			while (it.hasNext()) {
				if (o.equals(it.next())) {
					it.remove();
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		if (c == null) {
			throw new NullPointerException();
		}
		Iterator<?> i = c.iterator();
		while (i.hasNext()) {
			if (!contains(i.next())) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean addAll(Collection c) {
		if (c == null) {
			throw new NullPointerException();
		}
		int cs = c.size();
		if (capacity < size + cs) {
			resize(ensureCapacity((cs << 1) + capacity));
		}
		Object[] a = c.toArray();
		if (capacity - tail > cs) {
			System.arraycopy(a, 0, elements, tail, a.length);
		} else {
			System.arraycopy(a, 0, elements, tail, capacity - tail);
			System.arraycopy(a, capacity - tail, elements, capacity - tail, cs - (capacity - tail));
		}
		size += cs;
		int oldTail = tail;
		tail += cs;
		if (tail > capacity) {
			tail = cs - (capacity - oldTail);
		}
		return true;
	}

	/**
	 * Local operation used to resize the element array when the current
	 * capacity is reached, and trying to add a new element.
	 *
	 * Generates a new array with twice the current capacity, and iterates from
	 * the current head pointer, to the current tail pointer, adding all the
	 * elements to this new object array. The current element array is then set
	 * to this new one, and the relevant pointers are reset.
	 *
	 */
	@SuppressWarnings("unchecked")
	private void resize(int newCap) {
		if (newCap < 0) {
			throw new IllegalStateException();
		}
		T[] newElements = (T[]) new Object[newCap];
		if (head < tail) {
			System.arraycopy(elements, head, newElements, 0, size);
		} else {
			System.arraycopy(elements, head, newElements, 0, capacity - head);
			System.arraycopy(elements, 0, newElements, capacity - head, tail);
		}
		elements = newElements;
		head = 0;
		tail = size;
		capacity = newCap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		return rm(c, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return rm(c, false);
	}

	private boolean rm(Collection<?> c, boolean mod) {
		if (c == null) {
			throw new NullPointerException();
		}
		@SuppressWarnings("unchecked")
		T[] n = (T[]) new Object[capacity];
		int h = head, cnt = 0, p = 0, o = size;
		for (; cnt < o; h++, cnt++) {
			if (h == capacity) {
				h = 0;
			}
			if (c.contains(elements[h]) != mod) {
				n[p++] = elements[h];
			}
		}
		this.elements = n;
		head = 0;
		tail = p;
		size = p;
		return (size == o) != mod;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		// We don't even need to null any elements!
		tail = 0;
		head = 0;
		size = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Queue#add(java.lang.Object)
	 */
	@Override
	public boolean add(T e) {
		if (tail == capacity) {
			tail = 0;
		}
		if ((tail == head && size != 0) || capacity == 0) {
			resize(ensureCapacity(capacity << 1));
		}
		elements[tail++] = e;
		size++;
		return true;
	}

	private int ensureCapacity(int newCapacity) {
		return (newCapacity == 0) ? 1 : newCapacity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	@Override
	public boolean offer(T e) {
		return add(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Queue#remove()
	 */
	@Override
	public T remove() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		T o = elements[head++];
		if (head == capacity) {
			head = 0;
		}
		size--;
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Queue#poll()
	 */
	@Override
	public T poll() {
		return size == 0 ? null : remove();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Queue#element()
	 */
	@Override
	public T element() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return elements[head];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Queue#peek()
	 */
	@Override
	public T peek() {
		return size == 0 ? null : elements[head];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + capacity;
		result = prime * result + Arrays.hashCode(toArray());
		result = prime * result + head;
		result = prime * result + size;
		result = prime * result + tail;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CircularArrayQueue)) {
			return false;
		}
		CircularArrayQueue other = (CircularArrayQueue) obj;
		if (capacity != other.capacity) {
			return false;
		}
		if (!Arrays.equals(toArray(), other.toArray())) {
			return false;
		}
		if (head != other.head) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		if (tail != other.tail) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CircularArrayQueue<T> clone() {
		CircularArrayQueue<T> c;
		try {
			c = (CircularArrayQueue<T>) super.clone();
			c.elements = Arrays.copyOf(this.elements, capacity);
			return c;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}

	}
}
