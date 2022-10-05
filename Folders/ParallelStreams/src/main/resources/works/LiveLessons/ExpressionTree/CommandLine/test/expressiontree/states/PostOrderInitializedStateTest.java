package expressiontree.states;

import org.junit.Before;
import org.junit.Test;

public class PostOrderInitializedStateTest extends
		PostOrderUninitializedStateTest {

	@Before
	public void init() {
		super.init();
		state = new PostOrderInitializedState();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test$evaluate() {
		// FIXME IllegalStateException <- IllegalArgumentException. error type
		// should be consistent
		state.evaluate(tree, "");
	}
	
	@Test
	public void test$print() {
		// FIXME make no sense to 'print' without 'makeTree'
		state.print(tree, "");
	}

}
