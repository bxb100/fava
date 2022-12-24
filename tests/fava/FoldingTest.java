package fava;

import fava.Currying.F2;
import org.junit.Test;

import java.util.Arrays;

import static fava.Folding.foldr;
import static org.junit.Assert.assertEquals;

public class FoldingTest {
	@Test
	public void testFoldl() {
		F2<Integer, String, String> addParenthese = new F2<Integer, String, String>() {
			@Override
			public String apply(Integer arg1, String arg2) {
				return "(" + arg2 + "+" + arg1 + ")";
			}
		};

		assertEquals("(((0+1)+2)+3)", Folding.<Integer, String>foldl().apply(addParenthese, "0", Arrays.asList(1, 2, 3)));
	}

	@Test
	public void testFoldr() {
		F2<Integer, String, String> addParenthese = new F2<Integer, String, String>() {
			@Override
			public String apply(Integer arg1, String arg2) {
				return "(" + arg1 + "+" + arg2 + ")";
			}
		};

		assertEquals("(1+(2+(3+0)))", foldr(addParenthese, "0", Arrays.asList(1, 2, 3)));
	}
}
