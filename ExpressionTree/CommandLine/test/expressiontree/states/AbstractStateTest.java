package expressiontree.states;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Test;

import expressiontree.platspecs.Platform;
import expressiontree.platspecs.PlatformFactory;
import expressiontree.tree.TreeOps;

public abstract class AbstractStateTest {

	protected State state;
	protected TreeOps tree;
	protected final ByteArrayOutputStream out = new ByteArrayOutputStream();

	public void init() {
		tree = new TreeOps();
		// FIXME dependend on Global
		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(System.in, new PrintStream(out),
				null).makePlatform());
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test(expected = IllegalStateException.class)
	public void test$format() {
		state.format(tree, "");
	}

	@Test(expected = IllegalStateException.class)
	public void test$makeTree() {
		state.makeTree(tree, "");
	}

	@Test(expected = IllegalStateException.class)
	public void test$print() {
		state.print(tree, "");
	}

	@Test(expected = IllegalStateException.class)
	public void test$evaluate() {
		state.evaluate(tree, "");
	}

}