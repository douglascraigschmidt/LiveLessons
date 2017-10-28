package utils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * A Java utility class that defines useful helper methods for
 * fork-join operations.
 */
public class ForkJoinUtils {
    private ForkJoinUtils() {}

    /**
     * Apply {@code op} to all items in the {@code list} using
     * iterative calls to fork-join pool methods.
     */
    public static <T> List<T> applyAllIter(List<T> list,
                                           Function<T, T> op) {
        return ForkJoinPool.commonPool().invoke(new RecursiveTask<List<T>>() {
                protected List<T> compute() {
                    List<ForkJoinTask<T>> forks = new ArrayList<>();
                    List<T> results = new ArrayList<>();

                    for (T t : list)
                        forks.add(new RecursiveTask<T>() {
                                protected T compute() {
                                    return op.apply(t);
                                }
                            }.fork());

                    for (ForkJoinTask<T> task : forks)
                        results.add(task.join());

                    return results;
                }});
    }

    /**
     * Apply {@code op} to all items in the {@code list} by
     * recursively splitting up calls to fork-join pool methods.
     */
    public static <T> List<T> applyAllSplit(List<T> list,
                                            Function<T, T> op) {
        class SplitterTask extends RecursiveTask<List<T>> {
            private List<T> mList;

            private SplitterTask(List<T> list) {
                mList = list;
            }

            protected List<T> compute() {
                if (mList.size() <= 4) {
                    List<T> l = new ArrayList<>();
                    for (T t : mList)
                        l.add(op.apply(t));

                    return l;
                } else {
                    int splitPos = mList.size() / 2;
                    ForkJoinTask<List<T>> leftTask = 
                        new SplitterTask(mList.subList(0,
                                                       splitPos))
                        .fork();
                                         
                    mList = mList.subList(splitPos, mList.size());

                    List<T> rightResult = compute();
                    
                    List<T> leftResult = leftTask.join();

                    leftResult.addAll(rightResult);
                    
                    return leftResult;
                }
            }
        }

        return ForkJoinPool.commonPool().invoke(new SplitterTask(list));
    }

    /**
     * Apply {@code op} to all items in the {@code list} by
     * recursively splitting up calls to fork-join pool methods.
     */
    public static <T> List<T> applyAllSplitIndex(List<T> list,
                                                 Function<T, T> op) {
        T[] results = (T[]) Array.newInstance(list.get(0).getClass(),
                                                  list.size());
        class SplitterTask extends RecursiveTask<Void> {
            private int mLo;
            private int mHi;

            private SplitterTask(int lo, int hi) {
                mLo = lo;
                mHi = hi;
            }

            protected Void compute() {
                int mid = (mLo + mHi) >>> 1;
                if (mLo == mid) {
                    results[mLo] = op.apply(list.get(mLo));
                    return null;
                } else {
                    ForkJoinTask<Void> leftTask =
                        new SplitterTask(mLo, 
                                         mid) 
                        .fork();

                    mLo = mid;
                    compute();
                    
                    leftTask.join();
                    
                    return null;
                }
            }
        }

        ForkJoinPool.commonPool().invoke(new SplitterTask(0, list.size()));
        return Arrays.asList(results);
    }

    /**
     * Apply {@code op} to all items in the {@code list} using the
     * fork-join pool invokeAll() method.
     */
    public static <T> List<Future<T>> invokeAll(List<T> list,
                                                Function<T, T> op) {
        // Create a new list of callables.
        List<Callable<T>> tasks =
            new ArrayList<>();

        // Add all the ops to the list.
        for (T t : list)
            tasks.add(() -> { return op.apply(t);});

        // Call invokeAll() to process all elements in the list.
        return ForkJoinPool.commonPool().invokeAll(tasks);
    }
}
