package expressiontree.nodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class AbstractBinaryNodeTest {

	protected ComponentNode node;
	protected ComponentNode leaf = new LeafNode(1);

	public abstract void testItem();

	@Test
	public void testLeftChild() {
		assertEquals(leaf, node.left());
	}

	@Test
	public void testRightChild() {
		assertEquals(leaf, node.right());
	}

}