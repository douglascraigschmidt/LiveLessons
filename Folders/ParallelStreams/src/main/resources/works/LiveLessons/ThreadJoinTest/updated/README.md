This example implements an "embarrassingly parallel" program that uses
foundational modern Java functional programming features to
concurrently search for phrases in a string containing all the works
of Shakespeare.  The modern Java features showcased by this example
include lambda expressions, method references, and functional
interfaces, in conjunction with Thread.start() to run threads and
Thread.join() to wait for all running threads.

