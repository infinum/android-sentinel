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
internal fun Bundle.sizeTree(): BundleTree {
    val original = Bundle(this)
    val originalSize = original.sizeAsParcelable

    return BundleTree(
        "${System.identityHashCode(this)}",
        originalSize,
        original.keySet().map { key ->
            val withoutKey = Bundle(original)

            val internalTree: BundleTree? = if (withoutKey[key] is Bundle) {
                (withoutKey[key] as? Bundle)?.sizeTree()
            } else {
                null
            }

            withoutKey.remove(key)

            val valueSize = originalSize - withoutKey.sizeAsParcelable

            BundleTree(
                key,
                valueSize,
                internalTree?.let { listOf(it) } ?: emptyList()
            )
        }
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
            parcel.writeBundle(Bundle(this))
            parcel.dataSize()
        } catch (exception: ConcurrentModificationException) {
            Timber.e(exception)
            0
        } finally {
            parcel.recycle()
        }
    }
