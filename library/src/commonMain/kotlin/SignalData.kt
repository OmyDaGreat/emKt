package xyz.malefic.emkt

/**
 * A data class that wraps a value with associated parameters.
 * Used for emitting signals with additional metadata or context.
 *
 * @param T The type of the wrapped value.
 * @property value The main value being transmitted.
 * @property params Additional parameters associated with this signal.
 */
data class SignalData<T>(
    val value: T,
    val params: Map<String, Any?> = emptyMap(),
) {
    /**
     * Retrieves a parameter value by key.
     *
     * @param key The parameter key.
     * @return The parameter value, or null if the key doesn't exist.
     */
    fun <R> getParam(key: String): R? {
        @Suppress("UNCHECKED_CAST")
        return params[key] as? R
    }

    /**
     * Checks if a parameter with the given key exists.
     *
     * @param key The parameter key.
     * @return True if the parameter exists, false otherwise.
     */
    fun hasParam(key: String): Boolean = params.containsKey(key)
}
