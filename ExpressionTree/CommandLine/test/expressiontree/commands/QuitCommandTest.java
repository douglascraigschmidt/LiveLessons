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

public class QuitCommandTest {

	private UserCommand command;

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void init() {
		// FIXME dependend on Global
		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(System.in, new PrintStream(out),
				null).makePlatform());

		command = new QuitCommand(new TreeOps());
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void test$nullArgument() {
		new QuitCommand(null);
	}

	@Test
	public void test$execute() {
		command.execute();
	}

	@Test
	public void test$printValidCommands() {
		command.printValidCommands(true);
		assertEquals("", out.toString().trim());
	}

	@Test
	public void test$printValidCommands$verbose() {
		command.printValidCommands(false);
		assertEquals("", out.toString().trim());
	}

}
