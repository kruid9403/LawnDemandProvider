package com.jeremykruid.lawndemandprovider.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.core.KoinComponent

abstract class DatabaseRepo<T> : BaseRepo<T>(), KoinComponent {

    protected abstract suspend fun listenToDb(): Flow<RepoResource<T>>

    override suspend fun listenToRepo(): Flow<RepoResource<T>> {
        return listenToDb().distinctUntilChanged()
    }

}