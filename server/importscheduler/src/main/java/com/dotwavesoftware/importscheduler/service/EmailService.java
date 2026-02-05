package com.dotwavesoftware.importscheduler.service;

import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.logging.Logger;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.AddressException;
import org.springframework.mail.SimpleMailMessage;

@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private boolean isValidEmailAddress(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            logger.warning("Invalid email address: " + email);
            return false;
        }
    }

    public void sendWelcomeEmail(String to, String firstName) {
        if (!isValidEmailAddress(to)) {
            logger.warning("Attempted to send welcome email to invalid address: " + to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("jasontestingemail9@gmail.com");
            message.setTo(to);
            message.setSubject("Import Manager ");
            message.setText("Dear " + firstName + ",\n\n" +
                          "Thank you for registering to use the Import Manager. You will now be able to schedule import sessions to automatically transfer records from a HubSpot's CRM to Five9's dialing lists. \n\n" +
                          "Best regards,\n" +
                          "Infinity Systems LLC");

            mailSender.send(message);
            logger.info("Welcome email sent successfully to " + to);
        } catch (Exception e) {
            logger.warning("Failed to send welcome email to " + to + ": " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        if (!isValidEmailAddress(to)) {
            logger.warning("Attempted to send password reset email to invalid address: " + to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("jasontestingemail9@gmail.com");
            message.setTo(to);
            message.setSubject("Your Password Has Been Reset - Import Manager");
            message.setText("Hello,\n\n" +
                          "Your password has been successfully reset.\n\n" +
                          "If you did not make this change, please contact support immediately at jasontestingemail9@gmail.com.\n\n" +
                          "For your security, we recommend:\n" +
                          "- Using a strong, unique password\n" +
                          "- Not sharing your password with anyone\n" +
                          "- Enabling two-factor authentication if available\n\n" +
                          "Best regards,\n" +
                          "Infinity Systems LLC");

            mailSender.send(message);
            logger.info("Password reset confirmation email sent successfully to " + to);
        } catch (Exception e) {
            logger.warning("Failed to send password reset email to " + to + ": " + e.getMessage());
        }
    }

    /**
     * Send email notification when an import session has started
     * @param to The recipient email address
     * @param importName The name of the import
     * @param totalRecords The total number of records to be imported
     */
    public void sendImportStartedEmail(String to, String importName, int totalRecords) {
        if (!isValidEmailAddress(to)) {
            logger.warning("Attempted to send import started email to invalid address: " + to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("jasontestingemail9@gmail.com");
            message.setTo(to);
            message.setSubject("Import Started: " + importName + " - Import Manager");
            message.setText("Hello,\n\n" +
                          "Your import session has started.\n\n" +
                          "Import Details:\n" +
                          "- Name: " + importName + "\n" +
                          "- Total Records: " + totalRecords + "\n" +
                          "- Status: In Progress\n\n" +
                          "You will receive another notification when the import is complete.\n\n" +
                          "Best regards,\n" +
                          "Infinity Systems LLC");

            mailSender.send(message);
            logger.info("Import started email sent successfully to " + to + " for import: " + importName);
        } catch (Exception e) {
            logger.warning("Failed to send import started email to " + to + ": " + e.getMessage());
        }
    }

    /**
     * Send email notification when an import session has completed
     * @param to The recipient email address
     * @param importName The name of the import
     * @param success Whether the import completed successfully
     * @param recordsImported The number of records successfully imported
     * @param totalRecords The total number of records attempted
     */
    public void sendImportCompletedEmail(String to, String importName, boolean success, int recordsImported, int totalRecords) {
        if (!isValidEmailAddress(to)) {
            logger.warning("Attempted to send import completed email to invalid address: " + to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("jasontestingemail9@gmail.com");
            message.setTo(to);
            
            String status = success ? "Completed Successfully" : "Completed with Errors";
            message.setSubject("Import " + status + ": " + importName + " - Import Manager");
            
            String resultText = success 
                ? "Your import session has completed successfully.\n\n"
                : "Your import session has completed with some errors.\n\n";
            
            message.setText("Hello,\n\n" +
                          resultText +
                          "Import Details:\n" +
                          "- Name: " + importName + "\n" +
                          "- Records Imported: " + recordsImported + "\n" +
                          "- Total Records: " + totalRecords + "\n" +
                          "- Status: " + status + "\n\n" +
                          "Keep in mind, a record must have a phone number in order to be imported into Five9's Dialing lists.\n" +
                          "You can view the full details in the Import Manager dashboard.\n\n" +
                          "Best regards,\n" +
                          "Infinity Systems LLC");

            mailSender.send(message);
            logger.info("Import completed email sent successfully to " + to + " for import: " + importName);
        } catch (Exception e) {
            logger.warning("Failed to send import completed email to " + to + ": " + e.getMessage());
        }
    }
} 