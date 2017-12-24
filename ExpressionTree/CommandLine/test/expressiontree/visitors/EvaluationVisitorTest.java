package expressiontree.visitors;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import expressiontree.nodes.CompositeAddNode;
import expressiontree.nodes.CompositeDivideNode;
import expressiontree.nodes.CompositeMultiplyNode;
import expressiontree.nodes.CompositeNegateNode;
import expressiontree.nodes.CompositeSubtractNode;
import expressiontree.nodes.LeafNode;

public class EvaluationVisitorTest {

	private static final int DEFAULT_TOTAL = 0;
	private static final LeafNode LEAF = new LeafNode(1);
	private EvaluationVisitor visitor;

	@Before
	public void init() {
		visitor = new EvaluationVisitor();
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
	public void test$CompositeAddNode() {
		visitor.visit(LEAF);
		visitor.visit(LEAF);
		visitor.visit(new CompositeAddNode(LEAF, LEAF));
		assertEquals(2, visitor.total());
	}

	@Test
	public void test$CompositeSubtractNode() {
		visitor.visit(LEAF);
		visitor.visit(LEAF);
		visitor.visit(new CompositeSubtractNode(LEAF, LEAF));
		assertEquals(0, visitor.total());
	}

	@Test
	public void test$CompositeDivideNode() {
		visitor.visit(LEAF);
		visitor.visit(LEAF);
		visitor.visit(new CompositeDivideNode(LEAF, LEAF));
		assertEquals(1, visitor.total());
	}

	@Test
	public void test$CompositeMultiplyNode() {
		visitor.visit(LEAF);
		visitor.visit(LEAF);
		visitor.visit(new CompositeMultiplyNode(LEAF, LEAF));
		assertEquals(1, visitor.total());
	}


}
