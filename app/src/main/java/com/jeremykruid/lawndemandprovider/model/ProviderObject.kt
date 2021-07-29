package com.jeremykruid.lawndemandprovider.model

import android.os.Parcel
import android.os.Parcelable

data class ProviderObject(
    val id: String?,
    val name: String?,
    val imgUrl: String?,
    val lat: Long,
    val lon: Long
): Parcelable{
    constructor(parcel: Parcel): this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong()
    ){

    }
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(imgUrl)
        dest.writeLong(lat)
        dest.writeLong(lon)
    }

    companion object CREATOR: Parcelable.Creator<ProviderObject>{
        override fun createFromParcel(source: Parcel): ProviderObject {
            return ProviderObject(source)
        }

        override fun newArray(size: Int): Array<ProviderObject?> {
            return arrayOfNulls(size)
        }

    }

}