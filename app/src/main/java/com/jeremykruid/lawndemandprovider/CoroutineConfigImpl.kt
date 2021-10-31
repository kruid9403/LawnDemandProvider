package com.jeremykruid.lawndemandprovider

import kotlinx.coroutines.*

class CoroutineConfigImpl (
    override val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    override val compDispatcher: CoroutineDispatcher = Dispatchers.Default,
    override val job: Job = SupervisorJob(),
    override val applicationScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)
) : CoroutineConfig