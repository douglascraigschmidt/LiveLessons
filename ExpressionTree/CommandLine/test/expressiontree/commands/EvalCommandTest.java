package expressiontree.commands;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import expressiontree.platspecs.Platform;
import expressiontree.platspecs.PlatformFactory;
import expressiontree.tree.TreeOps;

public class EvalCommandTest {

	private UserCommand command;

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void init() {
		// FIXME dependend on Global
		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(System.in, new PrintStream(out),
				null).makePlatform());

		command = new EvalCommand(new TreeOps(), "");
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void test$nullArgument() {
		new EvalCommand(null, null);
	}

	@Test(expected = IllegalStateException.class)
	public void test$execute() {
		command.execute();
	}

	@Test
	public void test$printValidCommands() {
		command.printValidCommands(true);
		assertEquals(
				"1a. eval [post-order]"
						+ System.lineSeparator()
						+ "1b. print [in-order | pre-order | post-order| level-order]"
						+ System.lineSeparator() + "0a. format [in-order]"
						+ System.lineSeparator() + "0b. set [variable = value]"
						+ System.lineSeparator() + "0c. quit", out.toString()
						.trim());
	}

	@Test
	public void test$printValidCommands$verbose() {
		command.printValidCommands(false);
		assertEquals(
				"1a. eval [post-order]"
						+ System.lineSeparator()
						+ "1b. print [in-order | pre-order | post-order| level-order]"
						+ System.lineSeparator() + "0a. format [in-order]"
						+ System.lineSeparator() + "0b. set [variable = value]"
						+ System.lineSeparator() + "0c. quit", out.toString()
						.trim());
	}

}
