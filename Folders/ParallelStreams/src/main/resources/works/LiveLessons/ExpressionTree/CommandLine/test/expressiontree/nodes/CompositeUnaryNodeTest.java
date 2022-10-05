package expressiontree.nodes;

import org.junit.Before;
import org.junit.Test;

public class CompositeUnaryNodeTest extends AbstractUnaryNodeTest {

	@Before
	public void init() {
		node = new CompositeUnaryNode(leaf);
	}

	@Test
	public void testNullArg() {
		node = new CompositeUnaryNode(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testItem() {
		node.item();
	}
}
