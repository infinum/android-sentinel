package com.infinum.sentinel.sample

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.sample.databinding.ActivityMainBinding
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.system.exitProcess
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    @Suppress("TooGenericExceptionCaught", "LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val allPrefs = listOf(
            PreferenceManager.getDefaultSharedPreferences(applicationContext),
            applicationContext.getSharedPreferences(
                "SHARED_PREFERENCES",
                Context.MODE_PRIVATE
            ),
            applicationContext.getSharedPreferences(
                "PERSISTED_SHARED_PREFERENCES",
                Context.MODE_PRIVATE
            ),
            EncryptedSharedPreferences.create(
                this,
                "ENCRYPTED_SHARED_PREFERENCES",
                MasterKey.Builder(this)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        )

        Sentinel.setExceptionHandler { _, exception ->
            println("setExceptionHandler ${exception.message}")
            exitProcess(exception.hashCode())
        }

        Sentinel.setAnrListener { exception ->
            println("setAnrListener ${exception.message}")
            exitProcess(exception.hashCode())
        }

        with(viewBinding) {
            showSentinel.setOnClickListener {
                Sentinel.show()
            }
            randomizePrefs.setOnClickListener {
                allPrefs.forEach { putRandomIntoPreferences(it) }
            }
            showJavaScreen.setOnClickListener {
                startActivity(Intent(this@MainActivity, JavaMainActivity::class.java))
            }
            showBundleScreen.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, BundleActivity::class.java)
                        .apply {
                            // 12000 breaks 500 kB limit
                            // 3000 strings in bundle takes 25s to measure
                            (1..bundleItemSlider.value.roundToInt()).map { Random.nextLong() }
                                .forEachIndexed { index, value ->
                                    putExtra("random_$index", "$value")
                                }
                        }
                )
            }
            @Suppress("TooGenericExceptionThrown")
            generateException.setOnClickListener {
                throw RuntimeException("This is a test exception!")
            }
            generateDeadlock.setOnClickListener {
                ANRTester.deadLock()
            }
            generateLoop.setOnClickListener {
                ANRTester.infiniteLoop()
            }
            generateSleep.setOnClickListener {
                ANRTester.threadSleep()
            }
            generateInfo.setOnClickListener {
                if (tagView.isChecked) {
                    Timber.tag("Main").i("Some info message")
                } else {
                    Timber.i("Some info message")
                }
            }
            generateDebug.setOnClickListener {
                if (tagView.isChecked) {
                    Timber.tag("Main").d("Some debug message")
                } else {
                    Timber.d("Some debug message")
                }
            }
            generateWarning.setOnClickListener {
                if (tagView.isChecked) {
                    Timber.tag("Main").w("Some warning message")
                } else {
                    Timber.w("Some warning message")
                }
            }
            generateVerbose.setOnClickListener {
                if (tagView.isChecked) {
                    Timber.tag("Main").v("Some verbose message")
                } else {
                    Timber.v("Some verbose message")
                }
            }
            generateWtf.setOnClickListener {
                if (tagView.isChecked) {
                    Timber.tag("Main").wtf("Some WTF message")
                } else {
                    Timber.wtf("Some WTF message")
                }
            }
            generateLog.setOnClickListener {
                if (tagView.isChecked) {
                    Timber.tag("Main").log(Log.WARN, "Some log message with Log.WARN priority")
                } else {
                    Timber.log(Log.WARN, "Some log message with Log.WARN priority")
                }
            }
            generateError.setOnClickListener {
                if (tagView.isChecked) {
                    Timber.tag("Main").e("Some message")
                } else {
                    Timber.e("Some message")
                }
            }
            @Suppress("TooGenericExceptionThrown")
            generateErrorException.setOnClickListener {
                try {
                    throw RuntimeException("Oh no.")
                } catch (exception: RuntimeException) {
                    if (tagView.isChecked) {
                        Timber.tag("Main").e(exception)
                    } else {
                        Timber.e("Some exception message", exception)
                    }
                }
            }
        }
    }

    private fun putRandomIntoPreferences(sharedPreferences: SharedPreferences) =
        sharedPreferences.edit()
            .putBoolean(randomizeName(Boolean::class.simpleName), Random.nextBoolean())
            .putFloat(randomizeName(Float::class.simpleName), Random.nextFloat())
            .putInt(randomizeName(Int::class.simpleName), Random.nextInt())
            .putLong(randomizeName(Long::class.simpleName), Random.nextLong())
            .putString(randomizeName(String::class.simpleName), Random.nextInt().toString())
            .putStringSet(
                randomizeName(Set::class.simpleName),
                setOf(
                    Random.nextInt().toString(),
                    Random.nextFloat().toString(),
                    Random.nextBoolean().toString(),
                    Random.nextLong().toString(),
                    Random.nextInt().toString(),
                    Random.nextFloat().toString(),
                    Random.nextBoolean().toString(),
                    Random.nextLong().toString()
                )
            )
            .apply()

    @Suppress("MagicNumber")
    private fun randomizeName(base: String?) =
        "my_${base.orEmpty().lowercase(Locale.getDefault())}_${Random.nextInt(0, 10)}"
}
