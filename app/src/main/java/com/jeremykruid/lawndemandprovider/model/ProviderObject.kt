package com.jeremykruid.lawndemandprovider.model

import kotlinx.parcelize.Parcelize

@Parcelize
class ProviderObject{
    var id: String? = null
    var name: String? = null
    var imgUrl: String? = null
    var lat: Double? = null
    var lon: Double? = null
    var isOnline: Boolean? = null
    var topProvider: Boolean? = false
    var approved: Boolean = false
    var nextJob: String? = null

    constructor()
    constructor(id: String?, name: String?, imgUrl: String?, lat: Double?, lon: Double?,
                isOnline: Boolean, topProvider: Boolean?, approved: Boolean, nextJob: String?){
        this.id = id
        this.name = name
        this.imgUrl = imgUrl
        this.lat = lat
        this.lon = lon
        this.isOnline = isOnline
        this.topProvider = topProvider
        this.approved = approved
        this.nextJob = nextJob

    }
}
