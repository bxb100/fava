package fava;

import fava.Currying.F1;
import fava.data.Lists;
import org.junit.Test;

import static fava.data.Lists.map;
import static fava.data.Strings.*;
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
		F1<String, String> f = Composing._do(split(" "), Lists.reverse(), map(toUpperCase()), join("_"));
		assertEquals("JAVA_IN_PROGRAMMING_LOVE_I", f.apply("I love programming in Java"));
	}
}
