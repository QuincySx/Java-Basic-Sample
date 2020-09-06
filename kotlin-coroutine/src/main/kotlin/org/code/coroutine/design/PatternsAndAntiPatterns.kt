/**
 * Kotlin 协程的使用模式与反模式
 */
package org.code.coroutine.design

import kotlinx.coroutines.*

/**
 * SupervisorJob() 里面重写了
 * fun childCancelled(cause: Throwable): Boolean = false
 * 如果返回 false 代表协程不予处理该异常。可以直接在子协程外面包 try/catch 捕获异常
 * 如果是 JobImpl 的实现返回 true 代表协程处理了该错误，表现形式就是软件崩溃或根协程直接崩溃，就不能依靠 try/catch 捕获异常
 */

//region 反例:如果异步块可能引发异常，请不要依赖于用 try/catch 块包装它，他还是会崩溃。
/**
 * 如果异步块可能引发异常，因为 launch 是父协程范围内启动，发生异常会直接导致崩溃。
 * 此时异步块会直接触发根协程取消所有子协程任务并报错崩溃。
 */
suspend fun exceptionAsyncFailure() {
    val job: Job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)

    // may throw Exception
    fun doWork(): Deferred<Int> = scope.async {
        println("doWork")
        throw RuntimeException("error")
    }   // (1)
    scope.launch {
        println("函数开始执行")
        try {
            print("result:${doWork().await()}")                                // (2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("函数执行结束")
    }
    scope.launch {
        delay(100)
        println("另一个函数执行")
    }
    job.join()
}
//endregion


//region 正例:如果异步块可能引发异常，可以依赖于用 try / catch 块包装它。
/**
 * 如果异步块可能引发异常，因为 async 是父协程范围内启动，发生异常会直接导致崩溃。
 * 那么使用 SupervisorJob 后子协程的异常就与根协程无关了，记得依赖 try / catch 捕获异常。
 */
suspend fun exceptionAsync() {
    val job: Job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + job)

    // may throw Exception
    fun doWork(): Deferred<Int> = scope.async {
        println("doWork")
        throw RuntimeException("error")
    }   // (1)
    scope.launch {
        println("函数开始执行")
        try {
            print("result:${doWork().await()}")  // (2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("函数执行结束")
    }
    scope.launch {
        delay(100)
        println("另一个函数执行")
    }
    job.join()
}
//endregion


//region 反例:因为 async 是父协程范围启动的，请不要依赖于用try / catch块包装它，他还是会崩溃。
/**
 * 如果异步块可能引发异常，因为 async 是父协程范围内启动，发生异常会直接导致崩溃。
 * 此时异步块会直接触发根协程取消所有子协程任务并报错崩溃。
 */
suspend fun exceptionAsyncNotParentScopeFailure() {
    val job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)

    scope.launch {
        try {
            async {       // (1)
                println("doWork")
                throw RuntimeException("error")
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    scope.launch {
        delay(1000)
        println("另一个函数执行")
    }
    job.join()
}
//endregion


//region 正例: 使用 coroutineScope 包装子协程操作来处理异常。
/**
 * 如果异步块可能引发异常，因为 async 是父协程范围内启动，发生异常会直接导致崩溃。
 * 那么使用 CoroutineScope 可以改变协程作用域，后子协程的异常就与根协程无关了。
 * 如果某一个协程发生错误，则会取消该作用于的所有协程，不会触及其他作用域，然后记得使用 try/catch 捕获异常
 */
suspend fun exceptionAsyncCoroutineScopeFailure() {
    val job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)

    scope.launch {
        try {
            coroutineScope {
                async {                                         // (1)
                    println("doWork")
                    throw RuntimeException("error")
                }.await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    scope.launch {
        delay(100)
        println("另一个函数执行")
    }
    job.join()
}
//endregion


/**
 * 协程任务管理
 */
class WorkManager() {
    val job = Job()
    val scope = CoroutineScope(job + Dispatchers.Default)
    fun doWork1() {
        scope.launch {
            delay(1000)
            println("doWork1")
        }
    }

    fun doWork2() {
        scope.launch {
            delay(2000)
            println("doWork2")
        }
    }

    fun cancelAllWork() {
        job.cancel()
    }

    fun cancelChildWork() {
        scope.coroutineContext.cancelChildren()
    }
}


//region 反例: 直接使用 job.cancel() 关闭协程
/**
 * 直接取消 job 任务，那么这个协程也就结束了不会在工作
 */
suspend fun cancelAllTask() {
    val workManager = WorkManager()
    workManager.doWork1()
    workManager.doWork2()
    delay(1000)
    workManager.cancelAllWork()
    workManager.doWork1()
    workManager.job.join()
}
//endregion


//region 正例: 使用 scope.coroutineContext.cancelChildren() 关闭协程作用域内的所有协程，不结束主协程。
/**
 * 取消作用域内部的任务，那么这个协程也就结束了不会在工作
 */
suspend fun cancelCoroutineTask() {
    val workManager = WorkManager()
    workManager.doWork1()
    workManager.doWork2()
    delay(1000)
    workManager.cancelChildWork()
    workManager.doWork1()
    workManager.job.join()
}
//endregion


//region 正例: suspend 函数要指明运行的线程环境
suspend fun timeConsumingTask() {
    withContext(Dispatchers.Main) {
        // UI main thread 操作
        withContext(Dispatchers.IO) {
            //.. 具体任务
        }
        // UI main thread 操作
    }
}
//endregion


fun main() = runBlocking {
//    exceptionAsync()
//    exceptionAsyncFailure()
//    exceptionAsyncNotParentScopeFailure()
//    exceptionAsyncCoroutineScopeFailure()
    cancelAllTask()
//    cancelCoroutineTask()
}

