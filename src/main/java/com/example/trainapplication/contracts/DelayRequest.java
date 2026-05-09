package com.example.trainapplication.contracts;

public record DelayRequest(Long trainId, Integer delayMinutes) {}