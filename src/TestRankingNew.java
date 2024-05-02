import java.util.Comparator;
import java.util.function.Supplier;

import edu.uwm.cs351.Ranking;
import junit.framework.TestCase;

public class TestRankingNew extends TestCase {
	protected static final Comparator<String> unusable = (s1, s2) -> {
		throw new UnsupportedOperationException("do not call this comparator");
	};
	protected static final <T extends Comparable<T>> Comparator<T> nullComparator() {
		return (o1, o2) -> {
			if (o1 == null) {
				if (o2 == null) return 0;
				else return -15;
			} else {
				if (o2 == null) return 42;
				return o1.compareTo(o2);
			}
		};
	}
	
	protected <T> void assertException(Class<?> excClass, Runnable f) {
		try {
			f.run();
			assertFalse("Should have thrown an exception, not returned",true);
		} catch (RuntimeException ex) {
			if (!excClass.isInstance(ex)) {
				ex.printStackTrace();
				assertFalse("Wrong kind of exception thrown: "+ ex.getClass().getSimpleName(),true);
			}
		}		
	}

	protected Ranking<String> self;
	
	@Override // implementation
	protected void setUp() {
		try {
			assert self.size() > 0;
			System.out.println("Assertions must be enabled when runnig these tests");
			System.out.println("Add '-ea' to the VM Arguments box on the argument pane of ");
			System.out.println("the Ron Configuration");
			assertFalse("Must enable assertions", true);
		} catch (NullPointerException ex) {
			assertTrue("correctly enabling assertions", true);
		}
		self = new Ranking<>();
	}
	
	
	/// Locked Tests

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

	
	
	/// Test class used to check equivalent but unequal values
	
	class School implements Comparable<School> {
		private final String name;
		
		School(String n) { name = n; }
		
		@Override // implementation
		public String toString() { return "@" + name; }
		
		@Override // implementation
		public int hashCode() { return name.toLowerCase().hashCode(); }
		
		@Override // implementation
		public boolean equals(Object o) {
			if (!(o instanceof School)) return false;
			School other = (School)o;
			return name.equalsIgnoreCase(other.name);
		}

		@Override // required
		public int compareTo(TestRankingNew.School o) {
			return o.name.compareToIgnoreCase(this.name); // reverse case insensitive
		}
	}
	
	
	/// text0x: sorting zero or one elements with different arguments
	
	public void test00() {
		String[] a = new String[] { "xyzzy" };
		String[] result = self.toSortedArray(a, Comparator.naturalOrder());
		assertSame(a, result);
		assertEquals("xyzzy", result[0]);
	}
	
	public void test01() {
		String[] a = new String[] { };
		String[] result = self.toSortedArray(a, Comparator.naturalOrder());
		assertSame(a, result);
	}
	
	public void test02() {
		String[] a = new String[] { "xyzzy" };
		self.insert(1, "Hello");
		String[] result = self.toSortedArray(a, Comparator.naturalOrder());
		assertSame(a, result);
		assertEquals("Hello", result[0]);
	}
	
	public void test03() {
		String[] a = new String[] { };
		self.insert(1, "Hello");
		String[] result = self.toSortedArray(a, Comparator.naturalOrder());
		assertEquals(1, result.length);
		assertEquals("Hello", result[0]);
	}
	
	public void test04() {
		String[] a = new String[] { "xyzzy", "abracadabra" };
		self.insert(1, "Hello");
		String[] result = self.toSortedArray(a, Comparator.naturalOrder());
		assertSame(a, result);
		assertEquals("Hello", result[0]);
		assertEquals("abracadabra", result[1]);
	}
	
	public void test05() {
		String[] a = new String[] { "xyzzy" };
		String[] result = self.toSortedArray(a, unusable);
		assertSame(a, result);
		assertEquals("xyzzy", result[0]);
	}
	
	public void test06() {
		String[] a = new String[] { "xyzzy" };
		String[] result = self.toSortedArray(a, null);
		assertSame(a, result);
		assertEquals("xyzzy", result[0]);
	}
	
