package com.fava.promise;

import com.fava.functor.Functor;
import com.fava.Functions.IF1;
import com.fava.monad.Monad;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * An instance of {@code Promise<T>} represents a value of type T that may be
 * available asynchronously in the future. Users of a promise get the value
 * or failure info by registering listeners.
 *
 * <p>This class is intended to be inherited by subclasses to provide specific
 * asynchronous values, such as asynchronous HTTP response or asynchronous
 * database query result.
 *
 * @author dagang.wei (weidagang@gmail.com)
 */
public class Promise<T> implements Functor<T>, Monad<T> {
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();

	protected T value;
	protected Exception exception;
	protected ArrayList<Listener<T>> listeners = new ArrayList<>();
	private volatile State state = State.PENDING;

	/**
	 * Lifts a value into a promise.
	 */
	public static <T> Promise<T> unit(T value) {
		Promise<T> promise = new Promise<>();
		promise.state = State.SUCCEEDED;
		promise.value = value;
		return promise;
	}

	/**
	 * Lifts a failure into a promise.
	 */
	public static <T> Promise<T> failure(Exception exception) {
		Promise<T> promise = new Promise<>();
		promise.state = State.FAILED;
		promise.exception = exception;
		return promise;
	}

	public static <T> Promise<T> fulfillInAsync(final Callable<T> task, Executor executor) {
		final Promise<T> promise = new Promise<>();
		executor.execute(() -> {
			try {
				T value = task.call();
				promise.notifySuccess(value);
			} catch (Exception e) {
				promise.notifyFailure(e);
			}
		});
		return promise;
	}

	public Promise<T> onSuccess(Consumer<T> consumer) {
		addListener(new Listener<>() {
			@Override
			public void onSuccess(T value) {
				consumer.accept(value);
			}

			@Override
			public void onFailure(Exception exception) {
			}
		});
		return this;
	}

	public Promise<T> onFailure(Consumer<Exception> consumer) {
		addListener(new Listener<>() {
			@Override
			public void onSuccess(T value) {
			}

			@Override
			public void onFailure(Exception exception) {
				consumer.accept(exception);
			}
		});
		return this;
	}

	/**
	 * Returns the current state of the promise.
	 */
	public State state() {
		return state;
	}

	/**
	 * wait until the promise is fulfilled or rejected.
	 *
	 * @return the value if succeeded, or null if failed.
	 */
	public T await() {

		lock.lock();
		try {
			while (state == State.PENDING) {
				try {
					condition.await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		} finally {
			lock.unlock();
		}

		return state == State.SUCCEEDED ? value : null;
	}

	/**
	 * Gets the value of this promise.
	 *
	 * <p>Precondition: state == SUCCEEDED || state == FAILED
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Adds a listener to the promise. If the current state is PENDING, the listener
	 * will be called later on when the promise gets fulfilled to rejected. Otherwise,
	 * the listener will be called immediately.
	 */
	protected final void addListener(Listener<T> listener) {
		lock.lock();
		try {
			switch (state) {
				case SUCCEEDED -> listener.onSuccess(value);
				case FAILED -> listener.onFailure(exception);
				case PENDING -> listeners.add(listener);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public <R> Promise<R> fmap(IF1<T, R> f) {
		final Promise<R> promiseR = new Promise<>() {
		};

		this.addListener(new Listener<>() {
			@Override
			public void onSuccess(T value) {
				promiseR.notifySuccess(f.apply(value));
			}

			@Override
			public void onFailure(Exception exception) {
				promiseR.notifyFailure(exception);
			}
		});

		return promiseR;
	}

	@Override
	public <R> Promise<R> then(IF1<T, ? extends Monad<R>> f) {
		// promiseR is the composition of "this" promise and "that promise.
		final Promise<R> promiseR = new Promise<>() {
		};

		// callback for "this" promise
		this.addListener(new Listener<>() {
			@Override
			public void onSuccess(T value) {
				Promise<R> that = (Promise<R>) f.apply(value);
				// callback for "that" promise
				that.addListener(new Listener<>() {
					@Override
					public void onSuccess(R value) {
						promiseR.notifySuccess(value);
					}

					@Override
					public void onFailure(Exception exception) {
						promiseR.notifyFailure(exception);
					}
				});
			}

			@Override
			public void onFailure(Exception exception) {
				promiseR.notifyFailure(exception);
			}
		});

		return promiseR;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Promise<?> that)) {
			return false;
		}

		Object v1 = this.await();
		Object v2 = that.await();

		return this.state == that.state && (Objects.equals(v1, v2));
	}

	/**
	 * Fulfills the promise, moves the state from PENDING to SUCCEED. It's intended
	 * to be called inside of subclasses.
	 */
	protected final void notifySuccess(T value) {
		lock.lock();

		try {
			this.value = value;
			this.state = State.SUCCEEDED;
			for (Listener<T> listener : listeners) {
				listener.onSuccess(value);
			}

			condition.signalAll();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Rejects the promise, moves the state from PENDING to FAILED. It's intended to be
	 * called inside of subclasses.
	 */
	protected final void notifyFailure(Exception exception) {
		lock.lock();

		try {
			this.exception = exception;
			this.state = State.FAILED;
			for (Listener<T> listener : listeners) {
				listener.onFailure(exception);
			}
			condition.signalAll();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * States of a promise.
	 */
	public enum State {
		PENDING,
		SUCCEEDED,
		FAILED,
	}

	/**
	 * Listener to get the value or failure info in the future.
	 */
	public interface Listener<T> {
		void onSuccess(T value);

		void onFailure(Exception exception);
	}
}
