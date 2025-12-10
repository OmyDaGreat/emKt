package xyz.malefic.emkt

import kotlinx.coroutines.Job

/**
 * Represents a connection to a signal that can be cancelled.
 */
class Connection(
    private val collectionJob: Job,
    private val parentJob: Job,
) {
    /**
     * Cancels this connection, preventing any further actions from being executed.
     */
    fun cancel() {
        collectionJob.cancel()
        parentJob.cancel()
    }

    /**
     * Returns whether this connection is still active.
     */
    val isActive: Boolean
        get() = collectionJob.isActive
}
