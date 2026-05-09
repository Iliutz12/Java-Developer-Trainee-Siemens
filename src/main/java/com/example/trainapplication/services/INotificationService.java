package com.example.trainapplication.services;

public interface INotificationService {

    void sendBookingConfirmation(String toEmail, String username, String trainName, Integer tickets);
    void sendDelayNotification(String toEmail, String username, String trainName, Integer delayMinutes);
    void sendBookingCanceledNotification(String toEmail, String username, String trainName);
}
