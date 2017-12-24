package expressiontree.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class QuitCommandTest extends AbstractCommandTest {

	@Before
	public void init() {
		super.init();
		command = new QuitCommand(tree);
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
