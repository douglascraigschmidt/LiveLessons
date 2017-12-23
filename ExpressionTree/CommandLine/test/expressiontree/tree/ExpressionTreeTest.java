package expressiontree.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import expressiontree.nodes.ComponentNode;
import expressiontree.nodes.CompositeNegateNode;
import expressiontree.nodes.LeafNode;
import expressiontree.platspecs.Platform;
import expressiontree.platspecs.PlatformFactory;

public class ExpressionTreeTest {

	private ExpressionTree expression;

	private ComponentNode leaf = new LeafNode(1);

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void init() {
		expression = new ExpressionTree(leaf);
		// TODO brake dependency on Global
		// TODO 'ByteArrayOutputStream' can't be cast to 'PrintStream'
		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(System.in, new PrintStream(out),
				null).makePlatform());
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void testNullArgument() {
		assertTrue(new ExpressionTree(null).isNull());
	}

	@Test
	public void test$isNull() {
		assertFalse(expression.isNull());
	}

	@Test
	public void test$getRoot() {
		assertEquals(leaf, expression.getRoot());
	}

	@Test
	public void test$item() throws Exception {
		assertEquals(leaf.item(), expression.item());
	}

	@Test
	public void test$left() {
		// TODO ExpressionTree should implement equals method.
		assertNotEquals(new ExpressionTree(leaf.left()), expression.left());
	}

	@Test(expected = NullPointerException.class)
	public void test$left$NullArgument() {
		// FIXME nullPointer
		new ExpressionTree(null).left();
		fail();
	}

	@Test
	public void test$right() {
		// TODO ExpressionTree should implement equals method.
		assertNotEquals(new ExpressionTree(leaf.right()), expression.right());
	}

	@Test(expected = NullPointerException.class)
	public void test$right$NullArgument() {
		// FIXME nullPointer
		new ExpressionTree(null).right();
		fail();
	}

	@Test(expected = NullPointerException.class)
	public void test$makeIterator() {
		// FIXME nullPointer
		expression.makeIterator(null);
		fail();
	}

	@Test
	public void test$makeIterator$emptyString() {
		// TODO not obvious behavior
		Iterator<ExpressionTree> inOrderIterator = expression.makeIterator("");
		assertNotNull(inOrderIterator);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test$makeIterator$randomName() {
		expression.makeIterator("bla-bla");
		fail();
	}

	@Test(expected = NullPointerException.class)
	public void test$print() {
		// FIXME nullPointer
		expression.print(null);
		fail();
	}

	@Test
	public void test$print$emptyString() throws Exception {
		//TODO not obvious behavior
		expression.print("");
		//TODO '1[ ]' remove blank
		assertEquals("1", out.toString().trim());
	}
	
	@Test
	public void test$print$compositeComponent$emptyString() {
		//TODO not obvious behavior
		new ExpressionTree(new CompositeNegateNode(leaf)).print("");
		//TODO '1[ ]' remove blank
		assertEquals("-1", out.toString().trim());
	}
	
	@Test(expected = NullPointerException.class)
	public void test$evaluate() throws Exception {
		// FIXME nullPointer
		expression.evaluate(null);
		fail();
	}
	
	@Test
	public void test$evaluate$emptyString() throws Exception {
		//TODO not obvious behavior
		expression.evaluate("");
		assertEquals("1", out.toString().trim());
	}


}
