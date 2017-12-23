package expressiontree.nodes;

import static org.junit.Assert.*;

import org.junit.Test;

public class LeafNodeTest {

	private LeafNode leaf;

	@Test(expected = NumberFormatException.class)
	public void testWrongInputFormat() {
		new LeafNode(null);
	}

	@Test
	public void testMinValue() {
		leaf = new LeafNode(Integer.MIN_VALUE);
		assertEquals(Integer.MIN_VALUE, leaf.item());
	}

	@Test
	public void testMinStringValue() {
		leaf = new LeafNode(Integer.toString(Integer.MIN_VALUE));
		assertEquals(Integer.MIN_VALUE, leaf.item());
	}
	@Test
	public void testMaxStringValue() {
		leaf = new LeafNode(Integer.toString(Integer.MAX_VALUE));
		assertEquals(Integer.MAX_VALUE, leaf.item());
	}
	
	@Test
	public void testMinStringValueOverflow() {
		leaf = new LeafNode(Integer.toString(Integer.MIN_VALUE - 1));
		assertEquals(Integer.MAX_VALUE, leaf.item());
	}
	
	@Test
	public void testMaxStringValueOverflow() {
		leaf = new LeafNode(Integer.toString(Integer.MAX_VALUE + 1));
		assertEquals(Integer.MIN_VALUE, leaf.item());
	}

	@Test
	public void testMinValueOverflow() {
		leaf = new LeafNode(Integer.MIN_VALUE - 1);
		assertEquals(Integer.MAX_VALUE, leaf.item());
	}

	@Test
	public void testMaxValue() {
		leaf = new LeafNode(Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, leaf.item());
	}

	@Test
	public void testMaxValueOverflow() {
		leaf = new LeafNode(Integer.MAX_VALUE + 1);
		assertEquals(Integer.MIN_VALUE, leaf.item());
	}

	@Test
	public void testChildrens() {
		leaf = new LeafNode(1);
		assertNull(leaf.left());
		assertNull(leaf.right());
	}

}
