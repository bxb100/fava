package com.fava;

import com.fava.data.Numbers;
import com.fava.data.Strings;
import org.junit.Test;

import static com.fava.Currying.F2;
import static com.fava.Currying.P2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CurryingTest {
	@Test
	public void testCurrying() {
		Currying.F2<Integer, String, String> times = Currying.curry(Strings::times);
		assertEquals("abcabcabc", times.apply(3).apply("abc"));
		assertEquals("abcabcabc", times.apply(3).apply("abc"));
		assertTrue(times.apply(3) instanceof Currying.F1<?, ?>);
		assertEquals("abcabcabc", times.apply(3, "abc"));
	}

	@Test
	public void testNumbers() {

		Currying.F2<Integer, Integer, Integer> add = Currying.curry(Numbers::add);
		assertEquals(7, add.apply(3).apply(4).intValue());
		assertEquals(7, add.apply(3, 4).intValue());
		assertTrue(add.apply(3) instanceof Currying.F1<?, ?>);
	}

	@Test
	public void testP2() {

		F2<String, String, String> curry = Currying.curry((a, b) -> a + b);
		assertEquals("ab", curry.apply("a").apply("b"));

		P2<String> aliasF2 = Currying.curring((a, b) -> a + b);
		assertEquals("ab", aliasF2.apply("a").apply("b"));
		assertEquals("ab", aliasF2.apply("a", "b"));
	}
}
