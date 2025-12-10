package xyz.malefic.emkt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * A class representing a signal that can emit values of type T and allows
 * other components to connect and react to these emitted values.
 *
 * @param T The type of values that this signal emits.
 */
class Signal<T> {
    private val _flow = MutableSharedFlow<T>()
    val flow: SharedFlow<T> = _flow.asSharedFlow()

    /**
     * Emits a value to all collectors of this signal.
     *
     * @param value The value to emit.
     */
    suspend fun emit(value: T) = _flow.emit(value)

    /**
     * Emits a signal with associated parameters to all collectors.
     *
     * @param value The primary value to emit.
     * @param params Additional parameters to associate with this signal.
     */
    suspend fun emitWithParams(
        value: T,
        params: Map<String, Any?>,
    ) {
        when (value) {
            is SignalData<*> -> {
                val mergedParams = value.params + params
                _flow.emit(SignalData(value.value, mergedParams) as T)
            }

            else -> {
                _flow.emit(SignalData(value, params) as T)
            }
        }
    }

    /**
     * Creates a SignalData object with parameters, which can be emitted later.
     *
     * @param value The value to wrap.
     * @param params The parameters to associate with the value.
     * @return A SignalData object containing the value and parameters.
     */
    fun withParams(
        value: Any?,
        params: Map<String, Any?>,
    ): SignalData<Any?> = SignalData(value, params)

    /**
     * Connects an action to this signal within the provided coroutine scope.
     * The action will be executed for each value emitted by this signal.
     *
     * @param scope The coroutine scope in which to launch the collection.
     * @param action A suspending function that takes a value of type T and performs an action.
     * @return A Job that can be used to cancel the collection.
     */
    fun connect(
        scope: CoroutineScope,
        action: suspend (T) -> Unit,
    ): Job =
        scope.launch {
            flow.collect { value ->
                action(value)
            }
        }

    /**
     * Connects an action to this signal using an internal scope.
     * The action will be executed for each value emitted by this signal.
     *
     * Note: It's recommended to use the scoped version of connect when possible
     * for better lifecycle management.
     *
     * @param action A suspending function that takes a value of type T and performs an action.
     * @return A connection that can be cancelled when no longer needed.
     */
    fun connect(action: suspend (T) -> Unit): Connection {
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.Default)

        val collectionJob =
            scope.launch {
                flow.collect { value ->
                    action(value)
                }
            }

        return Connection(collectionJob, job)
    }
}
