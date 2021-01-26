package expressiontree.visitors;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class VisitorFactoryTest {

	private VisitorFactory factory;

	@Before
	public void init() {
		factory = new VisitorFactory();
	}

	@Test(expected = IllegalArgumentException.class)
	public void test$nullArgument() {
		factory.makeVisitor(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test$emptyArgumet() {
		factory.makeVisitor("");
	}

	@Test
	public void test$print() {
		Visitor visitor = factory.makeVisitor("print");
		assertTrue(visitor instanceof PrintVisitor);
	}

	@Test
	public void test$eval() {
		Visitor visitor = factory.makeVisitor("eval");
		assertTrue(visitor instanceof EvaluationVisitor);
	}

}
