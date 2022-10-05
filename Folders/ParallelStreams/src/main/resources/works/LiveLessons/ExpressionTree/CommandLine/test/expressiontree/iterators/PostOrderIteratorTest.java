package expressiontree.iterators;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import expressiontree.nodes.ComponentNode;
import expressiontree.nodes.CompositeAddNode;
import expressiontree.nodes.CompositeNegateNode;
import expressiontree.nodes.LeafNode;
import expressiontree.tree.ExpressionTree;

public class PostOrderIteratorTest {

	private Iterator<ExpressionTree> iter;

	@Test(expected = NullPointerException.class)
	public void test$nullArgument() {
		iter = new PostOrderIterator(null);
	}

	@Test
	public void test$nullExpression() {
		iter = new PostOrderIterator(new ExpressionTree(null));
		assertFalse(iter.hasNext());
	}

	@Test
	public void test$negation() {
		LeafNode leaf = new LeafNode(1);
		ComponentNode negation = new CompositeNegateNode(leaf);
		iter = new PostOrderIterator(new ExpressionTree(negation));
		assertTrue(iter.hasNext());
		assertEquals(leaf, iter.next().getRoot());
		assertEquals(negation, iter.next().getRoot());
	}
	
	@Test
	public void test$add() {
		LeafNode left = new LeafNode(1);
		LeafNode right = new LeafNode(1);
		ComponentNode add = new CompositeAddNode(left, right);
		iter = new PostOrderIterator(new ExpressionTree(add));
		assertTrue(iter.hasNext());
		assertEquals(left, iter.next().getRoot());
		assertEquals(right, iter.next().getRoot());
		assertEquals(add, iter.next().getRoot());
	}

}
