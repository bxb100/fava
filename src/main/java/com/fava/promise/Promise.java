package com.fava.promise;

import com.fava.Functions.IF1;
import com.fava.functor.Functor;
import com.fava.monad.Monad;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;
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
 * <p>
 * <strong>THIS IS JUST DEMO FOR PROMISE, YOU SHOULD USE </strong> {@link CompletableFuture}
 * </p>
 *
 * @author dagang.wei (weidagang@gmail.com)
 */
public class Promise<T> implements Functor<T>, Monad<T> {
	private static final ExecutorService ASYNC_POOL;
	// TODO: this barrier may be not necessary.
	private static final byte[] lock = new byte[0];

	static {
		int processors = Runtime.getRuntime().availableProcessors();
		ASYNC_POOL = Executors.newFixedThreadPool(processors * 100);
	}

	private final Deque<Thread> threads = new ConcurrentLinkedDeque<>();
	protected T value;
	protected Exception exception;
	protected List<Listener<T>> listeners = new ArrayList<>();
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

	public static <T> Promise<T> fulfillInAsync(final Callable<T> task) {
		final Promise<T> promise = new Promise<>();
		ASYNC_POOL.execute(() -> {
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
		addListener(consumer, _e -> {
		});
		return this;
	}

	public Promise<T> onFailure(Consumer<Exception> consumer) {
		addListener(_v -> {
		}, consumer);
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
		// TODO: need run in ForkJoinPool?
		synchronized (lock) {
			// In case notifyXxx executed before await
			if (state != State.PENDING) {
				return state == State.SUCCEEDED ? value : null;
			}
		}

		// support multiple threads waiting
		threads.add(Thread.currentThread());

		while (state == State.PENDING) {
			LockSupport.park(this);
		}

		threads.remove();

		return state == State.SUCCEEDED ? value : null;
	}

	public T get() {
		return await();
	}

	/**
	 * Gets the value of this promise.
	 *
	 * <p>Precondition: state == SUCCEEDED || state == FAILED
	 */
	public T getValue() {
		return value;
	}

	protected final void addListener(Consumer<T> success, Consumer<Exception> failure) {
		addListener(new Listener<>() {
			@Override
			public void onSuccess(T value) {
				success.accept(value);
			}

			@Override
			public void onFailure(Exception exception) {
				failure.accept(exception);
			}
		});
	}

	/**
	 * Adds a listener to the promise. If the current state is PENDING, the listener
	 * will be called later on when the promise gets fulfilled to rejected. Otherwise,
	 * the listener will be called immediately.
	 */
	protected final void addListener(Listener<T> listener) {
		synchronized (lock) {
			switch (state) {
				case SUCCEEDED:
					listener.onSuccess(value);
					break;
				case FAILED:
					listener.onFailure(exception);
					break;
				case PENDING:
					listeners.add(listener);
					break;
			}
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
		if (!(obj instanceof Promise)) {
			return false;
		}
		Promise<?> that = (Promise<?>) obj;

		Object v1 = this.await();
		Object v2 = that.await();

		return this.state == that.state && (Objects.equals(v1, v2));
	}

	/**
	 * Fulfills the promise, moves the state from PENDING to SUCCEED. It's intended
	 * to be called inside of subclasses.
	 */
	protected final void notifySuccess(T value) {
		synchronized (lock) {
			this.value = value;
			this.state = State.SUCCEEDED;
		}

		for (Listener<T> listener : listeners) {
			listener.onSuccess(value);
		}
		tryComplete();
	}

	void tryComplete() {
		this.threads.forEach(LockSupport::unpark);
	}

	/**
	 * Rejects the promise, moves the state from PENDING to FAILED. It's intended to be
	 * called inside of subclasses.
	 */
	protected final void notifyFailure(Exception exception) {
		synchronized (lock) {
			this.exception = exception;
			this.state = State.FAILED;
		}

		for (Listener<T> listener : listeners) {
			listener.onFailure(exception);
		}
		tryComplete();
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
