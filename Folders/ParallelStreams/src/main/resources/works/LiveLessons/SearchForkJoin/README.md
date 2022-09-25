This directory contains several example programs that illustrate how
to implement an "embarrassingly parallel" program that uses the Java
fork-join framework to concurrently search for phrases in the complete
works of Shakespeare.  Here's an overview of what's included:

. DivideNConquerForkJoin -- This example implements an "embarrassingly
  parallel" program that uses a Java fork-join pool and a "divide and
  conquer" strategy (i.e., splitting various folders, phrases, and
  strings in half) to search for phrases in a recursive directory
  folder containing all the works of Shakespeare.  All parallel
  processing in this program only uses "classic" Java 7 features
  (i.e., no Java 8 parallel streams) to demonstrate "raw" fork-join
  pool programming.

. IterativeForkJoin -- This example implements an "embarrassingly
  parallel" program that uses the Java fork-join framework and a
  "recursive traversal" strategy to search for phrases in a recursive
  directory folder containing the works of Shakespeare.  This example
  is loosely based on the tutorial at
  http://www.oracle.com/technetwork/articles/java/fork-join-422606.html,
  but is much more powerful and interesting.

