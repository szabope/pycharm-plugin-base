package works.szabope.plugins.common.action

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

abstract class AbstractScanJobRegistry {
    private var job: Job? = null

    fun set(job: Job) {
        if (!isAvailable()) {
            throw IllegalStateException("Current job has not been completed!")
        }
        this.job = job
    }

    fun isAvailable() = job?.isCompleted ?: true

    fun isActive() = job?.isActive ?: false

    suspend fun cancel() {
        job?.cancelAndJoin()
    }
}