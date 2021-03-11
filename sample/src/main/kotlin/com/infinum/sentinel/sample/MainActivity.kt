package com.infinum.sentinel.sample

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.sample.databinding.ActivityMainBinding
import java.util.Locale
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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
                            (1..300).map { Random.nextLong() }
                                .forEachIndexed { index, value ->
                                    putExtra("random_$index", "$value")
                                }
                            putExtra("my_bundle", Bundle().apply { putBundle("my_bundle_1",
                            Bundle().apply { putInt("my_int", 1) }) })
                        }
                )
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
        "my_${base.orEmpty().toLowerCase(Locale.getDefault())}_${Random.nextInt(0, 10)}"
}
