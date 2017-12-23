package expressiontree.interpreter;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import expressiontree.tree.ExpressionTree;

public class InterpreterTest {

	private Interpreter intepreter;

	@Before
	public void init() {
		intepreter = new Interpreter();
	}

	@Test
	public void test$notNullSymbolTable() {
		assertNotNull(intepreter.symbolTable());
	}

	@Test(expected = NullPointerException.class)
	public void test$nullInput() {
		intepreter.interpret(null);
	}

	@Test
	public void test$emptyInput() {
		ExpressionTree expression = intepreter.interpret("");
		assertTrue(expression.isNull());
	}

	@Test
	public void test$statement() throws Exception {
		ExpressionTree expression = intepreter.interpret("1");
		assertEquals(1, expression.item());
	}
	
	@Test
	public void test$negative() throws Exception {
		//FIXME TODO allow first number signet
		ExpressionTree expression = intepreter.interpret("-1");
		assertEquals(45, expression.item());
	}
	
	@Test(expected=NullPointerException.class)
	public void test$positive() throws Exception {
		//FIXME TODO allow first number signet
		intepreter.interpret("+1");
	}
	
	@Test
	public void test$add() throws Exception {
		ExpressionTree expression = intepreter.interpret("1+1");
		assertEquals(43, expression.item());
		assertEquals(1, expression.left().item());
		assertEquals(1, expression.right().item());
	}
	
	@Test
	public void test$multiply() throws Exception {
		ExpressionTree expression = intepreter.interpret("1*1");
		assertEquals(42, expression.item());
		assertEquals(1, expression.left().item());
		assertEquals(1, expression.right().item());
	}
	
	@Test
	public void test$devide() throws Exception {
		ExpressionTree expression = intepreter.interpret("1/1");
		assertEquals(47, expression.item());
		assertEquals(1, expression.left().item());
		assertEquals(1, expression.right().item());
	}
	
	@Test
	public void test$complex() throws Exception {
		ExpressionTree expression = intepreter.interpret("1+1+1*2-5/5+ \n(1+1) -(--1) + (-5) ");
		assertEquals(43, expression.item());
		assertEquals(45, expression.left().item());
		assertEquals(45, expression.right().item());
	}
	
	@Test
	public void test$complexFromVideoLesson() throws Exception {
		ExpressionTree expression = intepreter.interpret("-5*(3+4)");
		assertEquals(42, expression.item());
		assertEquals(45, expression.left().item());
		assertEquals(43, expression.right().item());
	}
	
	@Test
	public void test$assigment() throws Exception {
		ExpressionTree expression = intepreter.interpret("a=1");
		assertEquals(1, expression.item());
		assertTrue(expression.left().isNull());
		assertTrue(expression.right().isNull());
	}
	
	@Test
	public void test$assigment$complex() throws Exception {
		ExpressionTree expression = intepreter.interpret("a=1+12");
		assertEquals(43, expression.item());
		assertEquals(1, expression.left().item());
		assertEquals(12, expression.right().item());
	}

}
