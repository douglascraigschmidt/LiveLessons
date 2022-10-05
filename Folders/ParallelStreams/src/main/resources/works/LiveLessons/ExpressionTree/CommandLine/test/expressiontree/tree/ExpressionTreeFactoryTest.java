package expressiontree.tree;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ExpressionTreeFactoryTest {
	private ExpressionTreeFactory factory;

	@Before
	public void init() {
		factory = new ExpressionTreeFactory();
	}

	@Test
	public void test() {
		ExpressionTree expTree = factory.makeExpressionTree(null);
		assertTrue(expTree.isNull());
	}

}
