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

public class SuccinctModeInputHandlerTest {

	private InputHandler handler;

	private ByteArrayInputStream in = new ByteArrayInputStream("1+1".getBytes());
	private ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void init() {
		handler = new SuccinctModeInputHandler();

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
		assertEquals(">", out.toString().trim());
	}

	@Test
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
