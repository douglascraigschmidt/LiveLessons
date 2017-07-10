This directory contains two example programs that illustrate how to
implement an "embarrassingly parallel" program that uses the Java 7
fork-join framework to concurrently search for phrases in the complete
works of Shakespeare.  Here's an overview of what's included:

. DivideNConquer -- This example provides a Java 7 implementation of
  the fork-join-based program that uses a "divide and conquer"
  approach, i.e., splitting the various (inputs|phrases|strings) in
  half.

. RecursiveTraverse -- This example provides a Java 8 implementation
  of the fork-join-based example from
  http://www.oracle.com/technetwork/articles/java/fork-join-422606.html.

