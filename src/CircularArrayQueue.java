import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
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
 * Resizable circular array queue implementation of the {@code List} interface.
 * Implements all optional methods.
 *
 * Provides constant time access for {@code size}, {@code add} (although this is
 * amortized constant time), {@code remove} operations.
 *
 * @author David Brown
 * @see Queue
 * @see Collection
 * @param <T>
 *            Type of object to be stored in the CircularArrayQueue
 */
public class CircularArrayQueue<T> implements Queue<T>, Serializable, Cloneable, Iterable<T> {

	/**
	 * Generated serial ID for serialization of this collection.
	 */
	private static final long serialVersionUID = -2675072648272502131L;

	/**
	 * The default capacity of the queue when none is provided by the user.
	 */
	private static final int DEFAULT_CAPACITY = 10;

	/**
	 * Underlying array storing elements which have been added to the queue.
	 */
	private T[] elements;

	/**
	 * Current capacity of the queue (equal to the size of the elements array,
	 * not necessarily equal to the number of visible elements in the queue to
	 * the user).
	 */
	private int capacity;

	/**
	 * Location of the tail pointer, will point to the same element as the head
	 * pointer if the size is 0, or if the number of elements in the elements,
	 * array is the same as the capacity of the elements array. Points to the
	 * element after the last accessible element in the queue, i.e. the one that
	 * will be replaced next upon an {@code add(T e)} method call.
	 */
	private int tail = 0;

	/**
	 * Current location of the head pointer. Points to the next element in the
	 * array to be removed, so the first element which is accessible via the
	 * normal {@code remove} method from the queue interface.
	 */
	private int head = 0;

	/**
	 * Current size or number of elements in the queue. Equal to the number of
	 * accessible elements, not the size of the underlying array
	 */
	private int size = 0;

	/**
	 * Number of modifications made to the elements in this queue. For use when
	 * checking for {@code ConcurrentModificationException}s to be thrown
	 */
	private int mods = 0;

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

	/**
	 * Used to specify a collection to add to the queue initially, along with an
	 * initial capacity. The inial capacity is not allowed to be smaller than
	 * the given collection. If the collection is null, a null pointer exception
	 * is thrown. If these two conditions are not met, the queue is created with
	 * the specified default capacity, and the elements from the given
	 * collection are added to the queue.
	 *
	 * @param c
	 *            The collection from which to copy all the elements from
	 *            initially.
	 * @param initialCapacity
	 *            The initial capacity of which to start the queue with. Useful
	 *            if the user knows the size of the queue is likely to grow to a
	 *            certain size.
	 */
	@SuppressWarnings("unchecked")
	public CircularArrayQueue(Collection<? extends T> c, int initialCapacity) {
		if (c == null) {
			throw new NullPointerException();
		}
		if (initialCapacity < c.size()) {
			throw new IllegalArgumentException();
		}
		elements = (T[]) new Object[initialCapacity];
		addAll(c);
	}

