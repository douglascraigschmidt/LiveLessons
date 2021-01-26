package expressiontree.nodes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CompositeDivideNodeTest extends AbstractBinaryNodeTest {

	@Before
	public void init() {
		node = new CompositeDivideNode(leaf, leaf);
	}

	@Test
	public void testNullArg() {
		new CompositeDivideNode(null, null);
	}

	@Test
	public void testItem() {
		assertEquals('/', node.item());
	}

}
