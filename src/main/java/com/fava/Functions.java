package com.fava;

/**
 * Functional interfaces.
 */
public final class Functions {
	/**
	 * Functional interface for function of type {@code T -> R}.
	 */
	@FunctionalInterface
	public interface IF1<T, R> {
		R apply(T arg);
	}

	/**
	 * Functional interface for function of type {@code T1 -> T2 -> R}.
	 */
	@FunctionalInterface
	public interface IF2<T1, T2, R> {
		R apply(T1 arg1, T2 arg2);
	}

	/**
	 * Functional interface for function of type {@code T1 -> T2 -> T3 -> R}.
	 */
	@FunctionalInterface
	public interface IF3<T1, T2, T3, R> {
		R apply(T1 arg1, T2 arg2, T3 arg3);
	}

	/**
	 * Functional interface for function of type {@code T1 -> T2 -> T3 -> T4 -> R}.
	 */
	@FunctionalInterface
	public interface IF4<T1, T2, T3, T4, R> {
		R apply(T1 arg1, T2 arg2, T3 arg3, T4 arg4);
	}

	/**
	 * Functional interface for function of type {@code T1 -> T2 -> T3 -> T4 -> T5 -> R}.
	 */
	@FunctionalInterface
	public interface IF5<T1, T2, T3, T4, T5, R> {
		R apply(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5);
	}

	/*
	Java no structural type: https://en.wikipedia.org/wiki/Structural_type_system
	and you can see answer like this: https://stackoverflow.com/questions/28509596/java-lambda-expressions-casting-and-comparators
	so the code below is not possible:
	 ```java
	 F2<T, T, T> curry = curry(f);
	 P2<T> o = (P2<T>) curry;
	 ```
	 the curry(F2) return is the anonymous sub object of IF2, so it clearly
	 not allow convert `F2$1` to `P2`
	 */

	@FunctionalInterface
	public interface IP1<T> extends IF1<T, T> {
	}

	@FunctionalInterface
	public interface IP2<T> extends IF2<T, T, T> {
	}

	@FunctionalInterface
	public interface IP3<T> extends IF3<T, T, T, T> {
	}

	@FunctionalInterface
	public interface IP4<T> extends IF4<T, T, T, T, T> {
	}

	@FunctionalInterface
	public interface IP5<T> extends IF5<T, T, T, T, T, T> {
	}
}
