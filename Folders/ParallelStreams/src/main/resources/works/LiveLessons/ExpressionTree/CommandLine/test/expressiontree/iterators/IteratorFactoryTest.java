package expressiontree.iterators;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import expressiontree.tree.ExpressionTree;

public class IteratorFactoryTest {

	private IteratorFactory itr;

	@Before
	public void init() {
		itr = new IteratorFactory();
	}

	@Test(expected = NullPointerException.class)
	public void test$nullArguments() {
		itr.makeIterator(null, null);
	}

	@Test(expected = NullPointerException.class)
	public void test$nullExpressionTree() {
		itr.makeIterator(null, "");
	}

	@Test(expected = NullPointerException.class)
	public void test$nullTraversalOrderArgument() {
		itr.makeIterator(new ExpressionTree(null), null);
	}

	@Test
	public void test$default() {
		//TODO not obvious def behavior
		Iterator<ExpressionTree> iter = itr.makeIterator(new ExpressionTree(
				null), "");
		assertTrue(iter instanceof InOrderIterator);
	}
	
	@Test
	public void test$inOrder() {
		Iterator<ExpressionTree> iter = itr.makeIterator(new ExpressionTree(
				null), "in-order");
		assertTrue(iter instanceof InOrderIterator);
	}
	
	@Test
	public void test$preOrder() {
		Iterator<ExpressionTree> iter = itr.makeIterator(new ExpressionTree(
				null), "pre-order");
		assertTrue(iter instanceof PreOrderIterator);
	}
	
	@Test
	public void test$postOrder() {
		Iterator<ExpressionTree> iter = itr.makeIterator(new ExpressionTree(
				null), "post-order");
		assertTrue(iter instanceof PostOrderIterator);
	}
	
	@Test
	public void test$levelOrder() {
		Iterator<ExpressionTree> iter = itr.makeIterator(new ExpressionTree(
				null), "level-order");
		//FIXME wrong implementation
		assertTrue(iter instanceof IteratorFactory.LevelOrderIterator);
	}

}
