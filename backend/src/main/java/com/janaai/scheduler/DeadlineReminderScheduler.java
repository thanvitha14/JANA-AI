package com.janaai.scheduler;

import com.janaai.entity.GovernmentScheme;
import com.janaai.entity.Scholarship;
import com.janaai.entity.User;
import com.janaai.repository.GovernmentSchemeRepository;
import com.janaai.repository.ScholarshipRepository;
import com.janaai.repository.UserRepository;
import com.janaai.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineReminderScheduler {

    private final GovernmentSchemeRepository schemeRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // Run every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkAndSendDeadlineReminders() {
        log.info("Starting scheduled deadline check for schemes and scholarships...");
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysFromNow = today.plusDays(7);

        List<GovernmentScheme> schemesNearDeadline = schemeRepository.findAll().stream()
                .filter(s -> s.isActive() && s.getDeadline() != null 
                        && !s.getDeadline().isBefore(today) && !s.getDeadline().isAfter(sevenDaysFromNow))
                .toList();

        List<Scholarship> scholarshipsNearDeadline = scholarshipRepository.findAll().stream()
                .filter(s -> s.getDeadline() != null 
                        && !s.getDeadline().isBefore(today) && !s.getDeadline().isAfter(sevenDaysFromNow))
                .toList();

        if (schemesNearDeadline.isEmpty() && scholarshipsNearDeadline.isEmpty()) {
            log.info("No schemes or scholarships with deadlines in the next 7 days.");
            return;
        }

        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                continue;
            }

            // Reminders for matching Schemes
            for (GovernmentScheme scheme : schemesNearDeadline) {
                if (isUserEligibleForScheme(user, scheme)) {
                    log.info("Sending scheme deadline reminder to {} for {}", user.getEmail(), scheme.getSchemeName());
                    emailService.sendDeadlineReminder(
                            scheme.getSchemeName(),
                            scheme.getDescription(),
                            scheme.getEligibility(),
                            scheme.getDeadline(),
                            scheme.getApplyLink(),
                            user.getEmail()
                    );
                }
            }

            // Reminders for matching Scholarships
            for (Scholarship scholarship : scholarshipsNearDeadline) {
                if (isUserEligibleForScholarship(user, scholarship)) {
                    log.info("Sending scholarship deadline reminder to {} for {}", user.getEmail(), scholarship.getName());
                    emailService.sendDeadlineReminder(
                            scholarship.getName(),
                            scholarship.getDescription(),
                            scholarship.getEligibility(),
                            scholarship.getDeadline(),
                            scholarship.getApplyLink(),
                            user.getEmail()
                    );
                }
            }
        }
    }



    private boolean isUserEligibleForScheme(User user, GovernmentScheme scheme) {
        // State Match (if defined, and not Central / ALL)
        if (scheme.getState() != null && !scheme.getState().isBlank() 
                && !"Central".equalsIgnoreCase(scheme.getState()) && !"ALL".equalsIgnoreCase(scheme.getState())) {
            if (user.getState() == null || !scheme.getState().equalsIgnoreCase(user.getState())) {
                return false;
            }
        }

        // Gender Match (if defined, and not ALL)
        if (scheme.getGender() != null && !"ALL".equalsIgnoreCase(scheme.getGender())) {
            if (user.getGender() == null || !scheme.getGender().equalsIgnoreCase(user.getGender())) {
                return false;
            }
        }

        // Age Match
        if (user.getDateOfBirth() != null) {
            int age = Period.between(user.getDateOfBirth(), LocalDate.now()).getYears();
            if (scheme.getMinimumAge() != null && age < scheme.getMinimumAge()) {
                return false;
            }
            if (scheme.getMaximumAge() != null && age > scheme.getMaximumAge()) {
                return false;
            }
        }

        return true;
    }

    private boolean isUserEligibleForScholarship(User user, Scholarship scholarship) {
        // State Match
        if (scholarship.getState() != null && !scholarship.getState().isBlank() 
                && !"Central".equalsIgnoreCase(scholarship.getState()) && !"ALL".equalsIgnoreCase(scholarship.getState())) {
            if (user.getState() == null || !scholarship.getState().equalsIgnoreCase(user.getState())) {
                return false;
            }
        }

        // Category Match
        if (scholarship.getCategory() != null && !scholarship.getCategory().isBlank() && !"General".equalsIgnoreCase(scholarship.getCategory())) {
            // General match logic
        }

        return true;
    }
}
