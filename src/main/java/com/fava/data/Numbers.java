package com.fava.data;

import com.fava.Currying;

import static com.fava.Currying.curry;

/**
 * Functions for numbers.
 */
public class Numbers {

	public static int add(int arg1, int arg2) {
		return _add(arg1, arg2);
	}

	public static int subtract(int arg1, int arg2) {
		return _subtract(arg1, arg2);
	}

	public static Currying.F2<Integer, Integer, Integer> subtract() {
		return Currying.curry(Numbers::_subtract);
	}

	public static int multiply(int arg1, int arg2) {
		return _multiply(arg1, arg2);
	}

	public static Currying.F2<Integer, Integer, Integer> multiply() {
		return Currying.curry(Numbers::_multiply);
	}

	public static Maybe<Integer> divide(int arg1, int arg2) {
		return _divide(arg1, arg2);
	}

	public static Currying.F2<Integer, Integer, Maybe<Integer>> divide() {
		return Currying.curry(Numbers::_divide);
	}

	public static Maybe<Integer> modulo(int arg1, int arg2) {
		return _modulo(arg1, arg2);
	}

	public static Currying.F2<Integer, Integer, Maybe<Integer>> modulo() {
		return Currying.curry(Numbers::_modulo);
	}

	private static int _add(int arg1, int arg2) {
		return arg1 + arg2;
	}

	private static int _subtract(int arg1, int arg2) {
		return arg1 - arg2;
	}

	private static int _multiply(int arg1, int arg2) {
		return arg1 * arg2;
	}

	private static Maybe<Integer> _divide(int arg1, int arg2) {
		return arg2 != 0 ? Maybe.just(arg1 / arg2) : Maybe.nothing();
	}

	private static Maybe<Integer> _modulo(int arg1, int arg2) {
		return arg2 != 0 ? Maybe.just(arg1 % arg2) : Maybe.nothing();
	}
}
