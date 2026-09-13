package com.janaai.service.impl;

import com.janaai.entity.User;
import com.janaai.repository.UserRepository;
import com.janaai.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username:noreply@janaai.org}")
    private String fromEmail;

    @Value("${app.mail.test-recipient:}")
    private String testRecipient;

    @Override
    @Async
    public void sendNewSchemeNotification(String schemeName, String description, String eligibility, LocalDate deadline, String applyLink) {
        log.info("Preparing batch email notifications for new scheme: {}", schemeName);
        List<User> users = userRepository.findAll();
        String title = "New Welfare Scheme Launched!";
        String deadlineStr = deadline != null ? deadline.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "Not Specified";

        List<MimeMessage> messages = new ArrayList<>();
        for (User user : users) {
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                MimeMessage message = createMimeMessage(user.getEmail(), title, schemeName, description, eligibility, deadlineStr, applyLink);
                if (message != null) {
                    messages.add(message);
                }
            }
        }

        sendBatchEmails(messages);
    }

    @Override
    @Async
    public void sendNewScholarshipNotification(String scholarshipName, String description, String eligibility, LocalDate deadline, String applyLink) {
        log.info("Preparing batch email notifications for new scholarship: {}", scholarshipName);
        List<User> users = userRepository.findAll();
        String title = "New Scholarship Program Announced!";
        String deadlineStr = deadline != null ? deadline.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "Not Specified";

        List<MimeMessage> messages = new ArrayList<>();
        for (User user : users) {
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                MimeMessage message = createMimeMessage(user.getEmail(), title, scholarshipName, description, eligibility, deadlineStr, applyLink);
                if (message != null) {
                    messages.add(message);
                }
            }
        }

        sendBatchEmails(messages);
    }

    @Override
    @Async
    public void sendDeadlineReminder(String name, String description, String eligibility, LocalDate deadline, String applyLink, String recipientEmail) {
        // Enforce a small delay to prevent rapid consecutive connection failures
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String title = "Upcoming Application Deadline Reminder!";
        String deadlineStr = deadline != null ? deadline.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "Not Specified";
        sendHtmlEmail(recipientEmail, title, name, description, eligibility, deadlineStr, applyLink);
    }

    private MimeMessage createMimeMessage(String to, String title, String name, String description, String eligibility, String deadline, String applyLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String recipient = to;
            if (testRecipient != null && !testRecipient.isBlank()) {
                recipient = testRecipient.trim();
            }

            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject(title + " - " + name);
            
            String htmlBody = buildHtmlTemplate(title, name, description, eligibility, deadline, applyLink);
            helper.setText(htmlBody, true);

            return message;
        } catch (Exception e) {
            log.error("Failed to construct MimeMessage for recipient {}: {}", to, e.getMessage());
            return null;
        }
    }

    private void sendBatchEmails(List<MimeMessage> messages) {
        if (messages.isEmpty()) {
            return;
        }
        try {
            log.info("Sending batch of {} emails via single SMTP connection...", messages.size());
            mailSender.send(messages.toArray(new MimeMessage[0]));
            log.info("Batch email delivery completed successfully.");
        } catch (Exception e) {
            log.error("Failed to deliver batch of emails: {}", e.getMessage(), e);
        }
    }

    private void sendHtmlEmail(String to, String title, String name, String description, String eligibility, String deadline, String applyLink) {
        String htmlBody = buildHtmlTemplate(title, name, description, eligibility, deadline, applyLink);
        
        String recipient = to;
        if (testRecipient != null && !testRecipient.isBlank()) {
            recipient = testRecipient.trim();
            log.info("Redirecting email from {} to test recipient: {}", to, recipient);
        }

        log.info("Attempting to send real SMTP email to: {}, Subject: {} - {}", recipient, title, name);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject(title + " - " + name);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Notification email successfully sent via SMTP to {}", recipient);
        } catch (Exception e) {
            log.error("SMTP delivery failed for recipient {}: {}", recipient, e.getMessage(), e);
            throw new RuntimeException("Real SMTP delivery failed: " + e.getMessage(), e);
        }
    }

    private String buildHtmlTemplate(String title, String name, String description, String eligibility, String deadline, String applyLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"utf-8\">" +
                "    <title>JANA AI Notification</title>" +
                "    <style>" +
                "        body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f7f9fc; color: #333333; margin: 0; padding: 20px; }" +
                "        .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e1e8ed; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }" +
                "        .header { background: linear-gradient(135deg, #1f4068, #162447); color: #ffffff; padding: 30px; text-align: center; }" +
                "        .header h1 { margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 0.5px; }" +
                "        .content { padding: 30px; line-height: 1.6; text-align: left; }" +
                "        .content h2 { color: #1f4068; margin-top: 0; font-size: 20px; }" +
                "        .info-group { margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #f0f4f8; }" +
                "        .label { font-weight: bold; color: #162447; font-size: 12px; text-transform: uppercase; margin-bottom: 5px; }" +
                "        .value { color: #4e5d6c; font-size: 15px; }" +
                "        .btn-container { text-align: center; margin-top: 30px; }" +
                "        .btn { display: inline-block; padding: 12px 30px; background-color: #e43f5a; color: #ffffff !important; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 15px; }" +
                "        .footer { background-color: #f0f4f8; text-align: center; padding: 20px; font-size: 12px; color: #a0aab5; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"card\">" +
                "        <div class=\"header\">" +
                "            <h1>JANA AI Updates</h1>" +
                "        </div>" +
                "        <div class=\"content\">" +
                "            <h2>" + title + "</h2>" +
                "            <div class=\"info-group\">" +
                "                <div class=\"label\">Name</div>" +
                "                <div class=\"value\">" + name + "</div>" +
                "            </div>" +
                "            <div class=\"info-group\">" +
                "                <div class=\"label\">Description</div>" +
                "                <div class=\"value\">" + description + "</div>" +
                "            </div>" +
                "            <div class=\"info-group\">" +
                "                <div class=\"label\">Eligibility Criteria</div>" +
                "                <div class=\"value\">" + eligibility + "</div>" +
                "            </div>" +
                "            <div class=\"info-group\">" +
                "                <div class=\"label\">Application Deadline</div>" +
                "                <div class=\"value\">" + deadline + "</div>" +
                "            </div>" +
                "            <div class=\"btn-container\">" +
                "                <a href=\"" + (applyLink != null ? applyLink : "https://scholarships.gov.in") + "\" class=\"btn\" target=\"_blank\">Apply Now</a>" +
                "            </div>" +
                "        </div>" +
                "        <div class=\"footer\">" +
                "            <p>You received this update because you are registered with JANA AI.</p>" +
                "            <p>&copy; 2026 JANA AI. Secure Welfare Portal.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
