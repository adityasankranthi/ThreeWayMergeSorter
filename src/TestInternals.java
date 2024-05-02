import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.uwm.cs.junit.LockedTestCase;
import edu.uwm.cs351.Ranking;
import edu.uwm.cs351.Ranking.Spy.Node;

public class TestInternals extends LockedTestCase {
	protected void assertException(Class<? extends Throwable> c, Runnable r) {
		try {
			r.run();
			assertFalse("Exception should have been thrown",true);
		} catch (Throwable ex) {
			assertTrue("should throw exception of " + c + ", not of " + ex.getClass(), c.isInstance(ex));
		}
	}
	
	protected int reports;
	protected Ranking<String> r;
	protected Ranking.Spy<String> spy;
	protected Iterator<Entry<Integer, String>> it;
	
	protected static Comparator<String> natural = Comparator.naturalOrder();
	protected static Comparator<String> insensitive = (s1,s2) -> s1.compareToIgnoreCase(s2);
	protected static Comparator<String> nondiscriminate = (s1,s2) -> 0;
	
	
	protected void assertReporting(boolean expected, Supplier<Boolean> test) {
		reports = 0;
		Consumer<String> savedReporter = spy.getReporter();
		try {
			spy.setReporter((String message) -> {
				++reports;
				if (message == null || message.trim().isEmpty()) {
					assertFalse("Uninformative report is not acceptable", true);
				}
				if (expected) {
					assertFalse("Reported error incorrectly: " + message, true);
				}
			});
			assertEquals(expected, test.get().booleanValue());
			if (!expected) {
				assertEquals("Expected exactly one invariant error to be reported", 1, reports);
			}
			spy.setReporter(null);
		} finally {
			spy.setReporter(savedReporter);
		}
	}
	
	protected void assertWellFormed(boolean expected, Ranking<String> r) {
		assertReporting(expected, () -> spy.wellFormed(r));
	}
	
	protected void assertWellFormed(boolean expected, Iterator<Entry<Integer, String>> it) {
		assertReporting(expected, () -> spy.wellFormed(it));
	}
	
	@Override // implementation
	protected void setUp() {
		spy = new Ranking.Spy<String>();
	}
	
	@SuppressWarnings("unchecked")
	protected <E> Node<E>[] makeArray(int n) {
		return (Node<E>[])new Node<?>[n];
	}
	
	/// Locked tests
	
	protected String asString(Supplier<?> supp) {
		try {
			return "" + supp.get();
		} catch(RuntimeException ex) {
			return ex.getClass().getSimpleName();
		}
	}
	
	protected String asString(Runnable r) {
		return asString(() -> { r.run(); return "void"; });
	}

	public void test() {
		r = spy.newInstance(null, 0); // ignore the values here
		// give result, or exception name or "void"
		assertEquals(Ts(285018639), asString(() -> spy.isSorted(r, natural, new String[] { "A", "B", "C" }, 0, 3)));
		assertEquals(Ts(1575211266), asString(() -> spy.isSorted(r, natural, new String[] { "Z", "I", "B" }, 0, 3)));
		assertEquals(Ts(793229578), asString(() -> spy.isSorted(r, nondiscriminate, new String[] { "Z", "I" }, 0, 3)));
		assertEquals(Ts(1441710105), asString(() -> spy.isSorted(r, nondiscriminate, new String[] { "Z", null, "B" }, 0, 3)));
		testcont(false);
	}
	
	private void testcont(boolean ignored) {
		r = spy.newInstance(null, 0); // ignore the values here
		String[] out = new String[5];
		// give result, or exception name or "void"
		assertEquals(Ts(658068755), asString(() -> spy.merge3(r, natural, 0, 2, 5, out, new String[] { "A", "D", "C", "B", "E" })));
		assertEquals(Ts(199105020), asString(() -> spy.merge3(r, natural, 0, 1, 5, out, new String[] { "A", "D", "C", "B", "E" })));
		assertEquals(Ts(134553338), asString(() -> spy.merge3(r, natural, 0, 2, 6, out, new String[] { "A", "D", "C", "B", "E" })));
	}

	
	/// test0xx: tests of isSorted
	
