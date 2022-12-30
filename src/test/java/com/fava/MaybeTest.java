package com.fava;

import com.fava.Functions.IF2;
import com.fava.data.Maybe;
import com.fava.data.Maybes;
import com.fava.data.Numbers;
import org.junit.Test;

import static org.junit.Assert.*;

public class MaybeTest {
	Currying.F1<Integer, String> intToStr = new Currying.F1<Integer, String>() {
		@Override
		public String apply(Integer arg) {
			return String.valueOf(arg);
		}
	};

	@Test
	public void testNothing() {
		Maybe<Integer> nothing = Maybe.nothing();
		Maybe<String> result = Maybes.fmap(intToStr).apply(nothing);
		assertFalse(result.hasValue());
	}

	@Test
	public void testJust() {
		Maybe<Integer> just3 = Maybe.just(3);
		Maybe<String> result = Maybes.fmap(intToStr).apply(just3);
		assertTrue(result.hasValue());
		assertEquals("3", result.getValue());
	}

	@Test
	public void testLiftA_just() {
		Maybe<Integer> just3 = Maybe.just(3);
		Maybe<Integer> just4 = Maybe.just(4);
		IF2<Integer, Integer, Integer> add = Numbers::add;
		Currying.F2<Maybe<Integer>, Maybe<Integer>, Maybe<Integer>> addMaybe = Maybes.liftA(add);
		assertEquals(Maybe.just(7), addMaybe.apply(just3, just4));
	}

	@Test
	public void testLiftA_nothing() {
		Maybe<Integer> just3 = Maybe.just(3);
		IF2<Integer, Integer, Integer> add = Numbers::add;
		Currying.F2<Maybe<Integer>, Maybe<Integer>, Maybe<Integer>> addMaybe = Maybes.liftA(add);
		assertEquals(Maybe.nothing(), addMaybe.apply(just3, Maybe.nothing()));
	}
}
