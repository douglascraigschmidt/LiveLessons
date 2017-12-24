package expressiontree.input;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import expressiontree.platspecs.Platform;
import expressiontree.platspecs.PlatformFactory;

public class VerboseModeInputHandlerTest {

	private InputHandler handler;

	private ByteArrayInputStream in = new ByteArrayInputStream("1+1".getBytes());
	private ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void init() {
		handler = new VerboseModeInputHandler();

		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(in, new PrintStream(out), null)
				.makePlatform());
	}

	@After
	public void clean() {
		System.setOut(null);
	}

	@Test
	public void test$promtUser() {
		handler.promptUser();
		assertEquals("1a. format [in-order]" + System.lineSeparator()
				+ "1b. set [variable=value]" + System.lineSeparator()
				+ "2. expr [expression]" + System.lineSeparator()
				+ "3a. eval [post-order]" + System.lineSeparator()
				+ "3b. print [in-order | pre-order | post-order| level-order]"
				+ System.lineSeparator() + "0c. quit " + System.lineSeparator()
				+ "  " + System.lineSeparator() + ">", out.toString().trim());
	}

	@Test
	public void test$promtUser$promted() {
		handler.promptUser();
		handler.promptUser();
		assertEquals("1a. format [in-order]" + System.lineSeparator()
				+ "1b. set [variable=value]" + System.lineSeparator()
				+ "2. expr [expression]" + System.lineSeparator()
				+ "3a. eval [post-order]" + System.lineSeparator()
				+ "3b. print [in-order | pre-order | post-order| level-order]"
				+ System.lineSeparator() + "0c. quit " + System.lineSeparator()
				+ "  " + System.lineSeparator() + "> >", out.toString().trim());
	}

	@Test(expected = NullPointerException.class)
	public void test$makeUserCommand$nullArgument() {
		handler.makeUserCommand(null);
		assertEquals("", out.toString().trim());
	}

	@Test
	public void test$makeUserCommand$emptyArgument() {
		handler.makeUserCommand("");
		assertEquals("", out.toString().trim());
	}

}