	public void test000() {
		String[] a = new String[8];
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, natural, a, 0, 0));
	}
	
	public void test001() {
		String[] a = new String[0];
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, natural, a, 0, 0));
	}
	
	public void test002() {
		String[] a = new String[0];
		r = spy.newInstance(null, 0);
		assertException(ArrayIndexOutOfBoundsException.class, () -> spy.isSorted(r, natural, a, 0, 1));
	}
	
	public void test003() {
		String[] a = new String[] { "Hello" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, natural, a, 0, 1));
	}
	
	public void test004() {
		String[] a = new String[1];
		r = spy.newInstance(null, 0);
		assertException(ArrayIndexOutOfBoundsException.class, () -> spy.isSorted(r, natural, a, -1, 0));
	}
	
	public void test005() {
		String[] a = new String[] { "Hello", "world" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r,  natural, a, 0, 1));
	}
	
	public void test006() {
		String[] a = new String[] { "yay", "world" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r,  natural, a, 1, 2));
	}

	public void test007() {
		String[] a = new String[] { "zoo", "world" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r,  natural, a, 2, 2));
	}
	
	public void test010() {
		String[] a = new String[] { "Hello", "world" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r,  natural, a, 0, 2));
	}

	public void test011() {
		String[] a = new String[] { "hello", "World" };
		r = spy.newInstance(null, 0);
		assertFalse(spy.isSorted(r,  natural, a, 0, 2));
	}

	public void test012() {
		String[] a = new String[] { "hello", "World" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r,  insensitive, a, 0, 2));
	}
	
	public void test013() {
		String[] a = new String[] { "yay", "World" };
		r = spy.newInstance(null, 0);
		assertFalse(spy.isSorted(r,  insensitive, a, 0, 2));
	}
	
	public void test014() {
		String[] a = new String[] { "yay", "World" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, nondiscriminate, a, 0, 2));
	}
	
	public void test015() {
		String[] a = new String[] { null, "World" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, nondiscriminate, a, 0, 2));
	}
	
	public void test016() {
		String[] a = new String[] { "yay", "World" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, (s1,s2) -> s2.compareTo(s1), a, 0, 2));
	}
	
	public void test017() {
		String[] a = new String[] { null, "hello", "world", null };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, natural, a, 1, 3));
	}
	
	public void test018() {
		String[] a = new String[] { "yay", "hello", "world", "bye" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, natural, a, 1, 3));
	}
	
	public void test019() {
		String[] a = new String[] { "yay", "bye", "hello", "world" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, natural, a, 2, 4));
	}
	
	public void test020() {
		String[] a = new String[] { "yay", "bye", "hello", "world" };
		r = spy.newInstance(null, 0);
		assertFalse(spy.isSorted(r, natural, a, 0, 3));
	}
	
	public void test021() {
		String[] a = new String[] { "yay", "bye", "hello", "world" };
		r = spy.newInstance(null, 0);
		assertException(ArrayIndexOutOfBoundsException.class, () -> spy.isSorted(r, natural, a, 1, 5));
	}
	
	public void test022() {
		String[] a = new String[] { "bye", "yay", "hello", "world" };
		r = spy.newInstance(null, 0);
		assertFalse(spy.isSorted(r, natural, a, 0, 4));
	}
	
	public void test023() {
		String[] a = new String[] { "bye", "yay", "hello", "world" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, nondiscriminate, a, 0, 4));
	}
	
	public void test024() {
		String[] a = new String[] { "bye", "yay", "hello", "world" };
		r = spy.newInstance(null, 0);
		assertException(ArrayIndexOutOfBoundsException.class, () -> spy.isSorted(r, nondiscriminate, a, 1, 5));
	}
	
	public void test025() {
		String[] a = new String[] { "a", "a", "A", "A" };
		r = spy.newInstance(null, 0);
		assertFalse(spy.isSorted(r, natural, a, 0, 4));
 	}
	
	public void test026() {
		String[] a = new String[] { "a", "a", "A", "A" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, insensitive, a, 0, 4));
	}
	
	public void test027() {
		String[] a = new String[] { "a", "a", "A", "A" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, (s1,s2) -> s2.compareTo(s1), a, 0, 4));
	}
	
	public void test028() {
		String[] a = new String[] { "a", null, null, "A" };
		r = spy.newInstance(null, 0);
		assertTrue(spy.isSorted(r, nondiscriminate, a, 0, 4));
	}
	
	public void test029() {
		String[] a = new String[] { "a", "b", "C", "A" };
		r = spy.newInstance(null, 0);
		assertFalse(spy.isSorted(r, insensitive, a, 0, 4));
	}
	
	
	/// test1xx: tests of merge3
	
	//public void merge3(Ranking<T> rank, Comparator<T> comp, int start1, int start2, int end3, T[] out, T[] in) {
	public void test100() {
		String[] a = new String[8];
		String[] b = new String[8];
		b[0] = "A";
		b[1] = "B";
		r = spy.newInstance(null, 0);	
		spy.merge3(r, nondiscriminate, 0, 0, 1, b, a);
		assertNull(b[0]);
		assertEquals("B", b[1]);
	}
	
	public void test101() {
		String[] a = new String[1];
		a[0] = "A";
		String[] b = new String[1];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 0, 1, b, a);
		assertEquals("A", b[0]);
	}
	
	public void test102() {
		String[] a = new String[2];
		a[0] = "A";
		String[] b = new String[2];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 0, 1, b, a);
		assertEquals("A", b[0]);
	}
	
	public void test103() {
		String[] a = new String[2];
		a[0] = "A";
		a[1] = "B";
		String[] b = new String[2];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 0, 1, b, a);
		assertEquals("A", b[0]);
		assertNull(b[1]);
	}
	
	public void test104() {
		String[] a = new String[2];
		a[0] = "A";
		a[1] = "B";
		String[] b = new String[2];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 2, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
	}
	
	public void test105() {
		String[] a = new String[2];
		a[0] = "B";
		a[1] = "A";
		String[] b = new String[2];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 2, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
	}
	
	public void test106() {
		String[] a = new String[3];
		a[0] = null;
		a[1] = "A";
		a[2] = "B";
		String[] b = new String[3];
		b[0] = "3";
		r = spy.newInstance(null, 0);	
		spy.merge3(r, nondiscriminate, 0, 1, 3, b, a);
		assertNull(b[0]);
		assertEquals("A", b[1]);
		assertEquals("B", b[2]);
	}
	
	public void test107() {
		String[] a = new String[3];
		a[0] = null;
		a[1] = "A";
		a[2] = "B";
		String[] b = new String[3];
		b[0] = "Z";
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 1, 2, 3, b, a);
		assertEquals("Z", b[0]);
		assertEquals("A", b[1]);
		assertEquals("B", b[2]);
	}
	
	public void test108() {
		String[] a = new String[3];
		a[0] = "A";
		a[1] = null;
		a[2] = "B";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, nondiscriminate, 0, 1, 3, b, a);
		assertEquals("A", b[0]);
		assertNull(b[1]);
		assertEquals("B", b[2]);
	}
	
	public void test109() {
		String[] a = new String[3];
		a[0] = "A";
		a[1] = "B";
		a[2] = "C";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, nondiscriminate, 0, 1, 2, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertNull(b[2]);
	}
	
	public void test110() {
		String[] a = new String[3];
		a[0] = "A";
		a[1] = "B";
		a[2] = "C";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 3, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
	}
	
	public void test111() {
		String[] a = new String[3];
		a[0] = "A";
		a[1] = "C";
		a[2] = "B";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 3, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
	}
	
	public void test112() {
		String[] a = new String[3];
		a[0] = "B";
		a[1] = "C";
		a[2] = "A";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 3, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
	}
	
	public void test113() {
		String[] a = new String[4];
		a[0] = "B";
		a[1] = "C";
		a[2] = "A";
		a[3] = "D";
		String[] b = new String[4];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 3, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertNull(b[3]);
	}
	
	public void test114() {
		String[] a = new String[4];
		a[0] = "A";
		a[1] = "B";
		a[2] = "C";
		a[3] = "D";
		String[] b = new String[4];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 4, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertEquals("D", b[3]);
	}
	
	public void test115() {
		String[] a = new String[4];
		a[0] = "A";
		a[1] = "D";
		a[2] = "C";
		a[3] = "B";
		String[] b = new String[4];
		r = spy.newInstance(null, 0);	
		assertException(IllegalArgumentException.class, () -> spy.merge3(r, natural, 0, 1, 4, b, a));
	}
	
	public void test116() {
		String[] a = new String[4];
		a[0] = "D";
		a[1] = "A";
		a[2] = "B";
		a[3] = "C";
		String[] b = new String[4];
		r = spy.newInstance(null, 0);	
		assertException(IllegalArgumentException.class, () -> spy.merge3(r, natural, 0, 2, 4, b, a));
	}
	
	public void test117() {
		String[] a = new String[4];
		a[0] = "A";
		a[1] = "D";
		a[2] = "C";
		a[3] = "B";
		String[] b = new String[4];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 2, 4, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertEquals("D", b[3]);
	}
	
	public void test118() {
		String[] a = new String[4];
		a[0] = "C";
		a[1] = "D";
		a[2] = "A";
		a[3] = "B";
		String[] b = new String[4];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 4, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertEquals("D", b[3]);
	}
	
	public void test119() {
		String[] a = new String[8];
		a[0] = "A";
		a[1] = "B";
		a[2] = "C";
		a[3] = "D";
		a[4] = "E";
		a[5] = "F";
		String[] b = new String[8];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 6, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertEquals("D", b[3]);
		assertEquals("E", b[4]);
		assertEquals("F", b[5]);
	}
	
	public void test120() {
		String[] a = new String[8];
		a[0] = "B";
		a[1] = "A";
		a[2] = "C";
		a[3] = "D";
		a[4] = "E";
		a[5] = "F";
		String[] b = new String[8];
		r = spy.newInstance(null, 0);	
		assertException(IllegalArgumentException.class, () -> spy.merge3(r, natural, 0, 3, 6, b, a));
	}
	
	public void test121() {
		String[] a = new String[8];
		a[0] = "A";
		a[1] = "B";
		a[2] = "C";
		a[3] = "D";
		a[4] = "F";
		a[5] = "E";
		String[] b = new String[8];
		r = spy.newInstance(null, 0);	
		assertException(IllegalArgumentException.class, () -> spy.merge3(r, natural, 0, 3, 6, b, a));
	}
	
	public void test122() {
		String[] a = new String[8];
		a[0] = "A";
		a[1] = "D";
		a[2] = "E";
		a[3] = "B";
		a[4] = "C";
		a[5] = "F";
		String[] b = new String[8];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 3, 6, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertEquals("D", b[3]);
		assertEquals("E", b[4]);
		assertEquals("F", b[5]);
	}
	
	public void test123() {
		String[] a = new String[8];
		a[0] = "A";
		a[1] = "B";
		a[2] = "F";
		a[3] = "C";
		a[4] = "D";
		a[5] = "E";
		String[] b = new String[8];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 3, 6, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertEquals("D", b[3]);
		assertEquals("E", b[4]);
		assertEquals("F", b[5]);
	}
	
	//Stability tests
	public void test130() {
		String[] a = new String[3];
		a[0] = "aa";
		a[1] = "Aa";
		a[2] = "AA";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, nondiscriminate, 0, 1, 3, b, a);
		assertSame("aa", b[0]);
		assertSame("Aa", b[1]);
		assertSame("AA", b[2]);
	}
	
	public void test131() {
		String[] a = new String[3];
		a[0] = "AA";
		a[1] = "Aa";
		a[2] = "aa";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, nondiscriminate, 0, 1, 3, b, a);
		assertSame("AA", b[0]);
		assertSame("Aa", b[1]);
		assertSame("aa", b[2]);
	}
	
	public void test132() {
		String[] a = new String[3];
		a[0] = "aa";
		a[1] = "Aa";
		a[2] = "B";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, insensitive, 0, 1, 3, b, a);
		assertSame("aa", b[0]);
		assertSame("Aa", b[1]);
		assertSame("B", b[2]);
	}
	
	public void test133() {
		String[] a = new String[3];
		a[0] = "B";
		a[1] = "aa";
		a[2] = "Aa";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, insensitive, 0, 1, 3, b, a);
		assertSame("aa", b[0]);
		assertSame("Aa", b[1]);
		assertSame("B", b[2]);
	}
	
	public void test134() {
		String[] a = new String[3];
		a[0] = "aa";
		a[1] = "B";
		a[2] = "Aa";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, insensitive, 0, 1, 3, b, a);
		assertSame("aa", b[0]);
		assertSame("Aa", b[1]);
		assertSame("B", b[2]);
	}
	
	public void test135() {
		String[] a = new String[3];
		a[0] = "aa";
		a[1] = null;
		a[2] = "Aa";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, nondiscriminate, 0, 1, 3, b, a);
		assertSame("aa", b[0]);
		assertNull(b[1]);
		assertSame("Aa", b[2]);
	}
	
	public void test136() {
		String[] a = new String[2];
		String s = new String("A");
		String s1 = new String("A");
		a[0] = s;
		a[1] = s1;
		String[] b = new String[2];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 2, b, a);
		assertSame(s, b[0]);
		assertSame(s1, b[1]);
	}
	
	public void test137() {
		String[] a = new String[2];
		String s = new String("A");
		String s1 = new String("A");
		a[0] = s1;
		a[1] = s;
		String[] b = new String[2];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 2, b, a);
		assertSame(s1, b[0]);
		assertSame(s, b[1]);
	}
	
	public void test138() {
		String[] a = new String[3];
		String s = new String("A");
		String s1 = new String("A");
		a[0] = s;
		a[1] = s1;
		a[2] = "B";
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 3, b, a);
		assertSame(s, b[0]);
		assertSame(s1, b[1]);
		assertEquals("B", b[2]);
	}
	
	public void test139() {
		String[] a = new String[3];
		String s = new String("A");
		String s1 = new String("A");
		a[0] = "B";
		a[1] = s;
		a[2] = s1;
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 3, b, a);
		assertSame(s, b[0]);
		assertSame(s1, b[1]);
		assertEquals("B", b[2]);
	}
	
	public void test140() {
		String[] a = new String[3];
		String s = new String("A");
		String s1 = new String("A");
		a[0] = s;
		a[1] = "B";
		a[2] = s1;
		String[] b = new String[3];
		r = spy.newInstance(null, 0);	
		spy.merge3(r, natural, 0, 1, 3, b, a);
		assertSame(s, b[0]);
		assertSame(s1, b[1]);
		assertEquals("B", b[2]);
	}
	
	/// test2xx: tests of doMergesort
	
	public void test200() {
		String[] a = new String[8];
		String[] b = new String[8];
		r = spy.newInstance(null, 0);
		spy.doMergesort(r, null, natural, 0, b, a);
	}
	
	public void test201() {
		String[] a = new String[1];
		String[] b = new String[1];
		a[0] = "A";
		b[0] = "B";
		r = spy.newInstance(null, 0);
		spy.doMergesort(r, null, natural, 1, b, a);
		assertEquals("A", a[0]);
		assertEquals("B", b[0]);
	}
	
	public void test202() {
		Node<String> n0 = spy.newNode(null, 1, null, null, null);
		String[] a = new String[1];
		String[] b = new String[1];
		r = spy.newInstance(n0, 0);
		spy.doMergesort(r, n0, nondiscriminate, 0, b, a);
		assertNull(b[0]);
	}
	
	public void test203() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		String[] a = new String[1];
		String[] b = new String[1];
		r = spy.newInstance(n1, 0);
		spy.doMergesort(r, n1, natural, 0, b, a);
		assertEquals("A", b[0]);
	}
	
	public void test204() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		String[] a = new String[2];
		String[] b = new String[2];
		a[0] = "0";
		r = spy.newInstance(n1, 0);
		spy.doMergesort(r, n1, natural, 1, b, a);
		assertNull(b[0]);
		assertEquals("A", b[1]);
		assertEquals("0", a[0]);
	}
	
	public void test205() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		String[] a = new String[2];
		String[] b = new String[2];
		a[1] = "Z";
		b[1] = "Y";
		r = spy.newInstance(n1, 0);
		spy.doMergesort(r, n1, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertEquals("Y", b[1]);
		assertEquals("Z", a[1]);
	}
	
	public void test206() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 2, n1, null, null);
		String[] a = new String[2];
		String[] b = new String[2];
		spy.setParent(n1, n2);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n2, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
	}
	
	public void test207() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 2, n1, null, null);
		String[] a = new String[3];
		String[] b = new String[3];
		spy.setParent(n1, n2);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n2, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertNull(b[2]);
	}
	
	public void test208() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 2, n1, null, null);
		String[] a = new String[2];
		String[] b = new String[2];
		a[1] = "X";
		spy.setParent(n1, n2);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n1, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertNull(b[1]);
		assertEquals("X", a[1]);
	}
	
	public void test209() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 2, n1, null, null);
		String[] a = new String[3];
		String[] b = new String[3];
		b[0] = "X"; 
		a[0] = "W";
		spy.setParent(n1, n2);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n2, natural, 1, b, a);
		assertEquals("X", b[0]);
		assertEquals("A", b[1]);
		assertEquals("B", b[2]);
		assertEquals("W", a[0]);
	}
	
	public void test210() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 3, n1, null, null);
		Node<String> n3 = spy.newNode("C", 1, null, null, null);
		String[] a = new String[3];
		String[] b = new String[3];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n2, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
	}
	
	public void test211() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 3, n1, null, null);
		Node<String> n3 = spy.newNode("C", 1, null, null, null);
		String[] a = new String[4];
		String[] b = new String[4];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n2, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertNull(b[3]);
	}
	
	public void test212() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 3, n1, null, null);
		Node<String> n3 = spy.newNode("C", 1, null, null, null);
		String[] a = new String[3];
		String[] b = new String[3];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n3, natural, 0, b, a);
		assertEquals("C", b[0]);
		assertNull(b[1]);
		assertNull(b[2]);
	}
	
	public void test213() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 3, n1, null, null);
		Node<String> n3 = spy.newNode("C", 1, null, null, null);
		String[] a = new String[4];
		String[] b = new String[4];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n3, natural, 2, b, a);
		assertNull(b[0]);
		assertNull(b[1]);
		assertEquals("C", b[2]);
		assertNull(b[3]);
	}
	
	public void test214() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 4, n1, null, null);
		Node<String> n3 = spy.newNode("C", 2, null, null, null);
		Node<String> n4 = spy.newNode("D", 1, null, null, n3);
		String[] a = new String[4];
		String[] b = new String[4];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		spy.setRight(n3, n4);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n2, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertEquals("D", b[3]);
	}
	
	public void test215() {
		Node<String> n1 = spy.newNode("D", 1, null, null, null);
		Node<String> n2 = spy.newNode("A", 4, n1, null, null);
		Node<String> n3 = spy.newNode("C", 2, null, null, null);
		Node<String> n4 = spy.newNode("B", 1, null, null, n3);
		String[] a = new String[5];
		String[] b = new String[5];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		spy.setRight(n3, n4);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n2, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertEquals("D", b[3]);
		assertNull(b[4]);
	}
	
	public void test216() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 4, n1, null, null);
		Node<String> n3 = spy.newNode("C", 2, null, null, null);
		Node<String> n4 = spy.newNode("D", 1, null, null, n3);
		String[] a = new String[4];
		String[] b = new String[4];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		spy.setRight(n3, n4);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n3, natural, 0, b, a);
		assertEquals("C", b[0]);
		assertEquals("D", b[1]);
		assertNull(b[2]);
		assertNull(b[3]);
	}
	
	public void test217() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 4, n1, null, null);
		Node<String> n3 = spy.newNode("C", 2, null, null, null);
		Node<String> n4 = spy.newNode("D", 1, null, null, n3);
		String[] a = new String[4];
		String[] b = new String[4];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		spy.setRight(n3, n4);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n4, natural, 0, b, a);
		assertEquals("D", b[0]);
		assertNull(b[1]);
		assertNull(b[2]);
		assertNull(b[3]);
	}
	
	public void test218() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 4, n1, null, null);
		Node<String> n3 = spy.newNode("C", 2, null, null, null);
		Node<String> n4 = spy.newNode("D", 1, null, null, n3);
		String[] a = new String[4];
		String[] b = new String[4];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		spy.setRight(n3, n4);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n4, natural, 3, b, a);
		assertNull(b[0]);
		assertNull(b[1]);
		assertNull(b[2]);
		assertEquals("D", b[3]);
	}
	
	public void test219() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 4, n1, null, null);
		Node<String> n3 = spy.newNode("C", 2, null, null, null);
		Node<String> n4 = spy.newNode("D", 1, null, null, n3);
		String[] a = new String[4];
		String[] b = new String[4];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		spy.setRight(n3, n4);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n3, natural, 2, b, a);
		assertNull(b[0]);
		assertNull(b[1]);
		assertEquals("C", b[2]);
		assertEquals("D", b[3]);
	}
	
	public void test220() {
		Node<String> n1 = spy.newNode("F", 1, null, null, null);
		Node<String> n2 = spy.newNode("A", 3, n1, null, null);
		Node<String> n3 = spy.newNode("B", 1, null, null, n2);
		Node<String> n4 = spy.newNode("D", 7, n2, null, null);
		Node<String> n5 = spy.newNode("G", 1, null, null, null);
		Node<String> n6 = spy.newNode("E", 3, n5, null, n4);
		Node<String> n7 = spy.newNode("C", 1, null, null, n6);
		String[] a = new String[7];
		String[] b = new String[7];
		spy.setParent(n1, n2);
		spy.setParent(n2, n4);
		spy.setParent(n5, n6);
		spy.setRight(n2, n3);
		spy.setRight(n4, n6);
		spy.setRight(n6, n7);
		
		r = spy.newInstance(n4, 0);
		spy.doMergesort(r, n4, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertEquals("D", b[3]);
		assertEquals("E", b[4]);
		assertEquals("F", b[5]);
		assertEquals("G", b[6]);
	}
	
	public void test221() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 3, n1, null, null);
		Node<String> n3 = spy.newNode("C", 1, null, null, n2);
		Node<String> n4 = spy.newNode("D", 7, n2, null, null);
		Node<String> n5 = spy.newNode("E", 1, null, null, null);
		Node<String> n6 = spy.newNode("F", 3, n5, null, n4);
		Node<String> n7 = spy.newNode("G", 1, null, null, n6);
		String[] a = new String[8];
		String[] b = new String[8];
		spy.setParent(n1, n2);
		spy.setParent(n2, n4);
		spy.setParent(n5, n6);
		spy.setRight(n2, n3);
		spy.setRight(n4, n6);
		spy.setRight(n6, n7);
		
		r = spy.newInstance(n4, 0);
		spy.doMergesort(r, n4, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertEquals("D", b[3]);
		assertEquals("E", b[4]);
		assertEquals("F", b[5]);
		assertEquals("G", b[6]);
		assertNull(b[7]);
	}
	
	public void test222() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 3, n1, null, null);
		Node<String> n3 = spy.newNode("C", 1, null, null, n2);
		Node<String> n4 = spy.newNode("D", 7, n2, null, null);
		Node<String> n5 = spy.newNode("E", 1, null, null, null);
		Node<String> n6 = spy.newNode("F", 3, n5, null, n4);
		Node<String> n7 = spy.newNode("G", 1, null, null, n6);
		String[] a = new String[7];
		String[] b = new String[7];
		spy.setParent(n1, n2);
		spy.setParent(n2, n4);
		spy.setParent(n5, n6);
		spy.setRight(n2, n3);
		spy.setRight(n4, n6);
		spy.setRight(n6, n7);
		
		r = spy.newInstance(n4, 0);
		spy.doMergesort(r, n2, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertNull(b[3]);
		assertNull(b[4]);
		assertNull(b[5]);
	}
	
	public void test223() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 3, n1, null, null);
		Node<String> n3 = spy.newNode("C", 1, null, null, n2);
		Node<String> n4 = spy.newNode("D", 7, n2, null, null);
		Node<String> n5 = spy.newNode("E", 1, null, null, null);
		Node<String> n6 = spy.newNode("F", 3, n5, null, n4);
		Node<String> n7 = spy.newNode("G", 1, null, null, n6);
		String[] a = new String[7];
		String[] b = new String[7];
		spy.setParent(n1, n2);
		spy.setParent(n2, n4);
		spy.setParent(n5, n6);
		spy.setRight(n2, n3);
		spy.setRight(n4, n6);
		spy.setRight(n6, n7);
		
		r = spy.newInstance(n4, 0);
		spy.doMergesort(r, n6, natural, 0, b, a);
		assertEquals("E", b[0]);
		assertEquals("F", b[1]);
		assertEquals("G", b[2]);
		assertNull(b[3]);
		assertNull(b[4]);
		assertNull(b[5]);
	}
	
	public void test224() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 3, n1, null, null);
		Node<String> n3 = spy.newNode("C", 1, null, null, n2);
		Node<String> n4 = spy.newNode("D", 7, n2, null, null);
		Node<String> n5 = spy.newNode("E", 1, null, null, null);
		Node<String> n6 = spy.newNode("F", 3, n5, null, n4);
		Node<String> n7 = spy.newNode("G", 1, null, null, n6);
		String[] a = new String[7];
		String[] b = new String[7];
		spy.setParent(n1, n2);
		spy.setParent(n2, n4);
		spy.setParent(n5, n6);
		spy.setRight(n2, n3);
		spy.setRight(n4, n6);
		spy.setRight(n6, n7);
		
		r = spy.newInstance(n4, 0);
		spy.doMergesort(r, n3, natural, 0, b, a);
		assertEquals("C", b[0]);
		assertNull(b[1]);
		assertNull(b[2]);
		assertNull(b[3]);
		assertNull(b[4]);
		assertNull(b[5]);
		assertNull(b[6]);
	}
	
	public void test225() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 3, n1, null, null);
		Node<String> n3 = spy.newNode("C", 1, null, null, n2);
		Node<String> n4 = spy.newNode("D", 7, n2, null, null);
		Node<String> n5 = spy.newNode("E", 1, null, null, null);
		Node<String> n6 = spy.newNode("F", 3, n5, null, n4);
		Node<String> n7 = spy.newNode("G", 1, null, null, n6);
		String[] a = new String[7];
		String[] b = new String[7];
		spy.setParent(n1, n2);
		spy.setParent(n2, n4);
		spy.setParent(n5, n6);
		spy.setRight(n2, n3);
		spy.setRight(n4, n6);
		spy.setRight(n6, n7);
		
		r = spy.newInstance(n4, 0);
		spy.doMergesort(r, n7, natural, 0, b, a);
		assertEquals("G", b[0]);
		assertNull(b[1]);
		assertNull(b[2]);
		assertNull(b[3]);
		assertNull(b[4]);
		assertNull(b[5]);
		assertNull(b[6]);
	}
	
	public void test226() {
		Node<String> n1 = spy.newNode("A", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 3, n1, null, null);
		Node<String> n3 = spy.newNode("C", 1, null, null, n2);
		Node<String> n4 = spy.newNode("D", 7, n2, null, null);
		Node<String> n5 = spy.newNode("E", 1, null, null, null);
		Node<String> n6 = spy.newNode("F", 3, n5, null, n4);
		Node<String> n7 = spy.newNode("G", 1, null, null, n6);
		String[] a = new String[7];
		String[] b = new String[7];
		spy.setParent(n1, n2);
		spy.setParent(n2, n4);
		spy.setParent(n5, n6);
		spy.setRight(n2, n3);
		spy.setRight(n4, n6);
		spy.setRight(n6, n7);
		
		r = spy.newInstance(n4, 0);
		spy.doMergesort(r, n2, natural, 0, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		
		spy.doMergesort(r, n6, natural, 3, b, a);
		assertEquals("A", b[0]);
		assertEquals("B", b[1]);
		assertEquals("C", b[2]);
		assertEquals("E", b[3]);
		assertEquals("F", b[4]);
		assertEquals("G", b[5]);
		assertNull(b[6]);
	}
	
	//stability tests
	public void test230() {
		Node<String> n1 = spy.newNode("Aa", 1, null, null, null);
		Node<String> n2 = spy.newNode("AA", 3, n1, null, null);
		Node<String> n3 = spy.newNode("B", 1, null, null, null);
		String[] a = new String[3];
		String[] b = new String[3];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n2, insensitive, 0, b, a);
		assertEquals("Aa", b[0]);
		assertEquals("AA", b[1]);
		assertEquals("B", b[2]);
	}
	
	public void test231() {
		Node<String> n1 = spy.newNode("AA", 1, null, null, null);
		Node<String> n2 = spy.newNode("Aa", 3, n1, null, null);
		Node<String> n3 = spy.newNode("B", 1, null, null, null);
		String[] a = new String[3];
		String[] b = new String[3];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n2, insensitive, 0, b, a);
		assertEquals("AA", b[0]);
		assertEquals("Aa", b[1]);
		assertEquals("B", b[2]);
	}
	
	public void test232() {
		Node<String> n1 = spy.newNode("AA", 1, null, null, null);
		Node<String> n2 = spy.newNode("B", 3, n1, null, null);
		Node<String> n3 = spy.newNode("Aa", 1, null, null, null);
		String[] a = new String[3];
		String[] b = new String[3];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n2, insensitive, 0, b, a);
		assertEquals("AA", b[0]);
		assertEquals("Aa", b[1]);
		assertEquals("B", b[2]);
	}
	
	public void test233() {
		Node<String> n1 = spy.newNode("B", 1, null, null, null);
		Node<String> n2 = spy.newNode("AA", 3, n1, null, null);
		Node<String> n3 = spy.newNode("Aa", 1, null, null, null);
		String[] a = new String[3];
		String[] b = new String[3];
		spy.setParent(n1, n2);
		spy.setParent(n3, n2);
		spy.setRight(n2, n3);
		
		r = spy.newInstance(n2, 0);
		spy.doMergesort(r, n2, insensitive, 0, b, a);
		assertEquals("AA", b[0]);
		assertEquals("Aa", b[1]);
		assertEquals("B", b[2]);
	}
	
	public void test234() {
		Node<String> n1 = spy.newNode("AA", 1, null, null, null);
		Node<String> n0 = spy.newNode(null, 3, n1, null, null);
		Node<String> n3 = spy.newNode("Aa", 1, null, null, null);
		String[] a = new String[3];
		String[] b = new String[3];
		spy.setParent(n1, n0);
		spy.setParent(n3, n0);
		spy.setRight(n0, n3);
		
		r = spy.newInstance(n0, 0);
		spy.doMergesort(r, n0, nondiscriminate, 0, b, a);
		assertEquals("AA", b[0]);
		assertNull(b[1]);
		assertEquals("Aa", b[2]);
	}
}
