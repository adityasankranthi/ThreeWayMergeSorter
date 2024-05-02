import java.util.Comparator;
import java.util.Map;

import edu.uwm.cs.junit.EfficiencyTestCase;
import edu.uwm.cs351.Ranking;

public class TestEfficiencyNew extends EfficiencyTestCase {

	private Map<Integer,Double> m;
	private Ranking<Double> r;
	
	private static final int POWER = 21; // million entries
	private static final int SIZE = (1 << (POWER-1)) - 1;
	private static final int TESTS = SIZE/POWER; //
	
	protected void setUp() {
		super.setUp();
		try {
			assert m.size() == TESTS : "cannot run test with assertions enabled";
		} catch (NullPointerException ex) {
			throw new IllegalStateException("Cannot run test with assertions enabled");
		}
		m = r = new Ranking<Double>();
		for (int power = POWER; power > 1; --power) {
			int incr = 1 << power;
			int start = 1 << (power-1);
			for (int j=0; j < 1<<(POWER-power); ++j) {
				r.insert(j*2+1, Double.valueOf(j*incr+start));
			}
		}
	}
			
	@Override
	protected void tearDown() {
		m = r = null;
		super.tearDown();
	}

	
	public void testU() {
		Double[] a = r.toSortedArray(new Double[0], null);
		assertEquals(SIZE, a.length);
		for (int i=0; i < SIZE; ++i) {
			assertEquals(2+i*2, a[i].intValue());
		}
	}
	
	public void testV() {
		Double[] a = r.toSortedArray(new Double[0], (d1,d2) -> 0);
		assertEquals(SIZE, a.length);
		for (int i=0; i < SIZE; ++i) {
			assertEquals(2+i*2, a[i].intValue());
		}
	}
	
	public void testW() {
		Double[] a = r.toSortedArray(new Double[0], (d1,d2) -> Double.compare(d2,d1));
		assertEquals(SIZE, a.length);
		for (int i=0; i < SIZE; ++i) {
			assertEquals(2*SIZE-i*2, a[i].intValue());
		}
	}
	
	public void testX() {
		Comparator<Double> comp = (d1,d2) -> {
			int i1 = d1.intValue();
			int i2 = d2.intValue();
			if ((i1 & 3) == 0) i1 = -i1;
			if ((i2 & 3) == 0) i2 = -i2;
			return i1 - i2;
		};
		Double[] a = r.toSortedArray(new Double[0], comp);
		assertEquals(SIZE, a.length);
		for (int i=0; i < SIZE/2; ++i) {
			assertEquals(2*SIZE-4*i-2, a[i].intValue());
		}
		for (int i=0; i < SIZE/2; ++i) {
			assertEquals(4*i+2, a[SIZE/2+i].intValue());
		}
	}
}
