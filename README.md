# RateLimiter
Command Rate Limiter used by HUGE bot

Easy implementation ``(JDA)``:
```kotlin

class MyMessageListener : ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        // Get the user ID
        val userId = event.author.idLong
        
        // Ignore the user if the user has exceeded the limit
        if (rateLimiter[userId]?.exceded == true) return
        
        // Take an action if the user has reached the limit but has not yet exceeded it
        if (rateLimiter.isRateLimited(userId)) return event.channel.sendMessage("<@!$userId>,  you are exceeding the limits...").queue()
        
        // Follow with the workflow
    }

    companion object {
        private val scheduler = Executors.newSingleThreadScheduledExecutor()
        
        // Creating a rate limiter with 45 events every 60 seconds
        private val rateLimiter = RateLimiter.Builder()
            // Set the events allowed
            .setQuota(45)
            // Set the expiration time
            .setExpirationTime(60, TimeUnit.SECONDS)
            // RateLimiter.Builder() creates it own threadpool but you can add your custom threadpool
            .withScheduledExecutor(scheduler)
            .build()
    }
}
```


Enjoy!!
