package expressiontree.nodes;

import org.junit.Before;
import org.junit.Test;

public class CompositeBinaryNodeTest extends AbstractBinaryNodeTest {

	@Before
	public void init() {
		node = new CompositeBinaryNode(leaf, leaf);
	}

	@Test
	public void testNullArg() {
		node = new CompositeBinaryNode(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testItem() {
		node.item();
	}

}
