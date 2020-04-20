This example implements an "embarrassingly parallel" program that uses
basic Java 8 functional programming features to concurrently search
for phrases in a string containing all the works of Shakespeare.  The
Java 8 features it showcases include lambda expressions, method
references, and functional interfaces, in conjunction with
Thread.start() to run threads and Thread.join() to wait for all
running threads.

