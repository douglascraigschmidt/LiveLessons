package utils;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;

import static java.util.Collections.swap;

/**
 * A class that implements the heap sort algorithm in Java for arrays
 * and lists.  Based on <a href="https://java2blog.com/heap-sort-in-java/">this article</a>.
 */
public class HeapSort {
    private static <T> void buildheap(List<T> list,
                                      Comparator<? super T> c) {
        // As last non leaf node will be at (list.size() - 1) / 2 so we
        // will start from this location for heapifying the elements.
        for (int i = (list.size() - 1) / 2; i >= 0; i--) 
            heapify(list, i, list.size() - 1, c);
    }
 
    private static <T> void heapify(List<T> list,
                                    int i,
                                    int size,
                                    Comparator<? super T> c) {
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        int max;
        if(left <= size && c.compare(list.get(left), list.get(i)) > 0) {
            max = left;
        } else {
            max = i;
        }
 
        if (right <= size && c.compare(list.get(right), list.get(max)) > 0) {
            max = right;
        }
        // If max is not current node, exchange it with max of left
        // and right child.
        if (max != i) {
            swap(list, i, max);
            heapify(list, max, size, c);
        }
    }

    public static <T> void sort(List<T> list, Comparator<? super T> c) {
        HeapSort.buildheap(list, c);
        int sizeOfHeap = list.size() - 1;
        for (int i = sizeOfHeap; i > 0; i--) {
            swap(list, 0, i);
            --sizeOfHeap;
            heapify(list, 0, sizeOfHeap, c);
        }
    }
}
