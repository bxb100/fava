package fava;

import fava.data.Strings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringsTest {

	@Test
	public void testTimes() {
		assertEquals("abcabcabc", Strings.times().apply(3).apply("abc"));
	}
}
