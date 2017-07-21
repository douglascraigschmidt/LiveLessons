This folder contains source code thats implement pattern-oriented
variants of the
[ImageTaskGangApplication](https://github.com/douglascraigschmidt/LiveLessons/tree/master/ImageTaskGangApplication)
from my [Java
Concurrency](http://www.dre.vanderbilt.edu/~schmidt/LiveLessons/CPiJava/)
LiveLessons tutorial using Java Executor framework concurrency
features, such as the ExecutorService and ExecutorCompletionService,
to download, process, store, & display images concurrently.  

The original ImageTaskGangApplication was developed as a single code
base that worked both as an Android app and a Java console app.
However, this implementation only worked with Eclipse, which is
increasingly obsolete.  Therefore, this new version splits (and
enhances) the code base into two folders, as follows:

AndroidGUI -- This folder provides an Android Studio GUI-based
versions of the app that integrates the Executor framework with
Android's "Material Design" model.

CommandLine -- This folder provides a command-line version of the app
packaged as a gradle project that compares/contrasts the performance
of different Java Executor framework concurrency models.


