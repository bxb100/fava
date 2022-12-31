package com.fava.data;

import com.fava.Composing;
import com.fava.Currying;
import com.fava.Folding;
import com.fava.Functions;

import java.util.*;

/**
 * Functions for lists.
 *
 * @author dagang.wei (weidagang@gmail.com)
 */
public class Lists {
	/**
	 * Appends an element to a list. This function will not change the
	 * original list, it will create a new list instead.
	 *
	 * <p>append :: T -> [T] -> [T]
	 *
	 * @param <T>     the type of the element
	 * @param element the element
	 * @param list    the list
	 */
	public static <T> List<T> append(T element, List<T> list) {
		ArrayList<T> result = new ArrayList<T>(list);
		result.add(element);
		return result;
	}

	/**
	 * Curried version of append.
	 *
	 * <p>append :: T -> [T] -> [T]
	 */
	public static <T> Currying.F2<T, List<T>, List<T>> append() {
		return new Currying.F2<T, List<T>, List<T>>() {
			@Override
			public List<T> apply(T element, List<T> list) {
				return append(element, list);
			}
		};
	}

	/**
	 * Flattens a list of lists into a single list.
	 *
	 * <p>flatten: [[T]] -> [T]
	 */
	public static <T> List<T> flatten(final List<List<T>> listOfLists) {
		List<T> result = new ArrayList<T>();
		for (List<T> list : listOfLists) {
			result.addAll(list);
		}
		return result;
	}

	/**
	 * Curried version of flatten.
	 *
	 * <p>flatten: [[T]] -> [T]
	 */
	public static <T> Currying.F1<List<List<T>>, List<T>> flatten() {
		return new Currying.F1<List<List<T>>, List<T>>() {
			@Override
			public List<T> apply(List<List<T>> listOfLists) {
				return flatten(listOfLists);
			}
		};
	}

	/**
	 * Curried function for reversing the elements in a list.
	 *
	 * <p>reverse :: [T] -> [T]
	 */
	public static <T> Currying.F1<List<T>, List<T>> reverse() {
		return Folding.foldr(Lists.append(), new ArrayList<T>());
	}

	/**
	 * Sorts a list.
	 *
	 * <p>sort :: (T -> T -> Int) -> [T] -> [T]
	 */
	public static <T> List<T> sort(final Currying.F2<T, T, Integer> comparator, final List<T> list) {
		ArrayList<T> result = new ArrayList<T>(list);
		Collections.sort(result, new Comparator<T>() {
			@Override
			public int compare(T arg1, T arg2) {
				return comparator.apply(arg1, arg2);
			}
		});
		return result;
	}

	/**
	 * Curried version of sort.
	 *
	 * <p>sort :: (T -> T -> Int) -> [T] -> [T]
	 */
	public static <T> Currying.F2<Currying.F2<T, T, Integer>, List<T>, List<T>> sort() {
		return new Currying.F2<Currying.F2<T, T, Integer>, List<T>, List<T>>() {
			@Override
			public List<T> apply(final Currying.F2<T, T, Integer> comparator, final List<T> list) {
				return sort(comparator, list);
			}
		};
	}

	/**
	 * Curried version of sort with partial application serving as syntax sugar.
	 *
	 * <p>sort :: (T -> T -> Int) -> [T] -> [T]
	 */
	public static <T> Currying.F1<List<T>, List<T>> sort(Currying.F2<T, T, Integer> comparator) {
		return Lists.<T>sort().apply(comparator);
	}

	/**
	 * Checks if any of the element in a list matches the condition.
	 *
	 * <p>exists :: (T -> Boolean) -> [T] -> Boolean
	 */
	public static <T> boolean exists(Currying.F1<T, Boolean> predicate, List<T> list) {
		for (T element : list) {
			if (predicate.apply(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Curried version of {@link exists}.
	 */
	public static <T> Currying.F2<Currying.F1<T, Boolean>, List<T>, Boolean> exists() {
		return new Currying.F2<Currying.F1<T, Boolean>, List<T>, Boolean>() {
			@Override
			public Boolean apply(Currying.F1<T, Boolean> predicate, List<T> list) {
				return exists(predicate, list);
			}
		};
	}

	/**
	 * Maps a function over the elements of a list.
	 *
	 * <p> map: (T -> R) -> [T] -> [R]
	 *
	 * @param <T>  the type of elements in the source list
	 * @param <R>  the type of elements in the result list
	 * @param f    the function to be applied to each element of the list
	 * @param list the list to be mapped over
	 */
	public static <T, R> List<R> map(final Functions.IF1<T, R> f, List<T> list) {
		final ArrayList<R> result = new ArrayList<R>(list.size());
		// Here we define {@code map} with {@code foldl}, that means {@code fold}
		// is more fundamental than {@code map} in the level of abstraction.
		return Folding.foldl(Composing.__(f, Lists.append()), result, list);
	}

	/**
	 * Curried version of mapn.
	 */
	public static <T, R> Currying.F2<Functions.IF1<T, R>, List<T>, List<R>> map() {
		return new Currying.F2<Functions.IF1<T, R>, List<T>, List<R>>() {
			@Override
			public List<R> apply(Functions.IF1<T, R> f, List<T> list) {
				return map(f, list);
			}
		};
	}

	/**
	 * Curried version of map with partial application serving as syntax sugar.
	 */
	public static <T, R> Currying.F1<List<T>, List<R>> map(Functions.IF1<T, R> f) {
		return Lists.<T, R>map().apply(f);
	}

	/**
	 * Maps a function of type "T -> [R]" over the elements of a list, then
	 * flatten the results into a single list of type [R].
	 *
	 * <p> map: (T -> [R]) -> [T] -> [R]
	 *
	 * @param <T>  the type of elements in the source list
	 * @param <R>  the type of elements in the result list
	 * @param f    the function to be applied to each element of the list
	 * @param list the list to be mapped over
	 */
	public static <T, R> List<R> flatMap(final Functions.IF1<T, List<R>> f, List<T> list) {
		return flatten(map(f, list));
	}

	/**
	 * Curried version of flatMap.
	 */
	public static <T, R> Currying.F2<Functions.IF1<T, List<R>>, List<T>, List<R>> flatMap() {
		return new Currying.F2<Functions.IF1<T, List<R>>, List<T>, List<R>>() {
			@Override
			public List<R> apply(Functions.IF1<T, List<R>> arg1, List<T> arg2) {
				return flatMap(arg1, arg2);
			}
		};
	}

	/**
	 * Curried version of flatMap with partial application serving as syntax sugar.
	 */
	public static <T, R> Currying.F1<List<T>, List<R>> flatMap(Functions.IF1<T, List<R>> f) {
		return Lists.<T, R>flatMap().apply(f);
	}

	/**
	 * Returns a list of the unique elements of another list. It will preserve the
	 * order between the elements.
	 *
	 * <p>Time complexity: O(n)
	 *
	 * <p> unique :: [T] -> [T]
	 */
	public static <T> List<T> unique(List<T> list) {

		Set<T> set = new LinkedHashSet<>(list);
		return new ArrayList<>(set);
	}

	/**
	 * Curried version of {@link Lists#unique(List)}.
	 */
	public static <T> Currying.F1<List<T>, List<T>> unique() {
		return new Currying.F1<>() {
			@Override
			public List<T> apply(List<T> list) {
				return unique(list);
			}
		};
	}
}
