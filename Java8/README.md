This directory contains all the small Java 8 source code examples from
my LiveLessons course on [Java
Concurrency](http://www.dre.vanderbilt.edu/~schmidt/LiveLessons/CPiJava/),
as well as some other examples from my Safari Live Training courses.
Here's an overview of what's included:

. ex1 - This example shows how to use Java 8 lambda expressions and
  method references to sort elements of a collection.  It also shows
  how to use the Java 8 forEach() method.
  
. ex2 - This example shows the use of a simple lambda expression in
  the context of a Java List/ArrayList removeIf() method.
  
. ex3 - This example uses a Java Function-based method reference to
  sort a list of threads by their names.  It also demonstrates the use
  of the Comparator.comparing() factory method.
  
. ex4 - This example shows how a Java 7 BiFunction lambda can be used
  to replace all the values of all keys in a ConcurrentHashMap.  It
  also contrasts the Java 8 BiFunction with a conventional Java 7
  solution using a foreach loop.

. ex5 - This example shows how a Java 8 Consumer interface can be used
  with forEach() to print out the values in a list by binding the
  System.out println() method to the forEach() Consumer parameter.

. ex6 - This example shows how a Java 8 Supplier interface can be used
  to print a default value if a key is not found in a map.  It also
  shows how to use the Java 8 Optional class.
  
. ex7 - This example of shows how the Java 8 Supplier interface can be
  used in conjunction with a constructor reference.
  
. ex8 - This example shows the use of a CompletableFuture to
  concurrently compute the greatest common divisor (GCD) of two
  BigIntegers.

. ex9 - This example uses a Java 8 ConcurrentHashMap and Java 8
  function-based method reference to compute/cache/retrieve prime
  numbers.

. ex10 - This example shows the use of a lambda expression in the
  context of a Java ConcurrentHashMap removeIf() method.

. ex11 - This example shows the improper use of the Stream.peek()
  aggregate operation to interfere with a running stream.

. ex12 - This example shows a simple example of a Java 8 stream that
  illustrates how it can be used with "pure" functions, i.e.,
  functions whose return values are only determined by their input
  values, without observable side effects.

. ex13 - This example shows a simple example of a Java 8 Spliterator
  to traverse each word in a list containing a quote from a famous
  Shakespeare play.

. ex14 - This example shows the difference in overhead for using a
  parallel spliterator to split a Java LinkedList and an ArrayList
  into chunks.

. ex15 - This example shows the limitations of using inherently
  sequential Java 8 streams operations (such as iterate() and limit())
  in the context of parallel streams.
