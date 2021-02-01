import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 */
public class ex1 {
    static List<String> concatRxJava(List<String> list, int n) {
        return Observable
                .fromIterable(list)
                .repeat(n)
                .collect(toList())
                .blockingGet();
    }

    static List<String> concatStream1(List<String> list, int n) {
        Stream<String> s = Stream.empty();

        while (--n >= 0)
            s = Stream.concat(s, list.stream());

        return s.collect(toList());
    }

    static List<String> concatStream2(List<String> list, int n) {
        return IntStream
            .rangeClosed(1, 3)
            .mapToObj(__ -> list)
            .flatMap(List::stream)
            .collect(toList());
    }

    static public void main(String[] argv) throws InterruptedException {
        List<String> list = Arrays.asList("1", "2", "3");

        System.out.println(concatRxJava(list, 3));
        System.out.println(concatStream1(list, 3));
        System.out.println(concatStream2(list, 3));
    }
}

