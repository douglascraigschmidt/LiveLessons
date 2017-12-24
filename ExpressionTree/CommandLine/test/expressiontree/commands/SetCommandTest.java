package expressiontree.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SetCommandTest extends AbstractCommandTest {

	@Before
	public void init() {
		super.init();
		command = new SetCommand(tree, "a=3");
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
		// FIXME bugs in commands index numbers
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
