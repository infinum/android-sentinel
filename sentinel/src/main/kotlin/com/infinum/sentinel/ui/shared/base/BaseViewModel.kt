package com.infinum.sentinel.ui.shared.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infinum.sentinel.di.LibraryKoinComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal abstract class BaseViewModel : ViewModel(), LibraryKoinComponent {

    private val supervisorJob = SupervisorJob()

    protected val dispatchersIo = Dispatchers.IO
    protected val dispatchersMain = Dispatchers.Main

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
        scope.launch(errorHandler + dispatchersMain + supervisorJob) { block.invoke(this) }
    }

    protected suspend fun <T> io(block: suspend CoroutineScope.() -> T) =
        withContext(context = dispatchersIo) { block.invoke(this) }
}
