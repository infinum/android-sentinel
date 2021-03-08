package com.infinum.sentinel.extensions

import android.os.Bundle
import android.os.Parcel
import com.infinum.sentinel.data.models.memory.bundles.BundleTree
import timber.log.Timber

/**
 * Measure the sizes of all the values in a [Bundle] when written to a [Parcel].
 * Returns a map from keys to the sizes, in bytes, of the associated values in the Bundle.
 *
 * @return a map from keys to value sizes in bytes
 */
internal fun Bundle.toSizeTree(): BundleTree {
    val results = ArrayList<BundleTree>(this.size())
    // We measure the totalSize of each value by measuring the total totalSize of the bundle before and
    // after removing that value and calculating the difference. We make a copy of the original
    // bundle so we can put all the original values back at the end. It's not possible to
    // carry out the measurements on the copy because of the way Android parcelables work
    // under the hood where certain objects are actually stored as references.
    val copy = Bundle(this)
    try {
        var bundleSize = this.sizeAsParcelable
        // Iterate over copy's keys because we're removing those of the original bundle
        for (key in copy.keySet()) {
            this.remove(key)
            val newBundleSize = this.sizeAsParcelable
            val valueSize = bundleSize - newBundleSize
            results += BundleTree(
                key,
                valueSize,
                emptyList()
            )
            bundleSize = newBundleSize
        }
    } finally {
        // Put everything back into original bundle
        this.putAll(copy)
    }
    return BundleTree(
        "${System.identityHashCode(this)}",
        this.sizeAsParcelable,
        results
    )
}

/**
 * Size of a [Bundle] when written to a [Parcel].
 *
 * @return size when written to parcel in bytes
 */
@Suppress("TooGenericExceptionCaught")
internal val Bundle.sizeAsParcelable: Int
    get() {
        val parcel = Parcel.obtain()
        return try {
            parcel.writeBundle(this)
            parcel.dataSize()
        } catch (exception: ArrayIndexOutOfBoundsException) {
            Timber.e(exception)
            0
        } finally {
            parcel.recycle()
        }
    }
