package com.fava;

import com.fava.data.Lists;
import org.junit.Test;

import static com.fava.data.Lists.map;
import static com.fava.data.Strings.*;
import static org.junit.Assert.assertEquals;

public class ComposingTest {
	/**
	 * This test case demonstrate composing the following functions:
	 * 1) split by space;
	 * 2) reverse a list;
	 * 3)
	 */
	@Test
	public void testCompose() {
		Currying.F1<String, String> f = Composing.__(split(" "), Lists.reverse(), map(toUpperCase()), join("_"));
		assertEquals("JAVA_IN_PROGRAMMING_LOVE_I", f.apply("I love programming in Java"));
	}
}
