package com.jeremykruid.lawndemandprovider.repositories

import com.jeremykruid.lawndemandprovider.model.OrderDao
import com.jeremykruid.lawndemandprovider.model.OrderObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.inject

class OrderRepo: DatabaseRepo<OrderObject>() {

    private val orderDao: OrderDao by inject()

    override suspend fun checkCache(): OrderObject? {
        return orderDao.getOrder()
    }

    public override suspend fun storeToCache(t: OrderObject) {
        orderDao.insert(t)
    }

    override suspend fun listenToDb(): Flow<RepoResource<OrderObject>> {
        return orderDao.listenToOrder().map { order ->
            RepoResource(data = order)
        }
    }
}