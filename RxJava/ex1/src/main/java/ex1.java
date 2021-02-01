import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 */
public class ex1 {
    static public void main(String[] argv) throws InterruptedException {
        List<String> l1 = Arrays.asList("1","2","3");
        
        Single<List<String>> l2 = Observable
            .fromIterable(l1)
            .repeat(3)
            .collect(toList());

        System.out.println(l2.blockingGet());
    }
}

