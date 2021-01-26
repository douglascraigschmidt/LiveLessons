package expressiontree.states;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class InOrderUninitializedStateTest extends AbstractStateTest {

	@Before
	public void init() {
		super.init();
		state = new InOrderUninitializedState();
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

}
