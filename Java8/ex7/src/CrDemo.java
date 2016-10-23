import java.util.*;
import java.util.function.Supplier;

/**
 * ...
 */
public class CrDemo {
    public static void main(String[] argv) {
        Supplier<CrDemo> supplier = CrDemo::new;
        System.out.println(supplier.get().hello());
    }

    private String hello() {
        return "hello";
    }
}

