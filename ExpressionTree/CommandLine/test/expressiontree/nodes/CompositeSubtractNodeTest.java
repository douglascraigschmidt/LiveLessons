package expressiontree.nodes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CompositeSubtractNodeTest extends AbstractBinaryNodeTest {

	@Before
	public void init() {
		node = new CompositeSubtractNode(leaf, leaf);
	}

	@Test
	public void testNullArg() {
		new CompositeSubtractNode(null, null);
	}

	@Test
	public void testItem() {
		assertEquals('-', node.item());
	}

}
