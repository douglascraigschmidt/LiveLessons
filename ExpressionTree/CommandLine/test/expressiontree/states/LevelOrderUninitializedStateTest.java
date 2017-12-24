package expressiontree.states;

import org.junit.Before;
import org.junit.Test;

public class LevelOrderUninitializedStateTest extends AbstractStateTest {

	@Before
	public void init() {
		super.init();
		state = new LevelOrderUninitializedState();
	}

	@Test
	public void test$format() {
		state.format(tree, "");
		// TODO change mState visibility to be able to assert
	}

	@Test
	public void test$format$inOrder() {
		state.format(tree, "in-order");
		// TODO change mState visibility to be able to assert
	}

	@Test(expected = IllegalArgumentException.class)
	public void test$format$notImplemented() {
		state.format(tree, "bla-bla");
	}

}
