package expressiontree.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class NullCommandTest extends AbstractCommandTest {

	@Before
	public void init() {
		super.init();
		command = new NullCommand(tree);
	}

	@Test
	public void test$nullArgument() {
		new NullCommand(null);
	}

	@Test
	public void test$execute() {
		command.execute();
	}

	@Test
	public void test$printValidCommands() {
		command.printValidCommands(true);
		assertEquals("1a. format [post-order]" + System.lineSeparator()
				+ "1b. set [variable=value]" + System.lineSeparator()
				+ "2. expr [expression]" + System.lineSeparator()
				+ "3a. eval [post-order]" + System.lineSeparator()
				+ "3b. print [in-order | pre-order | post-order| level-order]"
				+ System.lineSeparator() + "0c. quit", out.toString().trim());
	}

	@Test
	public void test$printValidCommands$verbose() {
		command.printValidCommands(false);
		assertEquals("1a. format [post-order]" + System.lineSeparator()
				+ "1b. set [variable=value]" + System.lineSeparator()
				+ "2. expr [expression]" + System.lineSeparator()
				+ "3a. eval [post-order]" + System.lineSeparator()
				+ "3b. print [in-order | pre-order | post-order| level-order]"
				+ System.lineSeparator() + "0c. quit", out.toString().trim());
	}

}
