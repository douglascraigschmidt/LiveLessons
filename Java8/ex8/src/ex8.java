import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.math.BigInteger;

/**
 * ...
 */
public class ex8 {
	static public void main (String[] argv) {
		CompletableFuture<BigInteger>
			future = new CompletableFuture<>();

		new Thread (() -> {
            BigInteger bi1 =
                new BigInteger("188027234133482196");
            BigInteger bi2 =
                new BigInteger("2434101");
            future.complete(bi1.gcd(bi2));
		}).start();

		System.out.println("GCD = " + future.join());  
	}
}
