
import utils.RunTimer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static utils.FuturesCollectorStream.toFuture;

/**
 * This example shows ...
 */
public class ex23 {
    private static AtomicInteger mCounter = new AtomicInteger(0);

    private static Supplier<Integer> mSupplier = () -> {
        int result = mCounter.incrementAndGet();
        display("enter Supplier with value "
                + result);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        display("leave Supplier");
        return result;
    };

    private static Function<Integer, Integer> mAction = i -> {
        display("enter Function with value "
                + i);
        display("leave Function");
        return (i % 2) == 0 ? i : null;
    };

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws IOException {
        List<Supplier<Integer>> suppliers =
            Arrays.asList(mSupplier,
                          mSupplier,
                          mSupplier,
                          mSupplier);

        RunTimer.timeRun(() -> testManyJoins(suppliers),
                         "testManyJoins()");

        RunTimer.timeRun(() -> testOneJoin(suppliers),
                         "testOneJoin()");

        System.out.println(RunTimer.getTimingResults());
    }

    private static void testManyJoins(List<Supplier<Integer>> suppliers) {
        System.out.println(">>> testManyJoins()");

        CompletableFuture<Stream<Integer>> resultsFuture = suppliers
            .stream()
            .map(CompletableFuture::supplyAsync)
            .filter(intFuture -> intFuture.thenApply(mAction).join() != null)
            .collect(toFuture());

        resultsFuture
            .thenAccept(stream ->
                        display("results = "
                                + stream
                                .collect(toList())))
            .join();
        System.out.println("<<< leave testManyJoins()");
    }

    private static void testOneJoin(List<Supplier<Integer>> suppliers) {
        System.out.println(">>> enter testOneJoin()");

        CompletableFuture<Stream<Integer>> resultFuture = suppliers
            .stream()
            .map(CompletableFuture::supplyAsync)
            .map(intFuture -> intFuture.thenApply(mAction))
            .collect(toFuture());

        resultFuture
            .thenAccept(stream -> 
                        display("results = " 
                                + stream
                                .filter(Objects::nonNull)
                                .collect(toList())))
            .join();

        System.out.println("<<< leave testOneJoin()\n");
    }

    /**
     * Display the {@code string} after prepending the thread id.
     */
    private static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + ", "
                           + System.currentTimeMillis()
                           + "] "
                           + string);
    }
}
