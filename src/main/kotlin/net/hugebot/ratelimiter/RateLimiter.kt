package net.hugebot.ratelimiter

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class RateLimiter private constructor(
        internal val executor: ScheduledExecutorService,
        internal val quota: Int,
        internal val expirationTime: Long,
        internal val unit: TimeUnit
) {
    internal val store: ConcurrentHashMap<Long, Bucket> = ConcurrentHashMap()

    operator fun get(id: Long) = store[id]

    /**
     * Check if an (User or Guild) id is rate limited
     */
    @Synchronized
    fun isRateLimited(id: Long): Boolean {
        return try {
            val data = store[id]

            if (data != null) data.hit()
            else {
                store[id] = Bucket(this, id)
                false
            }

        } catch (ex: Throwable) {
            ex.printStackTrace()
            false
        }
    }

    /**
     * Build a new RateLimiter instance
     *
     * @param quota             The number of events allowed within the time period
     * @param expirationTime    Bucket life time
     * @param unit              Bucket life time unit
     * @param executor          ScheduledExecutorServices used so that each bucket can self-delete itself
     */
    class Builder {
        private var quota: Int = 45
        private var expirationTime: Long = 60L
        private var unit: TimeUnit = TimeUnit.SECONDS
        private var executor: ScheduledExecutorService? = null

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

        fun build(): RateLimiter {
            if (executor == null) executor = Executors.newSingleThreadScheduledExecutor(ThreadFactoryBuilder())

            return RateLimiter(executor!!, quota, expirationTime, unit)
        }
    }
}