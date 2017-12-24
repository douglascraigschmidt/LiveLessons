package expressiontree.visitors;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import expressiontree.nodes.CompositeAddNode;
import expressiontree.nodes.CompositeDivideNode;
import expressiontree.nodes.CompositeMultiplyNode;
import expressiontree.nodes.CompositeNegateNode;
import expressiontree.nodes.CompositeSubtractNode;
import expressiontree.nodes.LeafNode;
import expressiontree.platspecs.Platform;
import expressiontree.platspecs.PlatformFactory;

public class EvaluationVisitorTest {

	private static final int DEFAULT_TOTAL = 0;
	private static final LeafNode LEAF = new LeafNode(1);

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	private EvaluationVisitor visitor;

	@Before
	public void init() {
		// FIXME dependend on Global
		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(System.in, new PrintStream(out),
				null).makePlatform());

		visitor = new EvaluationVisitor();
	}
	
	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void test$LeafNode() {
		visitor.visit(LEAF);
		assertEquals(LEAF.item(), visitor.total());
	}

	@Test
	public void test$CompositeNegateNode$default() {
		visitor.visit(new CompositeNegateNode(null));
		assertEquals(DEFAULT_TOTAL, visitor.total());
	}

	@Test
	public void test$CompositeNegateNode() {
		visitor.visit(LEAF);
		visitor.visit(new CompositeNegateNode(LEAF));
		assertEquals(-1, visitor.total());
	}

	@Test
	public void test$CompositeNegateNode$minInteger() {
		LeafNode leaf = new LeafNode(Integer.MIN_VALUE);
		visitor.visit(leaf);
		visitor.visit(new CompositeNegateNode(leaf));
		assertEquals(-Integer.MIN_VALUE, visitor.total());
	}
	
	@Test
	public void test$CompositeNegateNode$maxInteger() {
		LeafNode leaf = new LeafNode(Integer.MAX_VALUE);
		visitor.visit(leaf);
		visitor.visit(new CompositeNegateNode(leaf));
		assertEquals(-Integer.MAX_VALUE, visitor.total());
	}

	@Test
	public void test$CompositeAddNode() {
		visitor.visit(LEAF);
		visitor.visit(LEAF);
		visitor.visit(new CompositeAddNode(LEAF, LEAF));
		assertEquals(2, visitor.total());
	}
	
	@Test
	public void test$CompositeAddNode$integerOverflow() {
		LeafNode leaf = new LeafNode(Integer.MAX_VALUE);
		visitor.visit(leaf);
		visitor.visit(LEAF);
		visitor.visit(new CompositeAddNode(leaf, LEAF));
		assertEquals(-Integer.MIN_VALUE, visitor.total());
	}

	@Test
	public void test$CompositeSubtractNode() {
		visitor.visit(LEAF);
		visitor.visit(LEAF);
		visitor.visit(new CompositeSubtractNode(LEAF, LEAF));
		assertEquals(0, visitor.total());
	}
	
	@Test
	public void test$CompositeSubtractNode$integerOverflow() {
		LeafNode leaf = new LeafNode(Integer.MIN_VALUE);
		visitor.visit(leaf);
		visitor.visit(LEAF);
		visitor.visit(new CompositeSubtractNode(leaf, LEAF));
		assertEquals(Integer.MAX_VALUE, visitor.total());
	}

	@Test
	public void test$CompositeDivideNode() {
		visitor.visit(LEAF);
		visitor.visit(LEAF);
		visitor.visit(new CompositeDivideNode(LEAF, LEAF));
		assertEquals(1, visitor.total());
	}

	@Test
	public void test$CompositeDivideNode$divideByZero() {
		// FIXME replace error log with exception
		visitor.visit(LEAF);
		visitor.visit(new LeafNode(0));
		visitor.visit(new CompositeDivideNode(LEAF, LEAF));
		assertEquals(0, visitor.total());
		assertEquals(
				"EvaluationVisitor" + " "
						+ "\n\n**: Division by zero is not allowed. "
						+ System.lineSeparator() + "EvaluationVisitor" + " "
						+ "Resetting evaluation visitor.\n\n"
						+ System.lineSeparator(), out.toString());
	}

	@Test
	public void test$CompositeMultiplyNode() {
		visitor.visit(LEAF);
		visitor.visit(LEAF);
		visitor.visit(new CompositeMultiplyNode(LEAF, LEAF));
		assertEquals(1, visitor.total());
	}

}
