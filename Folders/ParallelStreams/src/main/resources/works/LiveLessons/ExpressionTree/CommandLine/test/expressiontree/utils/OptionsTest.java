package expressiontree.utils;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import expressiontree.platspecs.Platform;
import expressiontree.platspecs.PlatformFactory;

public class OptionsTest {

	private ByteArrayInputStream in = new ByteArrayInputStream("1+1".getBytes());
	private ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void init() {

		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(in, new PrintStream(out), null)
				.makePlatform());
	}

	@After
	public void clean() {
		System.setOut(null);
	}

	@Test(expected = NullPointerException.class)
	public void test$nullArgs() {
		Options.instance().parseArgs(null);
	}

	@Test
	public void test$noArgs() {
		Options.instance().parseArgs(new String[] {});
		assertEquals("", out.toString().trim());
	}

	@Test
	public void test$emptyArgs() {
		Options.instance().parseArgs(new String[] { "" });
		assertEquals(
				"Options" + " " + "\nHelp Invoked on " + System.lineSeparator()
						+ "Options [-h|-v] " + System.lineSeparator() + " "
						+ System.lineSeparator() + " " + System.lineSeparator()
						+ "Options Usage: " + System.lineSeparator()
						+ "Options -h: invoke help " + System.lineSeparator()
						+ "Options -v: enter verbose mode", out.toString()
						.trim());
	}

	@Test
	public void test$verbose() {
		Options.instance().parseArgs(new String[] { "-v" });
		assertEquals("", out.toString().trim());
	}

}