	/**
	 * Constructor to create a queue with a given collection of elements. Given
	 * collection must not be null. The inital size of the queue is made to be
	 * equal to the size of the given collection.
	 *
	 * @param c
	 *            The collection from which to add all elements from initally
	 *            upon creating a queue.
	 */
	@SuppressWarnings("unchecked")
	public CircularArrayQueue(Collection<? extends T> c) {
		if (c == null) {
			throw new NullPointerException();
		}
		elements = (T[]) new Object[c.size()];
		addAll(c);
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
	 * Used to access the current capacity (size of underlying array).
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
	 * collection. If this iterator is created, and then the underlying
	 * collection modified not through the iterator's methods, a
	 * {@code ConcurrentModificationException} will be thrown, since we cannot
	 * guarantee that the iterator is going to behave properly once the queue
	 * has been modified not through these methods.
	 */
	private class It implements Iterator<T> {

		/**
		 * Local pointer for the iterator, initialised to the head of the
		 * underlying queue. Generally points to the next element to be
		 * accessed, unless {@code next} has not yet been called, in which case
		 * it will point to the head.
		 */
		private int p = head;

		/**
		 * Flag to check if next has been called or not, used by the
		 * {@code remove} method to ensure there is an element to remove.
		 */
		private boolean calledNext = false;

		/**
		 * The number of times next has been called by this iterator. Used by
		 * {@code hasNext} to check if there are more elements. (Such as when
		 * the queue's number of elements is the same as the underlying array's
		 * capacity. In this case, the tail and head pointers will point at the
		 * same element, but there is still elements to be accessed upon
		 * initialising the iterator.
		 */
		private int nextCount = 0;

		/**
		 * Expected modification count. If this is not equal to the underlying
		 * queue's number of modifications, then we know that the queue has been
		 * modified via the queue's methods, not the iterator's methods. This
		 * iterator does not increment the queue's modification count upon
		 * modification of the queue.
		 */
		private int xpm = mods;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return size == 0 ? false : p == tail ? nextCount == 0 : true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			if (xpm != mods) {
				throw new ConcurrentModificationException();
			}
			calledNext = true;
			T o = elements[p++];
			nextCount++;
			if (p == capacity && tail != capacity) {
				p = 0;
			}
			return o;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			if (!calledNext) {
				throw new IllegalStateException();
			}
			if (xpm != mods) {
				throw new ConcurrentModificationException();
			}
			int prev = p - 1 < 0 ? capacity - 1 : p - 1;
			if (head <= tail) {
				System.arraycopy(elements, p, elements, prev, tail - prev);
			} else {
				System.arraycopy(elements, p, elements, prev, capacity - prev);
				elements[capacity - 1] = elements[0];
				System.arraycopy(elements, 1, elements, 0, tail);
			}
			tail = tail - 1 < 0 ? capacity - 1 : tail - 1;
			p = p - 1 < 0 ? capacity - 1 : p - 1;
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
		if (a == null) {
			throw new NullPointerException();
		}
		if (a.length < size) {
			a = (E[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
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
					mods++;
					return true;
				}
			}
		} else {
			while (it.hasNext()) {
				if (o.equals(it.next())) {
					it.remove();
					mods++;
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
		final int oldTail = tail;
		size += cs;
		tail += cs;
		if (tail > capacity) {
			tail = cs - (capacity - oldTail);
		}
		if (oldTail != tail) {
			mods++;
		}
		return oldTail != tail;
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
	 * @param newCap
	 *            The new capacity with which to resize the underlying array
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
		return compareRemove(c, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return compareRemove(c, false);
	}

	/**
	 * Used to remove elements in this queue based on the elements in the given
	 * collection. Iterates through reachable elements in the queue, and checks
	 * if the given collection also has this element. If it does, and the flag
	 * is true, this current element from the queue is removed, if the flag is
	 * false, then it is kept. The reverse is also true. If the collection does
	 * not have this element, and the flag is true, the element is kept,
	 * otherwise if the collection does not have this element, and the flag is
	 * false, this element is removed.
	 *
	 * @param c
	 *            The collection from which to compare this queue's elements.
	 * @param mod
	 *            The flag to specify removal of an element of not based on if
	 *            the given collection has the same element
	 * @return Returns true if the queue was modified, false if it was not
	 */
	private boolean compareRemove(Collection<?> c, boolean mod) {
		if (c == null) {
			throw new NullPointerException();
		}
		int h = head, cnt = 0, p = head;
		for (; cnt < size; h++, cnt++) {
			if (h == capacity) {
				h = 0;
			}
			if (p == capacity) {
				p = 0;
			}
			if (c.contains(elements[h]) != mod) {
				elements[p++] = elements[h];
			}
		}
		boolean changed = tail != p;
		size -= (p <= tail ? tail - p : capacity - p + tail);
		tail = p;
		if (changed) {
			mods++;
		}
		return changed;
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
		mods++;
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
		mods++;
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
		mods++;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
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
