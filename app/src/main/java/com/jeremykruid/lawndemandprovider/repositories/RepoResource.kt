package com.jeremykruid.lawndemandprovider.repositories

data class RepoResource<T>(
    val data: T? = null
) {
    val isSuccessful = data != null
}
