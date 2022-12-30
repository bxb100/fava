package com.fava;

import com.fava.data.Strings;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class StringsTest {

	@Test
	public void testTimes() {
		assertEquals("abcabcabc", Strings.times().apply(3).apply("abc"));
	}

	@Test
	public void testSplit() {
		assertArrayEquals(new String[]{"a", "b", "c"}, Strings.split().apply(",").apply("a,b,c").toArray());
	}
}
