package fava;

import fava.Currying.F1;
import fava.Currying.F2;
import fava.data.Numbers;
import fava.data.Strings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CurryingTest {
	@Test
	public void testCurrying() {
		F2<Integer, String, String> times = Currying.curry(Strings::times);
		assertEquals("abcabcabc", times.apply(3).apply("abc"));
		assertEquals("abcabcabc", times.apply(3).apply("abc"));
		assertTrue(times.apply(3) instanceof F1<?, ?>);
		assertEquals("abcabcabc", times.apply(3, "abc"));
	}

	@Test
	public void testNumbers() {

		F2<Integer, Integer, Integer> add = Currying.curry(Numbers::add);
		assertEquals(7, add.apply(3).apply(4).intValue());
		assertEquals(7, add.apply(3, 4).intValue());
		assertTrue(add.apply(3) instanceof F1<?, ?>);
	}
}
