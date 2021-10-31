package com.jeremykruid.lawndemandprovider.repositories

import com.jeremykruid.lawndemandprovider.model.Provider
import com.jeremykruid.lawndemandprovider.model.ProviderDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.inject

class ProviderRepo: DatabaseRepo<Provider>() {

    private val providerDao: ProviderDao by inject()

    override suspend fun checkCache(): Provider? {
        return providerDao.getProvider()
    }

    public override suspend fun storeToCache(t: Provider) {
        providerDao.insert(t)
    }

    override suspend fun listenToDb(): Flow<RepoResource<Provider>> {
        return providerDao.listenToProvider().map { provider ->
            RepoResource(data = provider)
        }
    }
}