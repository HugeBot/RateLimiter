package net.hugebot.ratelimiter

data class Bucket(
    private val limiter: RateLimiter,
    val key: Long
) {
    private val store = limiter.store
    private val executor = limiter.executor
    private val quota = limiter.quota
    private val expirationTime = limiter.expirationTime
    private val unit = limiter.unit

    var exceded: Boolean = false
        private set
    var count: Int = 1
        private set

    init {
        executor.schedule({
            try {
                if (store.containsKey(key)) store.remove(key)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }, expirationTime, unit)
    }

    fun hit(): Boolean {
        if (exceded) return true

        count++

        if (count >= quota) {
            exceded = true
            return true
        }
        return false
    }
}