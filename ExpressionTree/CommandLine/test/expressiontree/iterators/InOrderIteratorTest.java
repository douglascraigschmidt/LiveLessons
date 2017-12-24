package expressiontree.iterators;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import expressiontree.nodes.ComponentNode;
import expressiontree.nodes.CompositeAddNode;
import expressiontree.nodes.CompositeNegateNode;
import expressiontree.nodes.LeafNode;
import expressiontree.tree.ExpressionTree;

public class InOrderIteratorTest {

	private Iterator<ExpressionTree> iter;

	@Test(expected = NullPointerException.class)
	public void test$nullArgument() {
		iter = new InOrderIterator(null);
	}

	@Test
	public void test$nullExpression() {
		iter = new InOrderIterator(new ExpressionTree(null));
		assertFalse(iter.hasNext());
	}

	@Test
	public void test$negation() {
		LeafNode leaf = new LeafNode(1);
		ComponentNode negation = new CompositeNegateNode(leaf);
		iter = new InOrderIterator(new ExpressionTree(negation));
		assertTrue(iter.hasNext());
		assertEquals(negation, iter.next().getRoot());
		assertEquals(leaf, iter.next().getRoot());
	}
	
	@Test
	public void test$add() {
		LeafNode left = new LeafNode(1);
		LeafNode right = new LeafNode(1);
		ComponentNode add = new CompositeAddNode(left, right);
		iter = new InOrderIterator(new ExpressionTree(add));
		assertTrue(iter.hasNext());
		assertEquals(left, iter.next().getRoot());
		assertEquals(add, iter.next().getRoot());
		assertEquals(right, iter.next().getRoot());
	}

}
