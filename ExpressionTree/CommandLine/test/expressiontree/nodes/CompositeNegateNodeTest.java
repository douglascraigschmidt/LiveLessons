package expressiontree.nodes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CompositeNegateNodeTest extends AbstractUnaryNodeTest {

	@Before
	public void init() {
		node = new CompositeNegateNode(leaf);
	}

	@Test
	public void testNullArg() {
		new CompositeNegateNode(null);
	}

	@Test
	public void testItem() {
		assertEquals('-', node.item());
	}

}
