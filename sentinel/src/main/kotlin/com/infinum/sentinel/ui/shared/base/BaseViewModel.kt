package com.infinum.sentinel.ui.shared.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infinum.sentinel.di.LibraryKoinComponent
import kotlinx.coroutines.*
import timber.log.Timber

internal abstract class BaseViewModel : ViewModel(), LibraryKoinComponent {

    private val supervisorJob = SupervisorJob()

    private val dispatchersIo = Dispatchers.IO

    protected open val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    override fun onCleared() {
        super.onCleared()
        dispatchersIo.cancel()
        supervisorJob.cancel()
    }

    protected fun launch(
        scope: CoroutineScope = viewModelScope,
        block: suspend CoroutineScope.() -> Unit
    ) {
        scope.launch(errorHandler + Dispatchers.Main + supervisorJob) { block.invoke(this) }
    }

    protected suspend fun <T> io(block: suspend CoroutineScope.() -> T) =
        withContext(context = dispatchersIo) { block.invoke(this) }
}
