package com.portfolio.backend.service;

public interface RateLimiterService {

    boolean tryAcquire(String clientIp);
}

