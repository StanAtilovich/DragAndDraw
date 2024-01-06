package ru.stan.draganddraw

import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable

data class Box(var start: PointF, var end: PointF? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(PointF::class.java.classLoader)!!,
        parcel.readParcelable(PointF::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(start, flags)
        parcel.writeParcelable(end, flags)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Box> {
        override fun createFromParcel(parcel: Parcel) = Box(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Box?>(size)
    }

    val left: Float?
        get() = end?.let { Math.min(start.x, it.x) }

    val right: Float?
        get() = end?.let { Math.max(start.x, it.x) }

    val top: Float?
        get() = end?.let { Math.min(start.y, it.y) }

    val bottom: Float?
        get() = end?.let { Math.max(start.y, it.y) }

}