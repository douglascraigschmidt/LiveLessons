package expressiontree.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class FormatCommandTest extends AbstractCommandTest {

	@Before
	public void init() {
		super.init();
		command = new FormatCommand(tree, "");
	}

	@Test
	public void test$nullArgument() {
		new FormatCommand(null, null);
	}

	@Test
	public void test$execute() {
		command.execute();
	}

	@Test
	public void test$printValidCommands() {
		command.printValidCommands(true);
		assertEquals("1. expr [expression]" + System.lineSeparator()
				+ "2a. eval [post-order]" + System.lineSeparator()
				+ "2b. print [in-order | pre-order | post-order| level-order]"
				+ System.lineSeparator() + "0b. set [variable = value]"
				+ System.lineSeparator() + "0c. quit", out.toString().trim());
	}

	@Test
	public void test$printValidCommands$verbose() {
		command.printValidCommands(false);
		assertEquals("1. expr [expression]" + System.lineSeparator()
				+ "2a. eval [post-order]" + System.lineSeparator()
				+ "2b. print [in-order | pre-order | post-order| level-order]"
				+ System.lineSeparator() + "0b. set [variable = value]"
				+ System.lineSeparator() + "0c. quit", out.toString().trim());
	}

}