	public void test07() {
		String[] a = new String[] { "xyzzy", "abracadabra" };
		self.insert(1, "Hello");
		String[] result = self.toSortedArray(a, null);
		assertSame(a, result);
		assertEquals("Hello", result[0]);
		assertEquals("abracadabra", result[1]);
	}
	
	public void test08() {
		Ranking<Object> r = new Ranking<>();
		Object[] a = new Object[] { "xyzzy" };
		Object[] result = r.toSortedArray(a, null);
		assertSame(a, result);
		assertEquals("xyzzy", result[0]);
	}
	
	public void test09() {
		Ranking<Object> r = new Ranking<>();
		Object[] a = new Object[] { "xyzzy" };
		r.insert(1,  new Object());
		assertException(ClassCastException.class, () -> r.toSortedArray(a, null));
	}
	
	
	/// test1x: sorting two elements with different comparators
	
	public void test10() {
		self.insert(1, "Hello");
		self.insert(1, "bye");
		String[] a = new String[] { "xyzzy" };
		String[] result = self.toSortedArray(a, null);
		assertEquals(2, result.length);
		assertEquals("Hello", result[0]);
		assertEquals("bye", result[1]);
	}
	
	public void test11() {
		self.insert(1, "Hello");
		self.insert(1, "bye");
		String[] a = new String[] { "xyzzy", "abracadabra" };
		String[] result = self.toSortedArray(a, null);
		assertSame(a, result);
		assertEquals("Hello", result[0]);
		assertEquals("bye", result[1]);
	}
	
	public void test12() {
		self.insert(1, "Hello");
		self.insert(1, "bye");
		String[] a = new String[] { "xyzzy", "abracadabra", "UWM" };
		String[] result = self.toSortedArray(a, null);
		assertSame(a, result);
		assertEquals("Hello", result[0]);
		assertEquals("bye", result[1]);
		assertEquals("UWM", result[2]);
	}
	
	public void test13() {
		self.insert(1, "Hello");
		self.insert(1, "bye");
		String[] a = new String[] { };
		String[] result = self.toSortedArray(a, String.CASE_INSENSITIVE_ORDER);
		assertEquals(2, result.length);
		assertEquals("bye", result[0]);
		assertEquals("Hello", result[1]);
	}

	public void test14() {
		self.insert(1, "Hello");
		self.insert(2, "bye");
		String[] a = new String[] { null, null, null, "xyzzy" };
		String[] result = self.toSortedArray(a, (s1, s2) -> s2.compareTo(s1));
		assertSame(a, result);
		assertEquals("bye", result[0]);
		assertEquals("Hello", result[1]);
		assertNull(result[2]);
		assertEquals("xyzzy", result[3]);
	}
	
	public void test15() {
		self.insert(1, "Hello");
		self.insert(1, "bye");
		String[] a = new String[] { };
		String[] result = self.toSortedArray(a, String.CASE_INSENSITIVE_ORDER);
		assertEquals(2, result.length);
		assertEquals("bye", result[0]);
		assertEquals("Hello", result[1]);
	}
	
	public void test16() {
		self.insert(1, "Hello");
		self.insert(1, null);
		String[] a = new String[] { };
		String[] result = self.toSortedArray(a, nullComparator());
		assertEquals(2, result.length);
		assertEquals(null, result[0]);
		assertEquals("Hello", result[1]);
	}
	
	public void test17() {
		self.insert(1, null);
		self.insert(1, "bye");
		String[] a = new String[] { };
		String[] result = self.toSortedArray(a, nullComparator());
		assertEquals(2, result.length);
		assertEquals(null, result[0]);
		assertEquals("bye", result[1]);
	}
	
	public void test18() {
		Ranking<School> r = new Ranking<>();
		r.insert(1, new School("UWM"));
		r.insert(2, new School("marquette"));
		School[] a = new School[] { null, new School("xyzzy"), new School("abracadabra")};
		School[] result = r.toSortedArray(a, nullComparator());
		assertSame(a, result);
		assertEquals(new School("UWM"), result[0]);
		assertEquals(new School("marquette"), result[1]);
		assertEquals(new School("abracadabra"), result[2]);
	}
	
