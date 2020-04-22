package com.infinum.sentinel.ui.children

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import com.infinum.sentinel.data.models.memory.storage.StoredPreferences
import com.infinum.sentinel.databinding.SentinelFragmentChildBinding
import com.infinum.sentinel.databinding.SentinelItemPreferenceBinding
import com.infinum.sentinel.databinding.SentinelItemPreferencesBinding
import java.io.File

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferencesFragment : Fragment() {

    companion object {
        fun newInstance() = PreferencesFragment()
        val TAG: String = PreferencesFragment::class.java.simpleName
    }

    private var viewBinding: SentinelFragmentChildBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = SentinelFragmentChildBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding?.let { binding ->
            val prefsDirectory =
                File(requireActivity().applicationContext.applicationInfo.dataDir, "shared_prefs")

            if (prefsDirectory.exists() && prefsDirectory.isDirectory) {
                prefsDirectory.list().orEmpty().toList().map { it.removeSuffix(".xml") }
            } else {
                listOf()
            }.map { name ->
                val allPrefs = requireActivity().getSharedPreferences(name, MODE_PRIVATE).all
                val tuples = allPrefs.keys.toSet().mapNotNull {
                    @Suppress("UNCHECKED_CAST")
                    when (val value = allPrefs[it]) {
                        is Boolean -> Triple(Boolean::class.java, it, value)
                        is Float -> Triple(Float::class.java, it, value)
                        is Int -> Triple(Int::class.java, it, value)
                        is Long -> Triple(Long::class.java, it, value)
                        is String -> Triple(String::class.java, it, value)
                        is Set<*> -> Triple(Set::class.java, it, value as Set<String>)
                        else -> null
                    }
                }
                StoredPreferences(
                    name = name,
                    values = tuples
                )
            }.forEach {
                binding.contentLayout.addView(
                    SentinelItemPreferencesBinding.inflate(
                        LayoutInflater.from(binding.contentLayout.context),
                        binding.contentLayout,
                        false
                    )
                        .apply {
                            nameView.text = it.name
                            addprefs(it.values, this)
                        }.root
                )
            }
        }
    }

    private fun addprefs(
        tuples: List<Triple<Class<out Any>, String, Any>>,
        binding: SentinelItemPreferencesBinding
    ) {
        tuples.forEach {
            binding.prefsLayout.addView(
                SentinelItemPreferenceBinding.inflate(
                    LayoutInflater.from(binding.prefsLayout.context),
                    binding.prefsLayout,
                    false
                ).apply {
                    labelView.text = it.second
                    valueView.text = it.third.toString()
                }.root
            )
        }
    }

    override fun onDestroy() =
        super.onDestroy().run {
            viewBinding = null
        }
}
