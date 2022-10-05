package expressiontree.states;

import org.junit.Before;
import org.junit.Test;

public class LevelOrderInitializedStateTest extends
		LevelOrderUninitializedStateTest {

	@Before
	public void init() {
		super.init();
		state = new LevelOrderInitializedState();
	}

	@Test
	public void test$print() {
		// FIXME make no sense to 'print' without 'makeTree'
		state.print(tree, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void test$evaluate() {
		// FIXME IllegalStateException <- IllegalArgumentException. error type
		// should be consistent
		state.evaluate(tree, "");
	}

}
