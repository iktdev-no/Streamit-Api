package no.iktdev.streamit.api.helper

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class Coroutines {
    companion object {
        private val _exception = Observables.ObservableValue<Throwable>()
        fun addListener(listener: Observables.ObservableValue.ValueListener<Throwable>) {
            _exception.addListener(listener)
        }
        val handler = CoroutinesExceptionHandler()
    }

    /**
     * Returns a new CoroutineScope with IO dispatcher and a Job, using the global CoroutinesExceptionHandler instance.
     * This scope is appropriate for IO-bound tasks, such as reading or writing to files, network communication or database access.
     *
     * @return a CoroutineScope instance with the IO dispatcher and a Job
     */
    fun CoroutineIO() =  CoroutineScope(Dispatchers.IO + Job() + handler)

    /**
     * Returns a new CoroutineScope with Default dispatcher and a Job, using the global CoroutinesExceptionHandler instance.
     * This scope is appropriate for CPU-bound tasks, such as sorting or filtering large data sets or performing complex calculations.
     *
     * @return a CoroutineScope instance with the Default dispatcher and a Job
     */
    fun Coroutine() =  CoroutineScope(Dispatchers.Default + Job() + handler)

    /**
     * A global exception handler that catches unhandled exceptions thrown in Coroutines and stores them in a LiveData instance.
     * The key of this handler is a CoroutineExceptionHandler.Key instance.
     */
    class CoroutinesExceptionHandler : CoroutineExceptionHandler {
        /**
         * The key of this CoroutineExceptionHandler instance.
         */
        override val key = CoroutineExceptionHandler.Key

        /**
         * Handles an uncaught exception thrown by a Coroutine by storing it in a LiveData instance.
         *
         * @param context the CoroutineContext of the failed Coroutine
         * @param exception the Throwable instance representing the exception that was thrown
         */
        override fun handleException(context: CoroutineContext, exception: Throwable) {
            var cause: Throwable? = null
            var current = exception
            while (current.cause != null) {
                cause = current.cause
                current = current.cause!!
            }

            if (cause != null) {
                _exception.next(cause)
            } else {
                _exception.next(exception)
            }
        }
    }
}