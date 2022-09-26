package expressiontree.platspecs;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CommandLinePlatformTest {

	private Platform platform;

	private final ByteArrayInputStream in = new ByteArrayInputStream(
			"hello".getBytes());
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void init() {
		System.setOut(new PrintStream(out));
		platform = new CommandLinePlatform(in, new PrintStream(out));
	}

	@After
	public void cleanUpStreams() {
		System.setOut(null);
	}

	@Test
	public void test$retrieveInput() throws IOException {
		platform.retrieveInput(true);
	}

	@Test
	public void test$outputLine() throws IOException {
		platform.outputLine("bla-bla");
		assertEquals("bla-bla", out.toString().trim());
	}

	@Test
	public void test$outputString() throws IOException {
		platform.outputString("bla-bla");
		assertEquals("bla-bla", out.toString().trim());
	}

	@Test
	public void test$platformName() throws IOException {
		String name = platform.platformName();
		assertEquals(System.getProperty("java.specification.vendor"), name);
	}

	@Test
	public void test$outputMenu() throws IOException {
		platform.outputMenu("1", "2", "3");
		assertEquals("1 2 3", out.toString().trim());
	}

	@Test
	public void test$addString() throws IOException {
		String string = platform.addString("bla-bla");
		assertEquals("bla-bla", string);
		assertEquals("bla-bla", out.toString().trim());
	}

	@Test
	public void test$errorLog() throws IOException {
		// FIXME TODO wrong implementation.
		platform.errorLog("bla", "bla");
		assertEquals("bla bla", out.toString().trim());
	}

}
