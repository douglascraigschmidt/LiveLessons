package expressiontree.commands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Test;

import expressiontree.platspecs.Platform;
import expressiontree.platspecs.PlatformFactory;
import expressiontree.tree.TreeOps;

public abstract class AbstractCommandTest {

	UserCommand command;
	TreeOps tree;

	final ByteArrayOutputStream out = new ByteArrayOutputStream();

	public void init() {
		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(System.in, new PrintStream(out),
				null).makePlatform());

		tree = new TreeOps();
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test(expected = IllegalStateException.class)
	public void test$execute() {
		command.execute();
	}

}