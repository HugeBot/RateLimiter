package net.hugebot.ratelimiter

interface ListenerAdapter {
    fun onBucketCreation(id: Long) {}
    fun onBucketExceeded(id: Long) {}
}