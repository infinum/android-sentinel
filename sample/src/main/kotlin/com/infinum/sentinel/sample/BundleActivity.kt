package com.infinum.sentinel.sample

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.infinum.sentinel.sample.databinding.ActivityBundleBinding
import timber.log.Timber

class BundleActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityBundleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityBundleBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        intent.extras?.let { breakdown(it) }?.run {
            Timber.tag("_BOJAN_2").i(this)
        }
    }

    fun breakdown(bundle: Bundle): String {
//        val (key, size, subTrees) = treeFromBundle(bundle)
//        var result = String.format(
//            "%s contains %d keys and measures %d bytes when serialized as a Parcel",
//            key, subTrees.size, size
//        )
//        for ((key, size) in subTrees) {
//            result += String.format(
//                "\n* %s = %d bytes",
//                key, size
//            )
//        }
//        return result
        return treeFromBundle(bundle).toString()
    }

    /**
     * Measure the sizes of all the values in a typed [Bundle] when written to a
     * [Parcel]. Returns a map from keys to the sizes, in bytes, of the associated values in
     * the Bundle.
     *
     * @param bundle to measure
     * @return a map from keys to value sizes in bytes
     */
    fun treeFromBundle(bundle: Bundle): BundleTree {
        val results = ArrayList<BundleTree>(bundle.size())
        // We measure the totalSize of each value by measuring the total totalSize of the bundle before and
        // after removing that value and calculating the difference. We make a copy of the original
        // bundle so we can put all the original values back at the end. It's not possible to
        // carry out the measurements on the copy because of the way Android parcelables work
        // under the hood where certain objects are actually stored as references.
        val copy = Bundle(bundle)
        try {
            var bundleSize = sizeAsParcel(bundle)
            // Iterate over copy's keys because we're removing those of the original bundle
            for (key in copy.keySet()) {
                bundle.remove(key)
                val newBundleSize = sizeAsParcel(bundle)
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
            bundle.putAll(copy)
        }
        return BundleTree(
            "${System.identityHashCode(bundle)}",
            sizeAsParcel(bundle),
            results
        )
    }

    /**
     * Measure the size of a typed [Bundle] when written to a [Parcel].
     *
     * @param bundle to measure
     * @return size when written to parcel in bytes
     */
    // TODO: This should be an extension
    fun sizeAsParcel(bundle: Bundle): Int {
        val parcel = Parcel.obtain()
        try {
            parcel.writeBundle(bundle)
            return parcel.dataSize()
        } finally {
            parcel.recycle()
        }
    }

    /**
     * Measure the size of a [Parcelable] when written to a [Parcel].
     *
     * @param parcelable to measure
     * @return size of parcel in bytes
     */
    fun sizeAsParcel(parcelable: Parcelable): Int {
        val parcel = Parcel.obtain()
        try {
            parcel.writeParcelable(parcelable, 0)
            return parcel.dataSize()
        } finally {
            parcel.recycle()
        }
    }
}

data class BundleTree(
    val id: String,
    val size: Int,
    val subTrees: List<BundleTree> = emptyList()
)