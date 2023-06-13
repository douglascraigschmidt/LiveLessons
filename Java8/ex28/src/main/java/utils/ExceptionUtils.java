package utils;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This utility class contains helper methods that convert checked
 * exceptions to unchecked exceptions.
 */
public final class ExceptionUtils {
    /**
     * Define an interface that's used to convert a checked exception
     * to an unchecked exception for a {@link Consumer} parameter.
     */
    @FunctionalInterface
    public interface Consumer_WithExceptions<T> {
        void accept(T t) throws Exception;
    }

    /**
     * Define an interface that's used to convert a checked exception
     * to an unchecked exception for a {@link Function} parameter.
     */
    @FunctionalInterface
    public interface Function_WithExceptions<T, R> {
        R apply(T t) throws Exception;
    }

    /**
     * Define an interface that's used to convert a checked exception
     * to an unchecked exception for a {@link Runnable} parameter.
     */
    @FunctionalInterface
    public interface Runnable_WithExceptions {
        void accept() throws Exception;
    }

    /**
     * Define an interface that's used to convert a checked exception
     * to an unchecked exception for a {@link Supplier} parameter.
     */
    @FunctionalInterface
    public interface Supplier_WithExceptions<T> {
        T get() throws Exception;
    }

    /**
     * Define an adapter method that's used to convert a checked
     * exception to an unchecked exception for a {@link Consumer}
     * parameter.
     *
     * .forEach(rethrowConsumer(name ->
     *          System.out.println(Class.forName(name)))); 
     * or
     * .forEach(rethrowConsumer(ClassNameUtil::println))
     *
     * @param consumer The {@link Consumer} to call
     * @return The result of applying the {@link Consumer}
     */
    public static <T> Consumer<T> rethrowConsumer(Consumer_WithExceptions<T> consumer) {
        return t -> {
            try { consumer.accept(t); }
            catch (Exception exception) { throwAsUnchecked(exception); }
        };
    }

    /**
     * Define an adapter method that's used to convert a checked
     * exception to an unchecked exception for a {@link Function}
     * parameter.
     * 
     * .map(rethrowFunction(name -> Class.forName(name))) 
     * or 
     * .map(rethrowFunction(Class::forName))
     *
     * @param function The {@link Function} to call
     * @return The result of applying the {@link Function}
     */
    public static <T, R> Function<T, R> rethrowFunction
        (Function_WithExceptions<T, R> function) {
        return t -> {
            try { return function.apply(t); }
            catch (Exception exception) { throwAsUnchecked(exception); return null; }
        };
    }

    /**
     * Define an adapter method that's used to convert a checked
     * exception to an unchecked exception for a {@link Supplier}
     * parameter.
     *
     * rethrowSupplier(() -> new StringJoiner
     *                 (new String(new byte[]{77, 97, 114, 107}, "UTF-8")))
     * @param supplier The {@link Supplier} to call
     * @return The result of applying the {@link Supplier}
     */
    public static <T> Supplier<T> rethrowSupplier
        (Supplier_WithExceptions<T> supplier) {
        return () -> {
            try { return supplier.get(); }
            catch (Exception exception) { throwAsUnchecked(exception); return null; }
        };
    }

    /**
     * Define an adapter method that's used to convert a checked
     * exception to an unchecked exception for a {@link Runnable}
     * parameter.
     *
     * uncheck(() -> Class.forName("xxx"));
     *
     * @param t The {@link Runnable} to call
     */
    public static void rethrowRunnable(Runnable_WithExceptions t)
    {
        try { t.accept(); }
        catch (Exception exception) { throwAsUnchecked(exception); }
    }

    /**
     * Define an adapter method that's used to convert a checked
     * exception to an unchecked exception for a {@link Supplier}
     * parameter.
     * 
     * uncheck(() -> Class.forName("xxx"));
     *
     * @param supplier The {@link Supplier} to call
     * @return The result of applying the {@link Supplier}
     */
    public static <R> R uncheck(Supplier_WithExceptions<R> supplier) {
        try { return supplier.get(); }
        catch (Exception exception) { throwAsUnchecked(exception); return null; }
    }

    /**
     * Define an adapter method that's used to convert a checked
     * exception to an unchecked exception for a {@link Function}
     * parameter.
     *
     * uncheck(Class::forName, "xxx");
     *
     * @param function The {@link Function} to call
     * @param t The parameter passed to the {@link Function}
     * @return The result of applying the {@link Function} to
     *         the parameter {@code t}
     */
    public static <T, R> R uncheck(Function_WithExceptions<T, R> function,
                                   T t) {
        try { return function.apply(t); }
        catch (Exception exception) {
            throwAsUnchecked(exception);
            return null;
        }
    }

    /**
     * Define a method that converts a checked exception to an
     * unchecked exception.
     *
     * @param exception The checked exception
     * @throws E The unchecked exception
     */
    @SuppressWarnings ("unchecked")
    private static <E extends Throwable> void throwAsUnchecked(Exception exception) throws E { throw (E)exception; }
}
