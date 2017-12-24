package expressiontree.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ExprCommandTest extends AbstractCommandTest {

	@Before
	public void init() {
		super.init();
		command = new ExprCommand(tree, "");
	}

	@Test
	public void test$nullArgument() {
		new ExprCommand(null, null);
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
