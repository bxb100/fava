package com.fava.data;

import com.fava.Composing;
import com.fava.Currying;
import com.fava.Functions.IF1;
import com.fava.Functions.IF2;

import static com.fava.Currying.curry;

/**
 * A set of functions for {@link Maybe}.
 */
public final class Maybes {
	/**
	 * {@code fmap} for Maybe Functor, which lifts a function of {@code T -> R}
	 * into a function of type {@code Maybe<T> -> Maybe<R>}.
	 */
	public static <T, R> Currying.F1<Maybe<T>, Maybe<R>> fmap(final IF1<T, R> f) {
		return new Currying.F1<Maybe<T>, Maybe<R>>() {
			@Override
			public Maybe<R> apply(Maybe<T> maybeT) {
				return maybeT.fmap(f);
			}
		};
	}

	/**
	 * {@code fapply} for Maybe Applicative Functor, which turns an instance of
	 * type {@code Maybe<T -> R>} into a function of type {@code Maybe<T> -> Maybe<R>}.
	 */
	public static <T, R, F extends IF1<T, R>> Currying.F1<Maybe<T>, Maybe<R>> fapply(final Maybe<F> f) {
		assert f.hasValue();
		return fmap(f.getValue());
	}

	/**
	 * Lifts a function of type {@code T1 -> T2 -> R} into a function of type
	 * {@code Maybe<T1> -> Maybe<T2> -> Maybe<R>}.
	 */
	public static <T1, T2, R> Currying.F2<Maybe<T1>, Maybe<T2>, Maybe<R>> liftA(IF2<T1, T2, R> f) {
		IF1<Maybe<Currying.F1<T2, R>>, Currying.F1<Maybe<T2>, Maybe<R>>> fapply =
				Maybes::fapply;
		return Currying.uncurry(Composing.__(fmap(Currying.curry(f)), fapply));
	}
}
