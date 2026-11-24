package com.infinum.sentinel.ui.shared.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infinum.sentinel.extensions.InjectedViewModelFactory

/**
 * The [VM] of the current `LocalViewModelStoreOwner`, created by the library's injector. In a Navigation 3 entry
 * that owner is the entry, so each screen gets its own view model, cleared when the screen leaves the back stack.
 *
 * [onCreated] runs once, when the view model is created, and not again on recomposition or a configuration change.
 * Its receiver gives access to the creation extras, for example to `createSavedStateHandle()`.
 */
@Composable
internal inline fun <reified VM : ViewModel> injectedViewModel(noinline onCreated: CreationExtras.(VM) -> Unit = {}): VM =
    viewModel { InjectedViewModelFactory.create(VM::class.java).also { onCreated(it) } }
