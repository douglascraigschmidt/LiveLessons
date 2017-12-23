package expressiontree.interpreter;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SymbolTableTest {
	
	private SymbolTable table;
	
	@Before
	public void init(){
		table = new SymbolTable();
	}

	@Test
	public void test$defaultValue() {
		assertEquals(0,table.get(null));
	}
	
	@Test
	public void test$set() {
		table.set("a", 1);
		assertEquals(1,table.get("a"));
	}

}
