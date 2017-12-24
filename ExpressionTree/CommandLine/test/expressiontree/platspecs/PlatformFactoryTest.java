package expressiontree.platspecs;

import static org.junit.Assert.*;

import org.junit.Test;

public class PlatformFactoryTest {

	@Test
	public void test$nullArguments() {
		PlatformFactory factory = new PlatformFactory(null, null, null);
		Platform platform = factory.makePlatform();
		assertNotNull(platform);
	}

	@Test
	public void test$comandLinePlatform() {
		PlatformFactory factory = new PlatformFactory(System.in, System.out,
				null);
		Platform platform = factory.makePlatform();
		assertNotNull(platform);
	}

}
