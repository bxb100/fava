package fava.data;

import fava.Currying;
import fava.Currying.F1;
import fava.Currying.F2;
import fava.Functions.IF2;

import java.util.Arrays;
import java.util.List;

/**
 * Functions for strings
 *
 * @author dagang.wei (weidagang@gmail.com)
 **/
public class Strings {

	/**
	 * Curried function for splitting a string by a delimiter.
	 *
	 * <p> split :: String -> String -> [String]
	 */
	@SuppressWarnings("CommentedOutCode")
	public static F2<String, String, List<String>> split() {

		return new F2<>() {
			@Override
			public List<String> apply(String delimiter, String data) {
				return Arrays.asList(data.split(delimiter));
			}
		};

		/*return new F2<>() {
			@Override
			public List<String> apply(String arg1, String arg2) {
				return Arrays.asList(Flipping.flip(Currying.<String, String, String[]>curry(String::split)).apply(arg1, arg2));
			}
		};*/

		/*return Composing.__(
				Flipping.flip(Currying.<String, String, String[]>curry(String::split)),
				Currying.<String[], List<String>>curry(Arrays::asList)
		);*/
	}

	/**
	 * Curried form of split with partial application as syntax sugar.
	 *
	 * <p> splitBy :: String -> String -> [String]
	 */
	public static F1<String, List<String>> split(String delimiter) {
		return split().apply(delimiter);
	}

	public static String concat(String arg1, String arg2) {
		return arg1 + arg2;
	}

	/**
	 * Curried function for concatenating two strings.
	 */
	public static F2<String, String, String> concat() {
		return Currying.curry(Strings::concat);
	}

	/**
	 * Joins a list of strings by a delimiter.
	 */
	public static String join(String delimiter, List<String> strings) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < strings.size(); i++) {
			if (i > 0) {
				builder.append(delimiter);
			}
			builder.append(strings.get(i));
		}
		return builder.toString();
	}

	/**
	 * Curried form of {@link Strings#join(String, List)}.
	 *
	 * <p> join :: String -> [String] -> String
	 */
	public static F2<String, List<String>, String> join() {
		return Currying.curry((IF2<String, List<String>, String>) Strings::join);
	}

	/**
	 * Curried form of {@link Strings#join()} with partial application as syntax suger.
	 *
	 * <p> join :: String -> [String] -> String
	 *
	 * @param delimiter the delimiter
	 */
	public static F1<List<String>, String> join(final String delimiter) {

		return new F1<>() {
			/**
			 * @param strings the string list
			 */
			@Override
			public String apply(List<String> strings) {
				return join(delimiter, strings);
			}
		};
	}

	/**
	 * Curried function for converting a string to upper case.
	 *
	 * <p> toUpperCase :: String -> String
	 */
	public static F1<String, String> toUpperCase() {
		return new F1<String, String>() {
			@Override
			public String apply(String str) {
				return str.toUpperCase();
			}
		};
	}

	/**
	 * Curried function for converting a string to lower case.
	 *
	 * <p> toLowerCase :: String -> String
	 */
	public static F1<String, String> toLowerCase() {
		return new F1<String, String>() {
			@Override
			public String apply(String str) {
				return str.toLowerCase();
			}
		};
	}

	/**
	 * Curried function for comparing two strings lexicographically.
	 *
	 * @return the value 0 if the argument string is equal to this
	 * string; a value less than 0 if this string is lexicographically
	 * less than the string argument; and a value greater than 0 if
	 * this string is lexicographically greater than the string argument.
	 */
	public static F2<String, String, Integer> compare() {
		return new F2<String, String, Integer>() {
			@Override
			public Integer apply(String str1, String str2) {
				return str1.compareTo(str2);
			}
		};
	}

	/**
	 * Curried function for comparing two strings lexicographically,
	 * ignoring case differences.
	 *
	 * @return the value 0 if the argument string is equal to this
	 * string; a value less than 0 if this string is lexicographically
	 * less than the string argument; and a value greater than 0 if
	 * this string is lexicographically greater than the string argument.
	 */
	public static F2<String, String, Integer> compareIgnoreCase() {
		return new F2<>() {
			@Override
			public Integer apply(String str1, String str2) {
				return str1.compareToIgnoreCase(str2);
			}
		};
	}

	/**
	 * Repeats the {@code str} {@code n} times.
	 */
	public static String times(int n, String str) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < n; i++) {
			builder.append(str);
		}
		return builder.toString();
	}

	/**
	 * Curried form of {@link Strings#times(int, String)}.
	 */
	public static F2<Integer, String, String> times() {
		return new F2<>() {
			@Override
			public String apply(Integer n, String str) {
				return times(n, str);
			}
		};
	}
}
