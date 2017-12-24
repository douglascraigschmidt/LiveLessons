package expressiontree.commands;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import expressiontree.platspecs.Platform;
import expressiontree.platspecs.PlatformFactory;
import expressiontree.tree.TreeOps;

public class SetCommandTest {

	private UserCommand command;

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void init() {
		// FIXME dependend on Global
		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(System.in, new PrintStream(out),
				null).makePlatform());

		command = new SetCommand(new TreeOps(), "a=3");
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void test$nullArgument() {
		new SetCommand(null, null);
	}

	@Test
	public void test$execute() {
		command.execute();
	}

	@Test
	public void test$printValidCommands() {
		command.printValidCommands(true);
		//FIXME bugs in commands index numbers
		assertEquals(
				"1. format [in-order]"
						+ System.lineSeparator()
						+ "2. expr [expression]"
						+ System.lineSeparator()
						+ "3a. eval [post-order]"
						+ System.lineSeparator()
						+ "3b. print [in-order | pre-order | post-order| level-order]"
						+ System.lineSeparator() + "97 set [variable=value]"
						+ System.lineSeparator() + "0. format [in-order]"
						+ System.lineSeparator() + "0. format [in-order]"
						+ System.lineSeparator() + "98. quit", out.toString()
						.trim());
	}

	@Test
	public void test$printValidCommands$verbose() {
		command.printValidCommands(false);
		assertEquals(
				"1. format [in-order]"
						+ System.lineSeparator()
						+ "2. expr [expression]"
						+ System.lineSeparator()
						+ "3a. eval [post-order]"
						+ System.lineSeparator()
						+ "3b. print [in-order | pre-order | post-order| level-order]"
						+ System.lineSeparator() + "97 set [variable=value]"
						+ System.lineSeparator() + "0. format [in-order]"
						+ System.lineSeparator() + "0. format [in-order]"
						+ System.lineSeparator() + "98. quit", out.toString()
						.trim());
	}

}
