package com.infinum.sentinel.sample

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.sample.databinding.ActivityMainBinding
import com.infinum.sentinel.sample.tools.SentinelTools
import kotlin.random.Random
import kotlin.random.nextULong

class MainActivity : AppCompatActivity() {

    private var sentinel: Sentinel? = null

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
                "ENCRYPTED_SHARED_PREFERENCES",
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        )

        sentinel = Sentinel.watch(this, SentinelTools.get())

        viewBinding.showSentinel.setOnClickListener {
            sentinel?.show()
        }
        viewBinding.randomizePrefs.setOnClickListener {
            allPrefs.forEach { putRandomIntoPreferences(it) }
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

    private fun randomizeName(base: String?) = "my_${base.orEmpty().toLowerCase()}_${Random.nextInt(0,10)}"
}