	public void test19() {
		Ranking<School> r = new Ranking<>();
		r.insert(1, new School("UWM"));
		r.insert(2, new School("marquette"));
		School[] a = new School[] { null, new School("xyzzy"), new School("abracadabra")};
		School[] result = r.toSortedArray(a, (s1,s2) -> s2.toString().compareTo(s1.toString()));
		assertSame(a, result);
		assertEquals(new School("marquette"), result[0]);
		assertEquals(new School("UWM"), result[1]);
		assertEquals(new School("abracadabra"), result[2]);
	}
	
	
	/// test2x: sorting with multiple elements
	
	public void test20() {
		self.insert(1, "A");
		self.insert(1, "b");
		self.insert(3, "C");
		String[] result = self.toSortedArray(new String[0], null);
		assertEquals(3, result.length);
		assertEquals("A", result[0]);
		assertEquals("C", result[1]);
		assertEquals("b", result[2]);
	}
	
	public void test21() {
		self.insert(1, "A");
		self.insert(1, "b");
		self.insert(3, "C");
		String[] result = self.toSortedArray(new String[0], nullComparator());
		assertEquals(3, result.length);
		assertEquals("A", result[0]);
		assertEquals("C", result[1]);
		assertEquals("b", result[2]);
	}
	
	public void test22() {
		self.insert(1, "A");
		self.insert(1, "b");
		self.insert(3, "C");
		String[] result = self.toSortedArray(new String[0], (s1,s2) -> s1.compareToIgnoreCase(s2));
		assertEquals(3, result.length);
		assertEquals("A", result[0]);
		assertEquals("b", result[1]);
		assertEquals("C", result[2]);		
	}
	
	public void test23() {
		self.insert(1, "A");
		self.insert(2, "b");
		self.insert(3, "C");
		String[] result = self.toSortedArray(new String[0], null);
		assertEquals(3, result.length);
		assertEquals("A", result[0]);
		assertEquals("C", result[1]);
		assertEquals("b", result[2]);
	}
	
	public void test24() {
		self.insert(1, "A");
		self.insert(1, "b");
		self.insert(2, "C");
		String[] result = self.toSortedArray(new String[0], (s1,s2) -> s2.compareToIgnoreCase(s1));
		assertEquals(3, result.length);
		assertEquals("C", result[0]);
		assertEquals("b", result[1]);
		assertEquals("A", result[2]);		
	}

	
	
	/// test3x: tests of stability

	public void test30() {
		self.insert(1, "A");
		self.insert(1, "a");
		String[] result = self.toSortedArray(new String[0], String.CASE_INSENSITIVE_ORDER);
		assertEquals(2, result.length);
		assertEquals("a", result[0]);
		assertEquals("A", result[1]);
	}
	
	public void test31() {
		self.insert(1, "A");
		self.insert(2, "B");
		String[] result = self.toSortedArray(new String[0], (s1, s2) -> 0);
		assertEquals(2, result.length);
		assertEquals("A", result[0]);
		assertEquals("B", result[1]);
	}
	
	public void test32() {
		self.insert(1, "A");
		self.insert(1, "b");
		self.insert(3, "C");
		String[] result = self.toSortedArray(new String[0], (s1, s2) -> 0);
		assertEquals(3, result.length);
		assertEquals("b", result[0]);
		assertEquals("A", result[1]);
		assertEquals("C", result[2]);
	}
	
	public void test33() {
		String s1 = new String("A");
		String s2 = new String("A");
		String s3 = new String("A");
		self.insert(1, s2);
		self.insert(1, s1);
		self.insert(3, s3);
		String[] result = self.toSortedArray(new String[0], null);
		assertEquals(3, result.length);
		assertSame(s1, result[0]);
		assertSame(s2, result[1]);
		assertSame(s3, result[2]);
	}

	public void test34() {
		self.insert(1, "A");
		self.insert(1, "b");
		self.insert(3, "B");
		String[] result = self.toSortedArray(new String[0], String.CASE_INSENSITIVE_ORDER);
		assertEquals(3, result.length);
		assertSame("A", result[0]);
		assertSame("b", result[1]);
		assertSame("B", result[2]);		
	}
}
