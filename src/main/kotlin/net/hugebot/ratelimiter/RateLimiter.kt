@file:Suppress("unused")

package net.hugebot.ratelimiter

import java.util.concurrent.*
import java.util.logging.Level
import java.util.logging.Logger

class RateLimiter private constructor(
    internal val executor: ScheduledExecutorService,
    internal val quota: Int,
    internal val expirationTime: Long,
    internal val unit: TimeUnit,
    private val listeners: List<ListenerAdapter>
) {
    internal val store: ConcurrentHashMap<Long, Bucket> = ConcurrentHashMap()
    private val forkJoinPool = ForkJoinPool.commonPool()

    private fun getBucket(id: Long) = store.computeIfAbsent(id) {
        Bucket(this, id)
    }

    /**
     * Check if an (User or Guild) id is rate limited
     */
    @Synchronized
    fun isRateLimited(id: Long): Boolean = getBucket(id).hit()

    @Synchronized
    fun isExceeded(id: Long): Boolean = getBucket(id).isExceeded()

    /**
     * Emit a bucket exceeded event.
     *
     * @param id The bucket id
     */
    internal fun emitBucketExceededEvent(id: Long) {
        forkJoinPool.execute {
            logger.log(Level.FINE, "Bucket with id $id was exceeded.")
            listeners.forEach {
                it.onBucketExceeded(id)
            }
        }
    }

    /**
     * Emit a bucket creation event.
     *
     * @param id The bucket id
     */
    internal fun emitBucketCreationEvent(id: Long) {
        forkJoinPool.execute {
            logger.log(Level.FINE, "Bucket with id $id was created.")
            listeners.forEach {
                it.onBucketCreation(id)
            }
        }
    }

    /**
     * Build a new RateLimiter instance
     *
     * @author BladeMaker
     */
    class Builder {
        private var quota: Int = 45
        private var expirationTime: Long = 60L
        private var unit: TimeUnit = TimeUnit.SECONDS
        private var executor: ScheduledExecutorService? = null
        private val listeners = mutableListOf<ListenerAdapter>()

        fun setQuota(quota: Int): Builder {
            this.quota = quota
            return this
        }

        fun setExpirationTime(time: Long, unit: TimeUnit): Builder {
            this.expirationTime = time
            this.unit = unit
            return this
        }

        fun withScheduledExecutor(scheduledExecutorService: ScheduledExecutorService): Builder {
            this.executor = scheduledExecutorService
            return this
        }

        fun addListener(listener: ListenerAdapter): Builder {
            listeners.add(listener)
            return this
        }

        fun build(): RateLimiter {
            if (executor == null) executor = Executors.newSingleThreadScheduledExecutor(ThreadFactoryBuilder())

            return RateLimiter(executor!!, quota, expirationTime, unit, listeners)
        }
    }

    companion object {
        private val logger = Logger.getLogger("RateLimiter")
    }
}