package fava;

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
