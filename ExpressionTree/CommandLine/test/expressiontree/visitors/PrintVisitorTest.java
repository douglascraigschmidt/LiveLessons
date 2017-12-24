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

public class PrintVisitorTest {

	private Visitor visitor;

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void init() {
		// FIXME dependend on Global
		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(System.in, new PrintStream(out),
				null).makePlatform());

		visitor = new PrintVisitor();
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void test$LeafNode() {
		visitor.visit(new LeafNode(0));
		assertEquals("0", out.toString().trim());
	}

	@Test
	public void test$CompositeNegateNode() {
		visitor.visit(new CompositeNegateNode(null));
		assertEquals("-", out.toString().trim());
	}

	@Test
	public void test$CompositeAddNode() {
		visitor.visit(new CompositeAddNode(null, null));
		assertEquals("+", out.toString().trim());
	}

	@Test
	public void test$CompositeSubtractNode() {
		visitor.visit(new CompositeSubtractNode(null, null));
		assertEquals("-", out.toString().trim());
	}

	@Test
	public void test$CompositeDivideNode() {
		visitor.visit(new CompositeDivideNode(null, null));
		assertEquals("/", out.toString().trim());
	}

	@Test
	public void test$CompositeMultiplyNode() {
		visitor.visit(new CompositeMultiplyNode(null, null));
		assertEquals("*", out.toString().trim());
	}

}
