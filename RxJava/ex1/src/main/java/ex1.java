import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 */
public class ex1 {
    private static Random mRand = new Random();

    private static Observable<Integer> fetch() {
        sleep(java.lang.Math.abs(mRand.nextInt() % 1000));
        print("Loading from ");
        return Observable
                .just(mRand.nextInt(), mRand.nextInt(), mRand.nextInt());
    }

    /**
     * A sensible sleep() method that doesn't throw InterruptedException.
     */
    private static void sleep(int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ignored) {
        }
    }

    private static void emit(ObservableEmitter<Integer> emitter) throws InterruptedException {
        for (int count = 0; count < 10; count++) {
            sleep(100);
            print("Emitting... " + count);
            emitter.onNext(count);

            if (count == 15)
                throw new RuntimeException("Something went wrong");
        }
        emitter.onComplete();
    }

    private static void process(int value) throws InterruptedException {
        sleep(1000);
        print(value);
    }

    private static void test2() {
        Observable
                .create(ex1::emit)
                .observeOn(Schedulers.computation(), true, 2)
                .map(data -> data)
                .subscribe(ex1::process,
                           err -> print("ERROR" + err),
                           () -> print("DONE"));

    }
    private static Observable<Integer> getValues() {
        return Observable
                .fromCallable(ex1::fetch)
                .flatMap(s -> s)
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io());
    }

    private static void test1() {
        Observable<Integer> n1 = getValues();
        Observable<Integer> n2 = getValues();

        n1
                .mergeWith(n2)
                // .first()
                .observeOn(Schedulers.computation())
                .subscribe(ex1::print);
    }

    private static void print(String s) {
        System.out.println(s + " in thread "+ Thread.currentThread().getName());
    }

    private static void print(Integer i) {
        System.out.println("Got: " + i + " in thread " + Thread.currentThread().getName());
    }

    /**
     *
     */
    static void test3() {
        List<Integer> list = new ArrayList<>(List.of(10, 20, 30, 40, 50));

        Single<List<Integer>> si = Single.just(list); // () -> { sleep(1000); return list; });

        Single<Integer> is1 = si
                .flatMap((List<Integer> l) -> Observable
                        .fromIterable(l)
                        .map((Integer i) -> i * i)
                        .reduce(Integer::sum)
                        .defaultIfEmpty(0));

        Single<Integer> is2 = si
                .flatMap((List<Integer> l) -> Observable
                        .fromIterable(l)
                        .map((Integer i) -> i * i)
                        .reduce(Integer::sum)
                        .defaultIfEmpty(0));

        is1.subscribe(System.out::println);
        is2.subscribe(System.out::println);
    }

    /**
     * 
     */
    static public void main(String[] argv) throws InterruptedException {
        // test1();
        // test2();
        test3();

        Thread.sleep(10000);
    }
}

