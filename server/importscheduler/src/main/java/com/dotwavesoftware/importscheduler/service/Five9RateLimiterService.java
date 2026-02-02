package com.dotwavesoftware.importscheduler.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

/**
 * Rate limiter for Five9 API requests.
 * Allows up to 20 requests, then all threads must wait 60 seconds before resuming.
 * All virtual threads share this single instance.
 */
@Service
public class Five9RateLimiterService {

    private static final Logger logger = Logger.getLogger(Five9RateLimiterService.class.getName());
    
    private static final int MAX_REQUESTS = 20;
    private static final long COOLDOWN_MS = 60_000; // 60 seconds
    
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition cooldownComplete = lock.newCondition();
    
    private volatile boolean inCooldown = false;
    private volatile long cooldownEndTime = 0;

    /**
     * Acquire permission to make a Five9 API request.
     * This method blocks if the rate limit has been reached until the cooldown period expires.
     * 
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void acquire() throws InterruptedException {
        lock.lock();
        try {
            // If we're in cooldown, wait until it's over
            while (inCooldown) {
                long waitTime = cooldownEndTime - System.currentTimeMillis();
                if (waitTime > 0) {
                    logger.info("Thread waiting for cooldown. Time remaining: " + (waitTime / 1000) + "s");
                    cooldownComplete.await(waitTime, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
                
                // Check if cooldown has expired
                if (System.currentTimeMillis() >= cooldownEndTime) {
                    endCooldown();
                }
            }
            
            // Increment counter and check if we've hit the limit
            int currentCount = requestCounter.incrementAndGet();
            logger.info("Five9 request count: " + currentCount + "/" + MAX_REQUESTS);
            
            if (currentCount >= MAX_REQUESTS) {
                startCooldown();
            }
            
        } finally {
            lock.unlock();
        }
    }

    /**
     * Start the cooldown period. All threads will wait until cooldown expires.
     */
    private void startCooldown() {
        inCooldown = true;
        cooldownEndTime = System.currentTimeMillis() + COOLDOWN_MS;
        logger.warning("=== RATE LIMIT REACHED (" + MAX_REQUESTS + " requests). Cooldown started for 60 seconds ===");
    }

    /**
     * End the cooldown period and reset the counter.
     */
    private void endCooldown() {
        requestCounter.set(0);
        inCooldown = false;
        cooldownEndTime = 0;
        logger.info("=== COOLDOWN COMPLETE. Counter reset. Resuming requests ===");
        cooldownComplete.signalAll();
    }

    /**
     * Get the current request count.
     */
    public int getCurrentCount() {
        return requestCounter.get();
    }

    /**
     * Check if currently in cooldown period.
     */
    public boolean isInCooldown() {
        return inCooldown;
    }

    /**
     * Get remaining cooldown time in seconds.
     */
    public long getRemainingCooldownSeconds() {
        if (!inCooldown) {
            return 0;
        }
        long remaining = cooldownEndTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : 0;
    }
}
