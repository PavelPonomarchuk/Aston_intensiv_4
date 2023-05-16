package ru.ponomarchukpn.aston_intensiv_4

import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StyleableRes

private const val DEFAULT_FLOAT_PERCENT = 1F

fun TypedArray.getFloatPercent(@StyleableRes index: Int): Float {
    val floatValue = getFloat(index, DEFAULT_FLOAT_PERCENT)
    return if (floatValue > 0 && floatValue <= 1) {
        floatValue
    } else {
        DEFAULT_FLOAT_PERCENT
    }
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}
