package expressiontree.iterators;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import expressiontree.nodes.ComponentNode;
import expressiontree.nodes.CompositeAddNode;
import expressiontree.nodes.CompositeNegateNode;
import expressiontree.nodes.LeafNode;
import expressiontree.tree.ExpressionTree;

public class LevelOrderIteratorTest {

	private Iterator<ExpressionTree> iter;

	@Test(expected = NullPointerException.class)
	public void test$nullArgument() {
		iter = new LevelOrderIterator(null);
	}

	@Test
	public void test$nullExpression() {
		iter = new LevelOrderIterator(new ExpressionTree(null));
		assertFalse(iter.hasNext());
	}

	@Test
	public void test$negation() {
		LeafNode leaf = new LeafNode(1);
		ComponentNode negation = new CompositeNegateNode(leaf);
		iter = new LevelOrderIterator(new ExpressionTree(negation));
		assertTrue(iter.hasNext());
		assertEquals(negation, iter.next().getRoot());
		assertEquals(leaf, iter.next().getRoot());
	}
	
	@Test
	public void test$add() {
		LeafNode left = new LeafNode(1);
		LeafNode right = new LeafNode(1);
		ComponentNode add = new CompositeAddNode(left, right);
		iter = new LevelOrderIterator(new ExpressionTree(add));
		assertTrue(iter.hasNext());
		assertEquals(add, iter.next().getRoot());
		assertEquals(right, iter.next().getRoot());
		assertEquals(left, iter.next().getRoot());
	}


}
