package fava;

import fava.Currying.F1;
import fava.Currying.F2;
import fava.data.Lists;
import fava.data.Strings;
import fava.promise.Promise;
import fava.promise.Promises;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static fava.data.Lists.map;
import static fava.data.Strings.join;
import static fava.data.Strings.*;
import static fava.promise.Promise.failure;
import static fava.promise.Promise.unit;
import static fava.promise.Promises.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PromiseTest {
	private static final String URL1 = "http://www.a.com/a.htm";
	private static final String URL2 = "http://www.b.com/b.htm";
	private static final String URL3 = "http://www.c.com/c.htm";
	private static final String URL4 = "http://www.d.com/d.htm";
	private static final String URL5 = "http://www.e.com/e.htm";
	private static final String URL6 = "http://www.f.com/f.htm";
	private static final String PAGE1 = "Hello world";
	private static final String PAGE2 = "I love programming in Java";
	private static final String PAGE3 = "Fava is Functional Java";
	private static final String PAGE4 = URL5;
	private static final String PAGE5 = URL6;
	private static final String PAGE6 = "You're here!";

	private static HttpPromise asyncGet(String url) {
		return new HttpPromise(url);
	}

	@Test
	public void testPromise_fmap() throws Exception {
		F1<List<String>, List<String>> reverse = Lists.reverse();
		F1<String, String> toUpperCase = toUpperCase();
		F1<String, String> convert = Composing._do(split(" "), reverse, map(toUpperCase), join("_"));
		Promise<String> page2 = asyncGet(URL2);

		// fmap turns a function of type "T -> R" into a function of type "Promise<T> -> Promise<R>"
		F1<Promise<String>, Promise<String>> convertForPromise = fmap(convert);
		assertEquals("JAVA_IN_PROGRAMMING_LOVE_I", convertForPromise.apply(page2).await());
	}

	/**
	 * Tests functor law: fmap id = id
	 */
	@Test
	public void testPromise_functorLaw1() {
		F1<Promise<String>, Promise<String>> id = Promises.fmap(Identity::id);
		assertEquals(unit("foo"), id.apply(unit("foo")));
		assertEquals(failure(new RuntimeException("xxx")), id.apply(failure(new RuntimeException("xxx"))));
	}

	/**
	 * Tests functor law: fmap (p . q) = (fmap p) . (fmap q)
	 */
	@Test
	public void testPromise_functorLaw2() throws Exception {
		F1<String, List<String>> splitByComman = split(",");
		F1<List<String>, String> joinByUnderscore = join("_");
		String data = "I,love,Java";
		assertEquals(fmap(Composing._do(splitByComman, joinByUnderscore)).apply(unit(data)).await(), "I_love_Java");
		assertEquals(
				fmap(Composing._do(splitByComman, joinByUnderscore)).apply(unit(data)),
				Composing._do(fmap(splitByComman), fmap(joinByUnderscore)).apply(unit(data)));
	}

	/**
	 * This test case concatenates 2 web pages which are asynchronously fetched
	 * from the Internet. It's to demonstrate lifting a function of type
	 * "String -> String" into a function of type "Promise<String> -> Promise<String>".
	 */
	@Test
	public void testPromise_liftA() throws Exception {
		Promise<String> page1 = asyncGet(URL1);
		Promise<String> page2 = asyncGet(URL2);

		// liftA lifts concat into concatPromise. This allows us to abstract away
		// the asynchronous callbacks from the scene, consequently concatenating 2 asynchronous
		// strings looks the same as concatenating 2 regular strings.
		F2<Promise<String>, Promise<String>, Promise<String>> concatPromise = liftA(concat());
		Promise<String> page1AndPage2 = concatPromise.apply(page1, page2);
		assertEquals("Hello worldI love programming in Java", page1AndPage2.await());
	}

	/**
	 * This test case concatenates a list of web pages asynchronously fetched
	 * from the Internet. It's to demonstrate lifting a function of type
	 * "List<T> -> R" into a function of type "List<Promise<T>> -> Promise<R>"
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testPromise_liftAForList() throws Exception {
		Promise<String> page1 = asyncGet(URL1);
		Promise<String> page2 = asyncGet(URL2);
		Promise<String> page3 = asyncGet(URL3);
		F1<List<String>, String> join = join(",");
		F1<List<List<String>>, List<String>> flatten = Lists.flatten();
		F1<List<String>, List<String>> unique = Lists.unique();
		F2<String, String, Integer> compareIgnoreCase = Strings.compareIgnoreCase();
		F1<List<String>, List<String>> sort = Lists.sort(compareIgnoreCase);
		F1<String, List<String>> split = split(" ");

		List<Promise<String>> promises = asList(page1, page2, page3);
		String r = liftA(join).apply(promises).await();
		assertEquals(PAGE1 + "," + PAGE2 + "," + PAGE3, r);

		F1<List<String>, Promise<String>> f2 = Composing._do(
				map(PromiseTest::asyncGet),
				map(fmap(split)),
				liftA(flatten),
				fmap(Composing._do(unique, sort, join)));
		String result2 = f2.apply(asList(URL1, URL2, URL3)).await();
		System.out.println(result2);
	}

	/**
	 * This test case will do 3 chained async HTTP GETs. The contents of the previous
	 * page is the URL of the next page.
	 *
	 * <p>This test case demonstrated the monadic way of turning an async code into a
	 * sync code. There're 3 async HTTP operations involved, but you see no callbacks.
	 */
	@Test
	public void testPromise_bind_flatMap() {
		// The following 2 forms are equivalent. I personally prefer the second form.

		// 1) bind chain
		{
			String result = asyncGet(URL4).bind(PromiseTest::asyncGet).bind(PromiseTest::asyncGet).await();
			assertEquals(PAGE6, result);
		}

		// 2) flatMap (it's essentially the bind operator of Monad with different parameter order)
		{
			// flatMap lifts a function of type T -> Promise<R> into a function of type
			// Promise<T> -> Promise<R>.
			F1<Promise<String>, Promise<String>> liftedAsyncGet = flatMap(PromiseTest::asyncGet);

			Promise<String> page4 = asyncGet(URL4); // the contents of page4 is the url of page5
			Promise<String> page5 = liftedAsyncGet.apply(page4); // the contents of page5 is the url of page6
			Promise<String> page6 = liftedAsyncGet.apply(page5); // the contents of page6 is the final result
			assertEquals(PAGE6, page6.await());
		}
	}

	/**
	 * Tests the invariant among {code fmap}, {code join} and {code bind}:
	 * <p>
	 * _(fmap(f), join) = flatMap(f)
	 */
	@Test
	public void testPromise_fmap_join_flatMap_invariant() {
		// _(fmap(f), join)
		F1<Promise<String>, Promise<String>> liftedAsyncGet1 = Composing
				.<Promise<String>, Promise<Promise<String>>, Promise<String>>_do(fmap(PromiseTest::asyncGet), Promises::join);

		// flatMap(f)
		F1<Promise<String>, Promise<String>> liftedAsyncGet2 = flatMap(PromiseTest::asyncGet);

		assertEquals(
				liftedAsyncGet1.apply(asyncGet(URL4)).await(),
				liftedAsyncGet2.apply(asyncGet(URL4)).await());
		assertEquals(
				liftedAsyncGet1.apply(asyncGet(URL5)).await(),
				liftedAsyncGet2.apply(asyncGet(URL5)).await());
		assertEquals(
				liftedAsyncGet1.apply(asyncGet(URL6)).await(),
				liftedAsyncGet2.apply(asyncGet(URL6)).await());
	}

	@Test
	public void testPromise_async() {
		HttpRequest request = HttpRequest.newBuilder(URI.create("https://google.com")).build();
		HttpClient client = HttpClient.newBuilder().build();
		Callable<String> action = () -> client.send(request, HttpResponse.BodyHandlers.ofString()).body();

		assertNotNull(
				Promise.fulfillInAsync(action, Executors.newSingleThreadExecutor())
						.onSuccess(System.out::println)
						.onFailure(System.err::println)
						.await()
		);
	}

	/**
	 * Fake HTTP promise for test purpose. It either returns a pre-configured web
	 * page asynchronously or throws a 404 NOT FOUND exception.
	 */
	private static class HttpPromise extends Promise<String> {
		private static final HashMap<String, String> pages = new HashMap<String, String>();

		static {
			pages.put(URL1, PAGE1);
			pages.put(URL2, PAGE2);
			pages.put(URL3, PAGE3);
			pages.put(URL4, PAGE4);
			pages.put(URL5, PAGE5);
			pages.put(URL6, PAGE6);
		}

		public HttpPromise(final String url) {
			final long interval = 100;
			// Simulate asynchronous HTTP request of 100 ms with thread.
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (pages.containsKey(url)) {
						HttpPromise.this.notifySuccess(pages.get(url));
					} else {
						HttpPromise.this.notifyFailure(new Exception("404 NOT FOUND"));
					}
				}
			})
					.start();
		}
	}
}
