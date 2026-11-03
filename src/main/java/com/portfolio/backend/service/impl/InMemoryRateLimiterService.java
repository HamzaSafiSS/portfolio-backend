package com.portfolio.backend.service.impl;

import com.portfolio.backend.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryRateLimiterService implements RateLimiterService {

    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Deque<Long>> requestLogs = new ConcurrentHashMap<>();

    public InMemoryRateLimiterService(
            @Value("${portfolio.rate-limit.max-requests:5}") int maxRequests,
            @Value("${portfolio.rate-limit.window-seconds:60}") int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowSeconds * 1000L;
    }

    @Override
    public synchronized boolean tryAcquire(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = "anonymous";
        }

        long now = System.currentTimeMillis();
        long cutoff = now - windowMillis;

        requestLogs.entrySet().removeIf(entry -> entry.getValue().isEmpty() || entry.getValue().peekLast() < cutoff);

        Deque<Long> timestamps = requestLogs.computeIfAbsent(clientIp, k -> new ArrayDeque<>());

        while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxRequests) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }
}

