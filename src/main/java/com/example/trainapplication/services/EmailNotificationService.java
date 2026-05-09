package com.example.trainapplication.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements INotificationService{

    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendBookingConfirmation(String toEmail, String username, String trainName, Integer tickets) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Confirmation Mail");
        message.setText("Hello, " + username + ",\n\n" +
                "You have successfully booked " + tickets + " tickets for train " + trainName + ".\n" +
                "We wish you a happy journey!\n\n" +
                "Team Train Application");

        mailSender.send(message);
    }

    @Async
    @Override
    public void sendDelayNotification(String toEmail, String username, String trainName, Integer delayMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Alert Delay Train: " + trainName);
        message.setText("Hello " + username + ",\n\n" +
                "We inform you that the train " + trainName + " has a delay of " + delayMinutes + " minutes.\n" +
                "We apologize for the inconvenient!\n\n" +
                "Team Train Application");
        mailSender.send(message);
    }

    @Override
    @Async
    public void sendBookingCanceledNotification(String toEmail, String username, String trainName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Alert Canceled Booking: " + trainName);
        message.setText("Hello " + username + ",\n\n" +
                "We inform you that the booking for train " + trainName + " has been canceled.\n" +
                "We apologize for the inconvenient!\n\n" +
                "Team Train Application");
        mailSender.send(message);
    }

}
