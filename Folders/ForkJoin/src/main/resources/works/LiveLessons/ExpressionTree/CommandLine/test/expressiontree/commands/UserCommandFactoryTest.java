package expressiontree.commands;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import expressiontree.tree.TreeOps;

public class UserCommandFactoryTest {

	private UserCommandFactory factory;

	@Before
	public void init() {
		factory = new UserCommandFactory(new TreeOps());
	}
	
	@Test
	public void test$toQuit() {
		UserCommand command = factory.makeUserCommand("1+2");
		assertTrue(command instanceof QuitCommand);
	}

	@Test
	public void test$format() {
		UserCommand command = factory.makeUserCommand("format");
		assertTrue(command instanceof FormatCommand);
	}

	@Test
	public void test$expr() {
		UserCommand command = factory.makeUserCommand("expr");
		assertTrue(command instanceof ExprCommand);
	}

	@Test
	public void test$eval() {
		UserCommand command = factory.makeUserCommand("eval");
		assertTrue(command instanceof EvalCommand);
	}

	@Test
	public void test$print() {
		UserCommand command = factory.makeUserCommand("print");
		assertTrue(command instanceof PrintCommand);
	}
	
	@Test
	public void test$print$expression() {
		UserCommand command = factory.makeUserCommand("print 1+2");
		assertTrue(command instanceof PrintCommand);
	}

	@Test
	public void test$set() {
		UserCommand command = factory.makeUserCommand("set");
		assertTrue(command instanceof SetCommand);
	}

	@Test
	public void test$macro() {
		UserCommand command = factory.makeUserCommand("macro");
		assertTrue(command instanceof MacroCommand);
	}

	@Test
	public void test$quit() {
		UserCommand command = factory.makeUserCommand("quit");
		assertTrue(command instanceof QuitCommand);
	}

}
