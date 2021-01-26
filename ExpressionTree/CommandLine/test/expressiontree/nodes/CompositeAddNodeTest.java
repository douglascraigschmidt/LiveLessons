package expressiontree.nodes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CompositeAddNodeTest extends AbstractBinaryNodeTest {

	@Before
	public void init() {
		node = new CompositeAddNode(leaf, leaf);
	}

	@Test
	public void testNullArg() {
		new CompositeAddNode(null, null);
	}

	@Test
	public void testItem() {
		assertEquals('+', node.item());
	}
}
