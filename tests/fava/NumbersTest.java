package fava;

import fava.data.Numbers;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class NumbersTest {

	@Test
	public void testAdd() {
		Currying.F2<Integer, Integer, Integer> add = Currying.curry(Numbers::add);
		int res = add.apply(Numbers.add(5, 6)).apply(1);
		assertEquals(12, res);
	}
}
