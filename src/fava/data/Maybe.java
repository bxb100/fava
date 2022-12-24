package fava.data;

import fava.Functions.IF1;
import fava.functor.Functor;

public class Maybe<T> implements Functor<T> {
	private final boolean hasValue;
	private final T value;

	private Maybe(boolean hasValue, T value) {
		this.hasValue = hasValue;
		this.value = value;
	}

	public static <T> Maybe<T> just(T t) {
		return new Maybe<T>(true, t);
	}

	public static <T> Maybe<T> nothing() {
		return new Maybe<T>(false, null);
	}

	@Override
	public <R> Maybe<R> fmap(IF1<T, R> f) {
		return hasValue ? just(f.apply(value)) : Maybe.nothing();
	}

	public boolean hasValue() {
		return hasValue;
	}

	public T getValue() {
		assert hasValue;
		return value;
	}

	public boolean equals(Maybe<T> rhs) {
		return (rhs != null && hasValue == rhs.hasValue) && (!hasValue || value.equals(rhs.value));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Maybe<?> && equals((Maybe<T>) obj);
	}
}
