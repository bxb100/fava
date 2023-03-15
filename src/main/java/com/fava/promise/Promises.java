package com.fava.promise;

import com.fava.Currying;
import com.fava.Functions.IF1;
import com.fava.Functions.IF2;
import com.fava.data.Lists;
import com.fava.promise.Promise.Listener;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A set of functions for {@link Promise}.
 */
public class Promises {
	/**
	 * Lifts a function of type T -> R into a function of type Promise<T> -> Promise<R>.
	 * It is the function form of {@link Promise<T>::fmap} for composability.
	 */
	public static <T, R> Promise<R> fmap(IF1<T, R> f, Promise<T> promiseT) {
		return promiseT.fmap(f);
	}

	/**
	 * The curried form of {@link Promises#fmap(IF1, Promise))}.
	 */
	public static <T, R> Currying.F2<IF1<T, R>, Promise<T>, Promise<R>> fmap() {
		return Currying.curry((IF2<IF1<T, R>, Promise<T>, Promise<R>>) Promises::fmap);
	}

	/**
	 * The curried form of {@link Promises#fmap(IF1, Promise)}.
	 */
	public static <T, R> Currying.F1<Promise<T>, Promise<R>> fmap(IF1<T, R> f) {
		return Promises.<T, R>fmap().apply(f);
	}

	public static <T1, T2, R> Currying.F2<Promise<T1>, Promise<T2>, Promise<R>> liftA(final Currying.F2<T1, T2, R> f) {
		return new Currying.F2<>() {
			private WeakReference<T1> value1 = new WeakReference<>(null);
			private WeakReference<Exception> e = new WeakReference<>(null);

			@Override
			public Promise<R> apply(Promise<T1> promiseT1, Promise<T2> promiseT2) {

				Promise<R> result = new Promise<>() {
				};
				CountDownLatch cdl = new CountDownLatch(1);

				promiseT1.addListener(v -> {
					value1 = new WeakReference<>(v);
					cdl.countDown();
				}, exception -> {
					e = new WeakReference<>(exception);
					cdl.countDown();
				});

				promiseT2.addListener(v -> {
					try {
						cdl.await();
					} catch (InterruptedException ex) {
						throw new RuntimeException(ex);
					}
					if (e.get() == null) {
						result.notifySuccess(f.apply(value1.get(), v));
					} else {
						result.notifyFailure(e.get());
					}
				}, exception -> {
					try {
						cdl.await();
					} catch (InterruptedException ex) {
						throw new RuntimeException(ex);
					}
					result.notifyFailure(e.get() == null ? exception : e.get());
				});
				return result;
			}
		};
	}

	/**
	 * This method perform like async map
	 */
	public static <T, R> Currying.F1<List<Promise<T>>, Promise<R>> liftA(final Currying.F1<List<T>, R> f) {
		return new Currying.F1<>() {
			@Override
			public Promise<R> apply(final List<Promise<T>> promisesT) {
				CountDownLatch cdl = new CountDownLatch(promisesT.size());
				Exception[] hasFailed = {null};
				for (Promise<T> promiseT : promisesT) {
					promiseT.addListener(
							v -> cdl.countDown(),
							exception -> {
								cdl.countDown();
								hasFailed[0] = exception;
							}
					);
				}
				// block, can also use ForJoinPool.managerBlock() like pseudocode
				// PromiseR res = new PromiseR(); block { res.notifySuccess(f.apply(promisesT.map(Promise::getValue))) }
				return Promise.fulfillInAsync(() -> {
					cdl.await();
					if (hasFailed[0] != null) {
						// don't know how to handle side effects, so we just throw an exception.
						throw new Exception("liftA: some promise failed.", hasFailed[0]);
					} else {
						return f.apply(Lists.map(Promises.getValue(), promisesT));
					}
				});
			}

			@Deprecated
			private void notifyIfDone(List<Promise<T>> promisesT, Promise<R> promiseR) {
				Promise.State state = getState(promisesT);
				if (state == Promise.State.SUCCEEDED) {
					R value = f.apply(Lists.map(Promises.getValue(), promisesT));
					promiseR.notifySuccess(value);
				} else if (state == Promise.State.FAILED) {
					promiseR.notifyFailure(new Exception());
				}
			}

			@Deprecated
			private Promise.State getState(List<Promise<T>> promises) {
				boolean hasFailure = false;
				for (Promise<?> promise : promises) {
					if (promise.state() == Promise.State.PENDING) {
						throw new IllegalStateException("Promise is not done.");
					}

					if (promise.state() == Promise.State.FAILED) {
						hasFailure = true;
					}
				}
				return hasFailure ? Promise.State.FAILED : Promise.State.SUCCEEDED;
			}
		};
	}

	/**
	 * Flattens a promise of promise.
	 * <p>
	 * join :: Promise<Promise<T>> -> Promise<T>
	 */
	public static <T> Promise<T> join(final Promise<Promise<T>> promiseOfPromiseT) {

		final Promise<T> promiseT = new Promise<T>();

		promiseOfPromiseT.addListener(new Listener<>() {
			@Override
			public void onSuccess(Promise<T> p) {
				p.addListener(new Listener<T>() {
					@Override
					public void onSuccess(T value) {
						promiseT.notifySuccess(value);
					}

					@Override
					public void onFailure(Exception exception) {
						promiseT.notifyFailure(exception);
					}
				});
			}

			@Override
			public void onFailure(Exception exception) {
				promiseT.notifyFailure(exception);
			}
		});

		return promiseT;
	}

	/**
	 * Flat-maps a function of type {@code T -> Promise<R>} over an instance of {@code Promise<T>}.
	 *
	 * <p>
	 * flatMap :: (T -> Promise R) -> Promise T -> Promise R
	 *
	 * <p>
	 * There's an invariant among fmap, join and flatMap: _(fmap(f), join) = flagMap(f).
	 */
	public static <T, R> Promise<R> flatMap(IF1<T, Promise<R>> f, Promise<T> promiseT) {
		return promiseT.then(f);
	}

	/**
	 * Curried form of the {@link Promises#flatMap(IF1, Promise)}. This function turns a
	 * function of type {@code T -> Promise<R>} into a function of type {@code Promise T -> Promise R}.
	 */
	public static <T, R> Currying.F1<Promise<T>, Promise<R>> flatMap(IF1<T, Promise<R>> f) {
		return Currying.curry((IF2<IF1<T, Promise<R>>, Promise<T>, Promise<R>>) Promises::flatMap).apply(f);
	}

	/**
	 * Gets value of the promise.
	 */
	public static <T> T getValue(Promise<T> promise) {
		return promise.getValue();
	}

	/**
	 * Curried form of the {@link Promises#getValue(Promise)}.
	 */
	public static <T> IF1<Promise<T>, T> getValue() {
		return Promise::getValue;
	}

	public static <V, I extends Iterable<Callable<V>>> Currying.F1<I, List<Promise<V>>> trySpawnBatch(
			int maxIORequests
	) {
		Semaphore semaphore = new Semaphore(maxIORequests);
		IF1<I, List<Promise<V>>> res = callables -> StreamSupport.stream(
				callables.spliterator(),
				false
		).map(c -> Promise.fulfillInAsync(() -> {
			semaphore.acquire();
			// heavy IO operation
			V v = c.call();
			semaphore.release();
			return v;
		})).collect(Collectors.toList());
		// to stream
		return Currying.curry(res);
	}

	private <T> Listener<T> builderCommonListener() {
		return new Listener<T>() {
			@Override
			public void onSuccess(T value) {

			}

			@Override
			public void onFailure(Exception exception) {

			}
		};
	}
}
