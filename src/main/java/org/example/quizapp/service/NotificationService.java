package org.example.quizapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger emailLogger = LoggerFactory.getLogger("EMAIL_LOGGER");
    private static final Logger smsLogger = LoggerFactory.getLogger("SMS_LOGGER");
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Async
    public void sendEmailNotification(String to, String subject, String body) {
        logger.info("Sending email notification to: {}", to);
        try {
            // Simulate email sending delay
            Thread.sleep(1000);
            
            // Log to email log file
            emailLogger.info("=== EMAIL SENT ===");
            emailLogger.info("To: {}", to);
            emailLogger.info("Subject: {}", subject);
            emailLogger.info("Body: {}", body);
            emailLogger.info("Timestamp: {}", System.currentTimeMillis());
            emailLogger.info("==================");
            
            logger.info("Email notification sent successfully to: {}", to);
        } catch (InterruptedException e) {
            logger.error("Error sending email notification to: {}", to, e);
            Thread.currentThread().interrupt();
        }
    }

    @Async
    public void sendSMSNotification(String phoneNumber, String message) {
        logger.info("Sending SMS notification to: {}", phoneNumber);
        try {
            // Simulate SMS sending delay
            Thread.sleep(800);
            
            // Log to SMS log file
            smsLogger.info("=== SMS SENT ===");
            smsLogger.info("Phone: {}", phoneNumber);
            smsLogger.info("Message: {}", message);
            smsLogger.info("Timestamp: {}", System.currentTimeMillis());
            smsLogger.info("================");
            
            logger.info("SMS notification sent successfully to: {}", phoneNumber);
        } catch (InterruptedException e) {
            logger.error("Error sending SMS notification to: {}", phoneNumber, e);
            Thread.currentThread().interrupt();
        }
    }

    @Async
    public void sendQuizAttemptNotification(String username, String quizTitle, double score) {
        String emailSubject = "Quiz Attempt Result - " + quizTitle;
        String emailBody = String.format("Hello %s,\n\nYou have completed the quiz: %s\nYour score: %.2f%%\n\nThank you!",
                username, quizTitle, score);
        
        sendEmailNotification(username + "@example.com", emailSubject, emailBody);
        
        String smsMessage = String.format("Quiz '%s' completed! Score: %.2f%%", quizTitle, score);
        sendSMSNotification("+1234567890", smsMessage);
    }

    @Async
    public void sendRegistrationNotification(String username) {
        String emailSubject = "Welcome to Quiz Application";
        String emailBody = String.format("Hello %s,\n\nWelcome to our Quiz Application!\nYour account has been created successfully.\n\nHappy Learning!",
                username);
        
        sendEmailNotification(username + "@example.com", emailSubject, emailBody);
        
        String smsMessage = String.format("Welcome %s! Your quiz account is ready.", username);
        sendSMSNotification("+1234567890", smsMessage);
    }
}
