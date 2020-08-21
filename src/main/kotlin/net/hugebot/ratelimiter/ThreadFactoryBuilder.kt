package net.hugebot.ratelimiter

import java.util.concurrent.ThreadFactory

internal class ThreadFactoryBuilder : ThreadFactory {
    override fun newThread(r: Runnable): Thread {
        val t = Thread(r)
        t.name = "HUGE-RateLimiter"
        return t
    }
}