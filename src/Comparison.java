import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Comparison {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Test {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Before {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface After {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Finally {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Ignore {}

	@Ignore
	public static void main(String[] args) {
		Comparison c = new Comparison();

		Method beforeMethod = null;
		Method afterMethod = null;
		Method[] methods = c.getClass().getDeclaredMethods();
		Method finalMethod = null;

		ArrayList<Method> testMethods = new ArrayList<>();

		for (Method m : methods) {
			if (m.isAnnotationPresent(Before.class)) {
				beforeMethod = m;
			} else if (m.isAnnotationPresent(After.class)) {
				afterMethod = m;
			} else if (m.isAnnotationPresent(Finally.class)) {
				finalMethod = m;
			} else if (m.isAnnotationPresent(Test.class)) {
				testMethods.add(m);
			}
		}

		try {
			repeatTests(c, testMethods, beforeMethod, afterMethod);

			if (finalMethod != null) {
				finalMethod.invoke(c);
			}

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private Queue<Object> ll;
	private Queue<Object> cq;

	// LL then CQ
	private static List<Long> results = new ArrayList<>();
	private static List<String> testNames = new ArrayList<>();

	private static final int numTests = 15;

	@Ignore
	private static void repeatTests(Comparison c, ArrayList<Method> testMethods, Method beforeMethod, Method afterMethod) {
		for (Method m : testMethods) {
			for (int i = 0; i < numTests; i++) {
				try {
					if (beforeMethod != null) {
						beforeMethod.invoke(c);
					}
					m.invoke(c);
					if (afterMethod != null) {
						afterMethod.invoke(c);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}

		}
	}

	@Before
	private void before() {
		ll = new LinkedList<>();
		cq = new CircularArrayQueue<>();
	}

	private interface TestCase<T> {
		public void go(Queue<T> t);
	}

	@Ignore
	private void testEach(TestCase<Object> t) {
		t.go(ll);
		t.go(cq);
		String name = Thread.currentThread().getStackTrace()[2].getMethodName();
		testNames.add(name);
		System.out.println(name + ": Linked list: " + results.get(results.size() - 2) + ", Circular queue: "
				+ results.get(results.size() - 1));
	}

	@Finally
	private void printout() {
		System.out.println("=======================================");
		System.out.println("In summary:");
		for (int i = 0; i < results.size(); i += 2) {
			double diff = results.get(i) - results.get(i + 1);
			double avg = (results.get(i) + results.get(i + 1)) / 2;
			double f = (diff / avg) * 100;
			double temp = f * 1000;
			double val = (int) temp;
			System.out.println(testNames.get(i / 2) + ":	 Linked list was: " + (val / 1000) + "% slower");
		}

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File("test.csv")));

			bw.write("Test,Linked list, Circular Array Queue\n");

			for (int i = 0; i < results.size(); i += 2) {
				bw.write(testNames.get(i / 2) + "," + results.get(i) + "," + results.get(i + 1));
				bw.write("\n");
			}

		} catch (IOException e) {} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void testAdd100000() {
		testEach((q) -> {
			long beforeTime = System.nanoTime();
			for (Integer i = 0; i < 100000; i++) {
				q.add(i);
			}
			results.add(System.nanoTime() - beforeTime);
		});
	}

	@Test
	public void testClear() {

		testEach((q) -> {
			long beforeTime = System.nanoTime();
			q.clear();
			results.add(System.nanoTime() - beforeTime);
		});

	}

	@Test
	public void testMultipleAddRemove() {
		long modCount = 1000000000;

		testEach((q) -> {
			long beforeTime = System.nanoTime();
			int mods = 0;
			int max = Integer.MAX_VALUE / 3;
			Random rand = new Random();
			while (mods < modCount) {
				float next = rand.nextFloat();
				if (q.size() == 0) {
					next = 0.9f;
				} else if (q.size() >= max) {
					next = 0.2f;
				}
				if (next >= 0.75f) {
					q.add(mods);
				} else {
					q.remove();
				}
				mods++;
			}
			results.add(System.nanoTime() - beforeTime);
		});

	}

	@Test
	public void testStrings() {
		long modCount = 10000000;
		long maxCount = Integer.MAX_VALUE / 3;
		List<ExpensiveObject> pregenerated = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			pregenerated.add(new ExpensiveObject());
		}

		testEach((q) -> {
			long beforeTime = System.nanoTime();
			Random rand = new Random();
			long mods = 0;
			while (mods < modCount) {
				float next = rand.nextFloat();

				if (q.size() == 0) {
					next = 0.8f;
				} else if (q.size() >= maxCount) {
					next = 0f;
				}

				if (next >= 0.5) {
					q.add(pregenerated.get(rand.nextInt(pregenerated.size())));
				} else {
					q.remove();
				}
				mods++;
			}
			results.add(System.nanoTime() - beforeTime);
		});
	}

	@Test
	public void testRetainAll() {
		Collection<Integer> toKeep = new ArrayList<>();
		Random rand = new Random();
		for (int i = 0; i < 1500; i++) {
			toKeep.add(rand.nextInt(1500));
		}
		testEach((q) -> {
			for (int i = 0; i < 1500; i++) {
				q.add(i);
			}
			long beforeTime = System.nanoTime();
			q.retainAll(toKeep);
			results.add(System.nanoTime() - beforeTime);
		});
	}

	@Test
	public void testIterator() {
		testEach((q) -> {
			for (int i = 0; i < 10000000; i++) {
				q.add(i);
			}

			Iterator<Object> it = q.iterator();
			long beforeTime = System.nanoTime();
			while (it.hasNext()) {
				it.next();
			}
			results.add(System.nanoTime() - beforeTime);
		});
	}

	@Test
	public void testAddAll() {
		Collection<Integer> toAdd = new ArrayList<>();
		for (int i = 0; i < 1000000; i++) {
			toAdd.add(i);
		}

		testEach((q) -> {
			long beforeTime = System.nanoTime();
			q.addAll(toAdd);
			results.add(System.nanoTime() - beforeTime);
		});
	}

	private static class ExpensiveObject {
		private String[] junkData;

		private ExpensiveObject() {
			junkData = new String[1000];
			Random rand = new Random();
			for (int i = 0; i < 1000; i++) {
				junkData[i] = "" + rand.nextInt();
			}
		}
	}
}
