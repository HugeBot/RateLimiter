package net.hugebot.ratelimiter

import java.util.concurrent.atomic.AtomicInteger

data class Bucket(
        private val limiter: RateLimiter,
        val key: Long
) {
    private val store = limiter.store
    private val executor = limiter.executor
    private val quota = limiter.quota
    private val expirationTime = limiter.expirationTime
    private val unit = limiter.unit

    private var exceded: Boolean = false
    private var count: AtomicInteger = AtomicInteger(0)

    init {
        limiter.emitBucketCreationEvent(key)
        executor.schedule({
            try {
                if (store.containsKey(key)) store.remove(key)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }, expirationTime, unit)
    }

    fun isExceeded(): Boolean = exceded

    fun hit(): Boolean {
        if (exceded) return true

        if (count.incrementAndGet() >= quota) {
            limiter.emitBucketExceededEvent(key)
            exceded = true
            return true
        }
        return false
    }
}