package expressiontree.commands;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class MacroCommandTest extends AbstractCommandTest {

	@Before
	public void init() {
		super.init();
		command = new MacroCommand(tree, Arrays.asList(new FormatCommand(tree,
				"")));
	}

	@Test
	public void test$nullArgument() {
		new MacroCommand(null, null);
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
