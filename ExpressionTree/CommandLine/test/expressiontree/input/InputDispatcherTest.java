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

public class InputDispatcherTest {

	private InputDispatcher dispatcher;

	private ByteArrayInputStream in = new ByteArrayInputStream("1+1".getBytes());
	private ByteArrayOutputStream out = new ByteArrayOutputStream();

	@Before
	public void init() {
		dispatcher = InputDispatcher.instance();
		dispatcher.makeHandler(false, in, new PrintStream(out), null);

		System.setOut(new PrintStream(out));
		Platform.instance(new PlatformFactory(in, new PrintStream(out), null)
				.makePlatform());
	}

	@After
	public void clean() {
		dispatcher.endInputDispatching();
		System.setOut(null);
	}

	@Test
	public void test$makeHandler$allInputs() {
		dispatcher.dispatchAllInputs();
		assertEquals("", out.toString().trim());
	}

	@Test
	public void test$makeHandlerAndPromptUser$nullArguments$oneInput() {
		dispatcher.makeHandlerAndPromptUser(false, null, null, null);
		dispatcher.dispatchOneInput();
		assertEquals("> > 2", out.toString().trim());
	}

	@Test
	public void test$makeHandler$oneInput() {
		dispatcher.dispatchOneInput();
		assertEquals("> 2", out.toString().trim());
	}
	// TODO dispatchAll with timeout

}
