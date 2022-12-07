package utils;

import java.util.List;

import static java.util.Collections.swap;

/**
 * A class that implements the heap sort algorithm in Java for arrays
 * and lists.  Based on https://java2blog.com/heap-sort-in-java/
 */
public class HeapSort {
    private static <T extends Comparable<? super T>> void buildheap(T []arr) {
        /*
         * As last non leaf node will be at (arr.length-1)/2 so we
         * will start from this location for heapifying the

         * elements */
        for (int i = (arr.length - 1) / 2; i >= 0; i--) 
            heapify(arr, i, arr.length - 1);
    }

    private static<T extends Comparable<? super T>> void heapify(T[] arr, int i,int size) {
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        int max;
        if(left <= size && arr[left].compareTo(arr[i]) > 0) {
            max = left;
        } else {
            max = i;
        }
 
        if (right <= size && arr[right].compareTo(arr[max]) > 0) {
            max = right;
        }
        // If max is not current node, exchange it with max of left
        // and right child.
        if (max != i) {
            exchange(arr, i, max);
            heapify(arr, max, size);
        }
    }
 
    private static<T extends Comparable<? super T>> void exchange(T[] arr, int i, int j) {
        T t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

    public static<T extends Comparable<? super T>> void sort(T[] arr) {
        buildheap(arr);
        int sizeOfHeap = arr.length - 1;

        for (int i = sizeOfHeap; i > 0; i--) {
            exchange(arr, 0, i);
            --sizeOfHeap;
            heapify(arr, 0, sizeOfHeap);
        }
    }
     
    private static <T extends Comparable<? super T>> void buildheap(List<T> list) {
        // As last non leaf node will be at (list.size() - 1) / 2 so we
        // will start from this location for heapifying the elements.
        for (int i = (list.size() - 1) / 2; i >= 0; i--) 
            heapify(list, i, list.size() - 1);
    }
 
    private static<T extends Comparable<? super T>> void heapify(List<T> list, int i, int size) {
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        int max;
        if(left <= size && list.get(left).compareTo(list.get(i)) > 0) {
            max = left;
        } else {
            max = i;
        }
 
        if (right <= size && list.get(right).compareTo(list.get(max)) > 0) {
            max = right;
        }
        // If max is not current node, exchange it with max of left
        // and right child.
        if (max != i) {
            swap(list, i, max);
            heapify(list, max, size);
        }
    }

    public static<T extends Comparable<? super T>> void sort(List<T> list) {
        buildheap(list);
        int sizeOfHeap = list.size() - 1;
        for (int i = sizeOfHeap; i > 0; i--) {
            swap(list, 0, i);
            --sizeOfHeap;
            heapify(list, 0, sizeOfHeap);
        }
    }
}
