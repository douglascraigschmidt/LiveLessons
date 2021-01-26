package expressiontree.nodes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CompositeMultiplyNodeTest extends AbstractBinaryNodeTest {

	@Before
	public void init() {
		node = new CompositeMultiplyNode(leaf, leaf);
	}

	@Test
	public void testNullArg() {
		new CompositeMultiplyNode(null, null);
	}

	@Test
	public void testItem() {
		assertEquals('*', node.item());
	}

}
