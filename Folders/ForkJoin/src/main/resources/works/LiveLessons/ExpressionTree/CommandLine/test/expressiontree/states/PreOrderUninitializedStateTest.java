package expressiontree.states;

import org.junit.Before;
import org.junit.Test;

public class PreOrderUninitializedStateTest extends AbstractStateTest {

	@Before
	public void init() {
		super.init();
		state = new PreOrderUninitializedState();
	}

	@Test
	public void test$format() {
		state.format(tree, "");
		// TODO change mState visibility to be able to assert
	}

}
