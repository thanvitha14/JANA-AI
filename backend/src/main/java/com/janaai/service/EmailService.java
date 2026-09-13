package com.janaai.service;

import java.time.LocalDate;

public interface EmailService {
    void sendNewSchemeNotification(String schemeName, String description, String eligibility, LocalDate deadline, String applyLink);
    void sendNewScholarshipNotification(String scholarshipName, String description, String eligibility, LocalDate deadline, String applyLink);
    void sendDeadlineReminder(String name, String description, String eligibility, LocalDate deadline, String applyLink, String recipientEmail);
}
