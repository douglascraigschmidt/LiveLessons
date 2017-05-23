import java.util.*;
import java.util.function.Supplier;

/**
 * This example of shows how the Java 8 Supplier interface can be used
 * in conjunction with a constructor reference.
 */
public class CrDemo {
    public static void main(String[] argv) {
        // Assign the Supplier a constructor reference.
        Supplier<CrDemo> supplier = CrDemo::new;

        // Create a new instance of CrDemo and call its hello()
        // method.
        System.out.println(supplier.get().hello());
    }

    // Simply return the string "hello".
    private String hello() {
        return "hello";
    }
}

