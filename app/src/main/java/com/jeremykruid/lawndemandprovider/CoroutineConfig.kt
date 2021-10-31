package com.jeremykruid.lawndemandprovider

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface CoroutineConfig {
    /**
     * Used to perform all the data operations such as networking, reading, or writing from the
     * database, reading, or writing to the files
     */
    val ioDispatcher: CoroutineDispatcher

    /**
     * Should choose this when we are planning to do complex and long-running calculations
     */
    val compDispatcher: CoroutineDispatcher


    /**
     * Unused at the moment but does give a way to cancel all jobs running on application scope
     * To replace the idea of using a [kotlinx.coroutines.GlobalScope]
     * ViewModels should use [androidx.lifecycle.viewModelScope]
     * activities/fragments use [androidx.lifecycle.lifecycleScope]
     */
    val job: Job

    /**
     * To be used instead of default global scope
     */
    val applicationScope: CoroutineScope

    fun applicationLaunchOnIO(block: suspend CoroutineScope.() -> Unit): Job {
        return applicationScope.launch(ioDispatcher, block = block)
    }

    fun applicationLaunchOnComp(block: suspend CoroutineScope.() -> Unit): Job {
        return applicationScope.launch(compDispatcher, block = block)
    }

    fun applicationLaunchOnMain(block: suspend CoroutineScope.() -> Unit): Job {
        return applicationScope.launch(block = block)
    }
}
