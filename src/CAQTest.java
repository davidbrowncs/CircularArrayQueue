import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class CAQTest
{
	CircularArrayQueue<Integer> queue;
	ArrayList<Integer> test;

	@Before
	public void setup()
	{
		queue = new CircularArrayQueue<Integer>();
		test = new ArrayList<Integer>();
	}

	@Test
	public void ensureCapacity()
	{
		queue = new CircularArrayQueue<Integer>(40);
		assertEquals(40, queue.capacity());
	}

	@Test
	public void add()
	{
		queue.add(1);
		assertEquals(1, queue.size());
		assertTrue(queue.contains(1));
	}

	@Test
	public void empty()
	{
		assertTrue(queue.isEmpty());
		queue.add(10);
		assertEquals(1, queue.size());
		assertTrue(!queue.isEmpty());
	}

	@Test
	public void testRemove()
	{
		queue.add(10);
		queue.add(20);
		int elem = queue.remove();
		assertEquals(elem, 10);
		assertEquals(1, queue.size());
	}

	@Test
	public void testAddMultiple()
	{
		for (int i = 0; i < 100; i++)
		{
			queue.add(i);
		}

		assertEquals(100, queue.size());
		for (int i = 0; i < test.size(); i++)
		{
			assertTrue(queue.contains(i));
		}
	}

	@Test
	public void testCapacity()
	{
		for (int i = 0; i < 10; i++)
		{
			queue.add(i);
			assertEquals(10, queue.capacity());
		}

		queue.add(10);
		assertEquals(20, queue.capacity());
	}

	@Test
	public void yetAnotherIteratorTest()
	{
		for (int i = 0; i < 100000; i++)
		{
			queue.add(i);
			test.add(i);
		}

		Iterator<Integer> i = test.iterator();
		Iterator<Integer> it = queue.iterator();

		Random rand = new Random();
		while (i.hasNext())
		{
			assertEquals(i.next(), it.next());

			if (rand.nextFloat() >= 0.5 && i.hasNext())
			{
				i.remove();
				it.remove();
			}

			assertEquals(i.hasNext(), it.hasNext());
			assertEquals(test.size(), queue.size());
			assertTrue(Arrays.equals(test.toArray(), queue.toArray()));
		}

	}

	@Test
	public void testMultipleAddRemove()
	{
		Random rand = new Random();
		int size = 0;
		for (int i = 0; i < 10000; i++)
		{
			float next = rand.nextFloat();
			if (size == 0)
			{
				next = 0.7f;
			}
			if (next >= 0.5)
			{
				queue.add(i);
				test.add(i);
				size++;
			} else
			{
				queue.remove();
				test.remove(0);
				size--;
			}
		}

		assertEquals(size, queue.size());
		assertEquals(test.size(), queue.size());
		for (Integer i : test)
		{
			assertTrue(queue.contains(i));
		}
	}

	@Test
	public void testLoadsOfAdding()
	{
		Random rand = new Random();
		int size = 0;
		for (int i = 0; i < 10000000; i++)
		{
			int next = rand.nextInt(1000);
			queue.add(next);
			test.add(next);
			size++;
			assertEquals(size, queue.size());
		}

		assertTrue(test.containsAll(queue));
		assertTrue(queue.containsAll(test));
	}

	@Test
	public void testTypeChecking()
	{
		queue.add(null);
		assertTrue(queue.contains(null));
	}

	@Test(expected = NoSuchElementException.class)
	public void testEmptyRemove()
	{
		queue.remove();
	}

	@Test(expected = NoSuchElementException.class)
	public void testElement()
	{
		queue.element();
	}

	@Test
	public void testPeek()
	{
		assertEquals(null, queue.peek());
	}

	@Test
	public void algorithmicsTestSimple()
	{
		checkSize(0, queue);
		queue.add(3);
		checkSize(1, queue);
		try
		{
			assertEquals("Dequeue returns wrong element", 3, (int) queue.remove());
		} catch (NoSuchElementException e)
		{
			throw e;
		}
		checkSize(0, queue);
	}

	private void checkSize(int length, Queue<?> queue)
	{
		assertEquals("Queue has wrong number of elements", length, queue.size());
		if (length == 0)
		{
			assertTrue("Queue should be empty", queue.isEmpty());
		} else
		{
			assertTrue("Queue should not be empty", !queue.isEmpty());
		}
	}

	@Test
	public void algorithmicsTestMultiInput()
	{
		for (int i = 0; i < 1000; ++i)
		{
			int r = (int) Math.round(Math.random());
			checkSize(0, queue);
			queue.add(r);
			checkSize(1, queue);
			assertEquals("Dequeue returns wrong element", r, (int) queue.remove());
		}
	}

	@Test
	public void algorithmicsTestManyEnqueueDequeue()
	{
		int cnt = 0;
		for (int i = 0; i < 100000; ++i)
		{
			if (Math.random() > 0.5)
			{
				queue.add(i);
				cnt++;
			} else
			{
				if (!queue.isEmpty())
				{
					queue.remove();
					cnt--;
				}
			}
			assertEquals("Correct number of items", cnt, queue.size());
		}
	}

	@Test
	public void algorithmicsTestLargeQueue()
	{
		int[] r = new int[1000];
		for (int i = 0; i < r.length; ++i)
		{
			r[i] = (int) Math.round(Math.random());
			checkSize(i, queue);
			queue.add(r[i]);
		}
		for (int i = 0; i < r.length; ++i)
		{
			assertEquals("Dequeue returns wrong element", r[i], (int) queue.remove());
			checkSize(r.length - i - 1, queue);
		}
		for (int i = 0; i < r.length; ++i)
		{
			r[i] = (int) Math.round(Math.random());
			checkSize(i, queue);
			queue.add(r[i]);
		}
		for (int i = 0; i < r.length; ++i)
		{
			assertEquals("Dequeue returns wrong element", r[i], (int) queue.remove());
			checkSize(r.length - i - 1, queue);
		}
	}

	@Test
	public void algorithmicsTestThrows()
	{
		int[] r = new int[1000];
		for (int i = 0; i < r.length; ++i)
		{
			r[i] = (int) Math.round(Math.random());
			checkSize(i, queue);
			queue.add(r[i]);
		}
		for (int i = 0; i < r.length; ++i)
		{
			assertEquals("Dequeue returns wrong element", r[i], (int) queue.remove());
			checkSize(r.length - i - 1, queue);
		}
		boolean throwsCorrectly = false;
		try
		{
			queue.remove();
		} catch (NoSuchElementException e)
		{
			throwsCorrectly = true;
		}
		assertTrue("Throws when dequeuing empty queue", throwsCorrectly);
	}

	@Test
	public void algorithmicsTestResize()
	{
		assertTrue("Initial capacity too large", queue.capacity() - queue.size() <= 1024);
		for (int i = 0; i < 1000; ++i)
		{
			queue.add(i);
		}
		int currentCapacity = queue.capacity() - queue.size();
		while (currentCapacity > 0)
		{
			queue.add(9);
			currentCapacity--;
			assertEquals("Array size should not change", currentCapacity, queue.capacity() - queue.size());
		}
		assertTrue("Should have reached capacity", queue.capacity() - queue.size() == 0);
		queue.add(42);
		assertTrue("Should have resized array", currentCapacity < queue.capacity() - queue.size());
		currentCapacity = queue.capacity() - queue.size();
		for (int i = 0; i < 100; ++i)
		{
			queue.add(i);
			currentCapacity--;
			assertEquals("Resizing too often (inefficient)", currentCapacity, queue.capacity() - queue.size());
		}
	}

	@Test
	public void testIterator()
	{
		CircularArrayQueue<String> queue = new CircularArrayQueue<>();
		ArrayList<String> test = new ArrayList<>();

		Random rand = new Random();

		int size = 0;
		for (int i = 0; i < 1000; i++)
		{
			float next = rand.nextFloat();
			if (size == 0)
			{
				next = 0.9f;
			}

			if (next >= 0.8f)
			{
				String nextString = UUID.randomUUID().toString();
				test.add(nextString);
				queue.add(nextString);
			} else
			{
				test.remove(0);
				queue.remove();
			}
		}

		Iterator<String> qi = queue.iterator();
		Iterator<String> ti = test.iterator();

		String elem = null;
		while (ti.hasNext())
		{
			elem = ti.next();
			assertEquals(elem, qi.next());
		}

		assertEquals(ti.hasNext(), qi.hasNext());
	}

	@Test
	public void checkToArray()
	{
		addRandomInts();

		Object[] arr1 = test.toArray();
		Object[] arr2 = queue.toArray();

		assertTrue(Arrays.equals(arr1, arr2));

		queue = new CircularArrayQueue<>();
		test = new ArrayList<>();

		for (int i = 0; i < 1000; i++)
		{
			queue.add(i);
			test.add(i);
		}

		arr1 = test.toArray();
		arr2 = queue.toArray();

		assertTrue(Arrays.equals(arr1, arr2));
	}

	@Test
	public void testConstructor()
	{
		for (int i = 0; i < 1000; i++)
		{
			test.add(i);
		}

		queue = new CircularArrayQueue<>(test);
		assertTrue(Arrays.equals(test.toArray(), queue.toArray()));
	}

	private void addRandomInts()
	{
		Random rand = new Random(System.currentTimeMillis());

		int size = 0;
		for (int i = 0; i < 1000; i++)
		{
			float next = rand.nextFloat();
			if (size == 0)
			{
				next = 0.9f;
			}

			if (next >= 0.8f)
			{
				int nextInt = rand.nextInt(1000);
				test.add(nextInt);
				queue.add(nextInt);
				size++;
			} else
			{
				test.remove(0);
				queue.remove();
				size--;
			}
		}
	}

	@Test
	public void checkToArrayParameterized()
	{
		addRandomInts();

		Object[] arr1 = test.toArray(new Object[10]);
		Object[] arr2 = queue.toArray(new Object[10]);

		assertTrue(Arrays.equals(arr1, arr2));

		queue = new CircularArrayQueue<>();
		test = new ArrayList<>();

		for (int i = 0; i < 1500; i++)
		{
			queue.add(i);
			test.add(i);
		}

		arr1 = test.toArray(new Object[0]);
		arr2 = queue.toArray(new Object[0]);

		assertTrue(Arrays.equals(arr1, arr2));

		arr1 = test.toArray(new Object[1502]);
		arr2 = queue.toArray(new Object[1502]);

		assertTrue(arr2[1500] == null);

	}

	@Test
	public void testAddAll()
	{
		ArrayList<Integer> toAdd = new ArrayList<>();

		for (int i = 0; i < 100; i++)
		{
			toAdd.add(i);
		}

		queue.addAll(toAdd);

		assertEquals(100, queue.size());

		assertTrue(queue.containsAll(toAdd));
		assertTrue(toAdd.containsAll(queue));
	}

	@Test
	public void testRemoveAll()
	{
		ArrayList<Integer> toRemove = new ArrayList<>();
		Random rand = new Random();

		for (int i = 0; i < 100; i++)
		{
			toRemove.add(rand.nextInt(100));
			test.add(i);
			queue.add(i);
		}

		queue.removeAll(toRemove);
		test.removeAll(toRemove);

		assertTrue(test.containsAll(queue));
		assertTrue(queue.containsAll(test));
		assertEquals(test.size(), queue.size());
	}

	@Test
	public void testMultipleRemoveAndSize()
	{
		Queue<Integer> test = new LinkedList<>();
		Random rand = new Random();

		int size = 0;
		for (int i = 0; i < 100000; i++)
		{
			float next = rand.nextFloat();
			if (size == 0)
			{
				next = 0.9f;
			}

			if (next >= 0.8f)
			{
				int nextInt = rand.nextInt(1000000);
				test.add(nextInt);
				queue.add(nextInt);
			} else
			{
				test.remove(0);
				queue.remove();
			}
		}

		assertEquals(test.size(), queue.size());
	}

	@Test
	public void testRemoveObjects()
	{
		ArrayList<String> test = new ArrayList<String>(150);
		Queue<String> queue = new CircularArrayQueue<String>(150);

		Random rand = new Random();

		int size = 0;
		for (int i = 0; i < 75; i++)
		{
			float next = rand.nextFloat();
			if (size == 0)
			{
				next = 0.9f;
			}

			if (next >= 0.2f)
			{
				String ns = "" + rand.nextInt();
				test.add(ns);
				queue.add(ns);
				size++;
			} else
			{
				String toRemove = test.get(rand.nextInt(test.size()));
				test.remove(toRemove);
				queue.remove(toRemove);
				size--;
			}
		}
		assertTrue(test.containsAll(queue));
		assertTrue(queue.containsAll(test));
	}

	@Test(expected = NullPointerException.class)
	public void testNullCollection()
	{
		queue.removeAll(null);
	}

	@Test(expected = NullPointerException.class)
	public void testRemoveAllNull()
	{
		queue.addAll(null);
	}

	@Test(expected = NullPointerException.class)
	public void testContainsAllNull()
	{
		queue.containsAll(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testNotSupported()
	{
		queue.retainAll(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorArguments()
	{
		queue = new CircularArrayQueue<>(-100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorIllegal()
	{
		test = new ArrayList<>();
		for (int i = 0; i < 100; i++)
		{
			test.add(i);
		}
		queue = new CircularArrayQueue<>(test, 99);
	}

	private void testElementsEqual()
	{
		assertTrue(Arrays.equals(test.toArray(), queue.toArray()));
		assertEquals(test.size(), queue.size());
	}

	@Test
	public void testEmptyIterator()
	{
		Iterator<Integer> it = queue.iterator();
		assertFalse(it.hasNext());
	}

	@Test
	public void testHasNext()
	{
		queue.add(1);
		Iterator<Integer> it = queue.iterator();
		for (int i = 0; i < 10; i++)
		{
			assertTrue(it.hasNext());
		}
	}

	@Test(expected = NoSuchElementException.class)
	public void testEmptyExceptionIterator()
	{
		Iterator<Integer> it = queue.iterator();
		it.next();
	}

	@Test
	public void multipleIteratorOperations()
	{
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		for (int i = 0; i < 1000000; i++)
		{
			float next = rand.nextFloat();

			if (test.size() == 0)
			{
				next = 0.9f;
			}

			if (next >= 0.8f)
			{
				queue.add(i);
				test.add(i);
			} else
			{

				test.remove(0);
				queue.remove();
			}

			assertTrue(Arrays.equals(queue.toArray(), test.toArray()));
			assertTrue(Arrays.equals(queue.toArray(new Object[0]), test.toArray(new Object[0])));
		}

		queue = new CircularArrayQueue<>();
		test = new ArrayList<>();

		int size = 0;
		for (int i = 0; i < 100000; i++)
		{
			Iterator<Integer> it = test.iterator();
			Iterator<Integer> iter = queue.iterator();

			assertEquals(it.hasNext(), iter.hasNext());

			if (size == 0)
			{
				assertFalse(iter.hasNext());
			}

			float next = rand.nextFloat();
			if (size == 0)
			{
				next = 0.4f;
			}

			if (next >= 0.3f)
			{
				queue.add(i);
				test.add(i);
				size++;
				it = test.iterator();
				iter = queue.iterator();
				assertEquals(it.hasNext(), iter.hasNext());
			} else
			{
				queue.remove();
				test.remove(0);
				it = test.iterator();
				iter = queue.iterator();
				size--;
				assertEquals(it.hasNext(), iter.hasNext());
			}
		}
	}

	@Test
	public void testAddAndRemove()
	{
		queue.add(1);
		Iterator<Integer> it = queue.iterator();
		assertTrue(it.hasNext());
		assertEquals(1, (int) it.next());
		for (int i = 0; i < 10; i++)
		{
			assertFalse(it.hasNext());
		}
	}

	@Test
	public void testIteratorRemove()
	{
		long seed = System.currentTimeMillis();
		Random rand = new Random(seed);
		int size = 0;
		for (int i = 0; i < 100000; i++)
		{
			float next = rand.nextFloat();

			if (size == 0)
			{
				next = 0.7f;
			}

			if (next >= 0.2f)
			{
				test.add(i);
				queue.add(i);
				size++;
			} else
			{
				test.remove(0);
				queue.remove();
				size--;
			}
		}

		Iterator<Integer> it = queue.iterator();
		Iterator<Integer> i = test.iterator();

		boolean calledNext = false;
		while (i.hasNext())
		{
			float next = rand.nextFloat();

			if (next < 0.4f && !calledNext)
			{
				next = 0.8f;
			}
			if (next >= 0.6f)
			{
				i.next();
				it.next();
				calledNext = true;

				assertEquals(i.hasNext(), it.hasNext());
			} else
			{

				if (calledNext)
				{
					i.remove();
					it.remove();
				}

				assertEquals(i.hasNext(), it.hasNext());
				calledNext = false;
			}
		}

	}

	@Test
	public void testAllTogether()
	{
		addRandomInts();
		testElementsEqual();

		Collection<Integer> c = new HashSet<Integer>();
		Random rand = new Random();
		for (int i = 0; i < 1500; i++)
		{
			c.add(rand.nextInt());
		}

		test.addAll(c);
		queue.addAll(c);

		testElementsEqual();

		c = new TreeSet<Integer>();
		for (int i = 0; i < 1500; i++)
		{
			c.add(rand.nextInt(1000));
		}

		test.removeAll(c);
		queue.removeAll(c);

		addRandomInts();
		testElementsEqual();

		queue = new CircularArrayQueue<>(c);
		test = new ArrayList<>(c);
		testElementsEqual();
		addRandomInts();
		testElementsEqual();

		queue = new CircularArrayQueue<>(100);
		test = new ArrayList<>(100);

		addRandomInts();
		testElementsEqual();
	}

	@Test
	public void testPeekNonNull()
	{
		for (int i = 0; i < 11; i++)
		{
			queue.add(i);
		}
		for (int i = 0; i < 3; i++)
		{
			queue.remove();
		}
		assertEquals(3, (int) queue.peek());
	}

	@Test
	public void testElementNonNull()
	{
		for (int i = 0; i < 11; i++)
		{
			queue.add(i);
		}
		for (int i = 0; i < 3; i++)
		{
			queue.remove();
		}
		assertEquals(3, (int) queue.element());
	}

	@Test
	public void testCloneable()
	{
		CircularArrayQueue<Integer> clone = queue.clone();
		assertTrue(clone.equals(queue));
		assertFalse(clone == queue);
		assertTrue(Arrays.equals(clone.toArray(), queue.toArray()));
	}

}
