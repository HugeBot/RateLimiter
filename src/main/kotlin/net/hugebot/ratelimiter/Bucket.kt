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
        executor.schedule({
            try {
                if (store.containsKey(key)) store.remove(key)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }, expirationTime, unit)
    }

    fun isExceded(): Boolean = exceded

    fun hit(): Boolean {
        if (exceded) return true

        if (count.incrementAndGet() >= quota) {
            exceded = true
            return true
        }
        return false
    }
}