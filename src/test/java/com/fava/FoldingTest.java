package com.fava;

import com.fava.Currying;
import com.fava.Folding;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class FoldingTest {
	@Test
	public void testFoldl() {
		Currying.F2<Integer, String, String> addParenthese = new Currying.F2<Integer, String, String>() {
			@Override
			public String apply(Integer arg1, String arg2) {
				return "(" + arg2 + "+" + arg1 + ")";
			}
		};

		Assert.assertEquals("(((0+1)+2)+3)", Folding.<Integer, String>foldl().apply(addParenthese, "0", Arrays.asList(1, 2, 3)));
	}

	@Test
	public void testFoldr() {
		Currying.F2<Integer, String, String> addParenthese = new Currying.F2<Integer, String, String>() {
			@Override
			public String apply(Integer arg1, String arg2) {
				return "(" + arg1 + "+" + arg2 + ")";
			}
		};

		Assert.assertEquals("(1+(2+(3+0)))", Folding.foldr(addParenthese, "0", Arrays.asList(1, 2, 3)));
	}
}
