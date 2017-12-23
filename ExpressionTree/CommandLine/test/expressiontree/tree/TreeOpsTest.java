package expressiontree.tree;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import expressiontree.platspecs.Platform;
import expressiontree.platspecs.PlatformFactory;

public class TreeOpsTest {

	private TreeOps ops;
	
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void init() {
		ops = new TreeOps();
		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(System.in, new PrintStream(out),
				null).makePlatform());
	}
	
	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void test$interpreter() {
		assertNotNull(ops.interpreter());
	}

	@Test(expected = NullPointerException.class)
	public void test$format() {
		ops.format(null);
	}

	@Test
	public void test$format$emptyString() {
		ops.format("");
		assertTrue(ops.formatted());
	}

	@Test(expected = IllegalStateException.class)
	public void test$makeTree() {
		ops.makeTree(null);
	}

	@Test(expected = IllegalStateException.class)
	public void test$makeTree$emptyString() {
		ops.makeTree("");
	}

	@Test(expected = IllegalStateException.class)
	public void test$print() {
		ops.print(null);
	}

	@Test(expected = IllegalStateException.class)
	public void test$print$emptyString() {
		ops.print("");
	}

	@Test(expected = IllegalStateException.class)
	public void test$evaluate() {
		ops.evaluate(null);
	}

	@Test(expected = IllegalStateException.class)
	public void test$evaluate$emptyString() {
		ops.evaluate("");
	}

	@Test(expected = RuntimeException.class)
	public void test$set() {
		ops.set(null);
	}

	@Test(expected = RuntimeException.class)
	public void test$set$emptyString() {
		ops.set("");
	}
	
	@Test(expected = RuntimeException.class)
	public void test$set$someString() {
		ops.set("a");
	}
	
	@Test(expected = RuntimeException.class)
	public void test$set$notFullExpression() {
		ops.set("a=");
		ops.interpreter().symbolTable().print();
		// TODO rem dependency to Global lineSeparator
		assertEquals("a = 1" + System.lineSeparator(), out.toString());
	}
	

	@Test
	public void test$set$expressionWithSpaces() {
		ops.set("  a  =   1    ");
		ops.interpreter().symbolTable().print();
		assertEquals("a = 1" + System.lineSeparator(), out.toString());
	}

	@Test
	public void test$set$expression() {
		ops.set("a=1");
		ops.interpreter().symbolTable().print();
		assertEquals("a = 1" + System.lineSeparator(), out.toString());
	}
	
	@Test
	public void test$state() {
		assertNotNull(ops.state());
	}
	
	@Test
	public void test$tree() {
		assertNotNull(ops.tree());
	}
	
	@Test
	public void test$formatted() {
		assertFalse(ops.formatted());
	}

}
