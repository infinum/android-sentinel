package com.infinum.sentinel.extensions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.infinum.sentinel.di.LibraryComponents

internal inline fun <reified VM : ViewModel> ComponentActivity.viewModels(): Lazy<VM> = viewModels { InjectedViewModelFactory }

internal inline fun <reified VM : ViewModel> ComponentActivity.viewModels(state: SavedStateHandle): Lazy<VM> =
    viewModels {
        InjectedSavedStateViewModelFactory(this, intent.extras, state)
    }

internal inline fun <reified VM : ViewModel> Fragment.viewModels(): Lazy<VM> = viewModels { InjectedViewModelFactory }

internal inline fun <reified VM : ViewModel> Fragment.viewModels(state: SavedStateHandle): Lazy<VM> =
    viewModels {
        InjectedSavedStateViewModelFactory(this, arguments, state)
    }

@Suppress("UNCHECKED_CAST")
internal object InjectedViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = LibraryComponents.viewModels().viewModelMap[modelClass] as T
}

@Suppress("UNCHECKED_CAST")
internal class InjectedSavedStateViewModelFactory(
    owner: SavedStateRegistryOwner,
    defaultArguments: Bundle?,
    private val state: SavedStateHandle,
) : AbstractSavedStateViewModelFactory(owner, defaultArguments) {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T =
        if (modelClass.constructors.any { it.parameterTypes.contains(SavedStateHandle::class.java) }) {
            (LibraryComponents.viewModels().viewModelMap[modelClass] as T).let { clazz ->
                clazz.javaClass.constructors
                    .find { it.parameterTypes.contains(SavedStateHandle::class.java) }
                    ?.parameterTypes
                    ?.find { it == SavedStateHandle::class.java }
                    ?.let { param ->
                        val field = clazz.javaClass.getDeclaredField(param.name)
                        field.set(clazz, state)
                    }
                clazz
            }
        } else {
            LibraryComponents.viewModels().viewModelMap[modelClass] as T
        }
}
