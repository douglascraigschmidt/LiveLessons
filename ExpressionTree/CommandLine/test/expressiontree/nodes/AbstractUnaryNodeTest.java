package expressiontree.nodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public abstract class AbstractUnaryNodeTest {

	protected ComponentNode node;
	protected ComponentNode leaf = new LeafNode(1);

	public abstract void testItem();

	@Test
	public void testLeftChild() {
		assertNull(node.left());
	}

	@Test
	public void testRightChild() {
		assertEquals(leaf, node.right());
	}

}