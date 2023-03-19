import kotlinx.coroutines.*
fun main() = runBlocking {
    var sharedCounter = 0
    val scope = 
    // We want our code to run on 4 threads
    CoroutineScope(newFixedThreadPoolContext(4, "synchronizationPool")) 
    scope.launch {
	//create 1000 coroutines (light-weight threads).
	val coroutines = 1.rangeTo(1000).map { 
            launch {
                for(i in 1..1000){ // and in each of them, increment the sharedCounter 1000 times.
                    sharedCounter++
                }
            }
        }

        coroutines.forEach {
            coroutine->
            coroutine.join() // wait for all coroutines to finish their jobs.
        }
    }.join()
  
    println("The number of shared counter should be 1000000, but actually is $sharedCounter")
}
