package xyz.malefic.emkt

import kotlin.reflect.KClass

/**
 * Base type for all signals that can be emitted on a [SignalBus].
 *
 * Users should subclass this class to define specific signal types.
 */
open class Signal

/**
 * Lightweight event bus for registering handlers and emitting signals.
 *
 * Handlers are keyed by the concrete KClass of the emitted [Signal]. This class
 * is intentionally simple and does not provide thread-safety guarantees; if
 * handlers are added/removed or signals emitted from multiple threads, external
 * synchronization is required.
 */
open class SignalBus {
    /**
     * Map that associates an event type ([KClass]) with a list of handler functions.
     *
     * Each handler is stored as a function accepting [Any] to avoid repeated casts
     * when dispatching. Handlers should accept the specific [Signal] subtype when
     * registered via [on].
     */
    val handlers = mutableMapOf<KClass<*>, MutableList<(Any) -> Unit>>()

    /**
     * Register a handler for signals of type [T].
     *
     * The handler will be invoked with instances of [T] when such a signal is
     * emitted via [emit]. The returned [Connection] can be used to disconnect the
     * handler and prevent further invocations.
     *
     * @param T The concrete [Signal] subtype to listen for.
     * @param handler Lambda invoked when a [T] signal is emitted.
     * @return A [Connection] that disconnects the registered handler when closed.
     */
    inline fun <reified T : Signal> on(noinline handler: (T) -> Unit): Connection {
        val key = T::class
        handlers.getOrPut(key) { mutableListOf() }.add { signal -> handler(signal as T) }
        return {
            handlers[key]?.remove(handler)
        }
    }

    /**
     * Emit a signal to all handlers registered for the signal's concrete type.
     *
     * Handlers registered for the exact runtime class of [signal] will be invoked
     * in insertion order. No handlers for supertypes are invoked by this
     * implementation.
     *
     * @param T The concrete [Signal] subtype being emitted.
     * @param signal The signal instance to dispatch to registered handlers.
     */
    fun <T : Signal> emit(signal: T) {
        handlers[signal::class]?.forEach { it(signal) }
    }
}

/**
 * Represents a connection to an event handler registered with [SignalBus].
 *
 * Use the returned [Connection] to disconnect the handler and prevent further invocations.
 * Implementations are expected to remove internal references to the handler to avoid memory leaks.
 */
fun interface Connection {
    /**
     * Disconnects the associated event handler so it will no longer be invoked.
     *
     * Calling this multiple times should be safe and idempotent.
     */
    fun disconnect()
}

/**
 * Global, shared instance of [SignalBus] for convenient access.
 *
 * This is provided as a convenience for application-wide signals. For isolated subsystems
 * or tests, prefer creating a dedicated [SignalBus] instance instead of using this global.
 */
object Signals : SignalBus()
