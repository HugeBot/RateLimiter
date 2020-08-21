package net.hugebot.ratelimiter

import java.util.concurrent.*

class RateLimiter private constructor(
    internal val executor: ScheduledExecutorService,
    internal val quota: Int,
    internal val expirationTime: Long,
    internal val unit: TimeUnit
){
    internal val store: ConcurrentHashMap<Long, Bucket> = ConcurrentHashMap()

    fun getRateLimited(id: Long) = store[id]

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