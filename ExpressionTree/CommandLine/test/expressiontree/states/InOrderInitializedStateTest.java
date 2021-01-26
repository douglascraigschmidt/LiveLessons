package expressiontree.states;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class InOrderInitializedStateTest extends InOrderUninitializedStateTest {

	@Before
	public void init() {
		super.init();
		state = new InOrderInitializedState();
	}

	@Test
	public void test$format() {
		state.format(tree, "");
		// TODO change mState visibility to be able to assert
	}

	@Test
	public void test$makeTree() {
		state.makeTree(tree, "");
		assertNotNull(tree.tree());
		assertTrue(tree.tree().isNull());
	}

	@Test
	public void test$print() {
		state.print(tree, "");
	}

	@Test
	public void test$evaluate() {
		state.makeTree(tree, "1+1");
		state.evaluate(tree, "post-order");
		assertEquals("2", out.toString().trim());
	}

}
