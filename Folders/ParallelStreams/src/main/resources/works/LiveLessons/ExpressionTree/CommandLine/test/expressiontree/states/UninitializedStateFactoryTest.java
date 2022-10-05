package expressiontree.states;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class UninitializedStateFactoryTest {

	private UninitializedStateFactory factory;

	@Before
	public void init() {
		factory = new UninitializedStateFactory();
	}

	@Test(expected = IllegalArgumentException.class)
	public void test$unImplemented() {
		factory.makeUninitializedState("bla-bla");
	}

	@Test
	public void test$inOrder() {
		State state = factory.makeUninitializedState("in-order");
		assertTrue(state instanceof InOrderUninitializedState);
	}

	@Test
	public void test$preOrder() {
		State state = factory.makeUninitializedState("pre-order");
		assertTrue(state instanceof PreOrderUninitializedState);
	}

	@Test
	public void test$postOrder() {
		State state = factory.makeUninitializedState("post-order");
		assertTrue(state instanceof PostOrderUninitializedState);
	}

	@Test
	public void test$levelOrder() {
		State state = factory.makeUninitializedState("level-order");
		assertTrue(state instanceof LevelOrderUninitializedState);
	}

}
