package com.janaai.controller;

import com.janaai.dto.response.ApiResponse;
import com.janaai.entity.GovernmentScheme;
import com.janaai.entity.Scholarship;
import com.janaai.repository.GovernmentSchemeRepository;
import com.janaai.repository.ScholarshipRepository;
import com.janaai.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class DatabaseLinkVerificationController {

    private final GovernmentSchemeRepository schemeRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final EmailService emailService;

    @GetMapping("/verify-links")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyAllLinks() {
        log.info("Starting verification of all database links with trust-all SSL and browser User-Agent...");
        List<GovernmentScheme> schemes = schemeRepository.findAll();
        List<Scholarship> scholarships = scholarshipRepository.findAll();

        List<LinkCheckResult> results = new ArrayList<>();
        HttpClient client;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }}, new SecureRandom());

            client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(4))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .sslContext(sslContext)
                    .build();
        } catch (Exception e) {
            log.error("Failed to build SSL-bypassed HttpClient: {}", e.getMessage());
            client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(4))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (GovernmentScheme scheme : schemes) {
            if (scheme.getOfficialWebsite() != null && !scheme.getOfficialWebsite().isBlank()) {
                futures.add(checkUrlAsync(client, scheme.getOfficialWebsite(), "Scheme: " + scheme.getSchemeName() + " (Official Website)", results));
            }
            if (scheme.getApplyLink() != null && !scheme.getApplyLink().isBlank()) {
                futures.add(checkUrlAsync(client, scheme.getApplyLink(), "Scheme: " + scheme.getSchemeName() + " (Apply Link)", results));
            }
        }

        for (Scholarship scholarship : scholarships) {
            if (scholarship.getOfficialWebsite() != null && !scholarship.getOfficialWebsite().isBlank()) {
                futures.add(checkUrlAsync(client, scholarship.getOfficialWebsite(), "Scholarship: " + scholarship.getName() + " (Official Website)", results));
            }
            if (scholarship.getApplyLink() != null && !scholarship.getApplyLink().isBlank()) {
                futures.add(checkUrlAsync(client, scholarship.getApplyLink(), "Scholarship: " + scholarship.getName() + " (Apply Link)", results));
            }
        }

        // Wait for all checks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long brokenCount = results.stream().filter(r -> !r.isOk()).count();

        Map<String, Object> data = new HashMap<>();
        data.put("totalChecked", results.size());
        data.put("brokenCount", brokenCount);
        data.put("results", results);

        return ResponseEntity.ok(ApiResponse.success(data, "Link verification completed"));
    }

    @GetMapping("/trigger-test-emails")
    public ResponseEntity<ApiResponse<String>> triggerTestEmails() {
        log.info("Triggering test emails to recipient...");
        try {
            // 1. Trigger new scheme notification
            emailService.sendNewSchemeNotification(
                    "PM Kisan Samman Nidhi Test",
                    "Financial assistance of Rs 6,000 per year to farmers.",
                    "All landholding farmer families",
                    java.time.LocalDate.now().plusDays(30),
                    "https://pmkisan.gov.in"
            );

            // 2. Trigger new scholarship notification
            emailService.sendNewScholarshipNotification(
                    "National Merit Scholarship Test",
                    "Scholarship for meritorious students from low-income families.",
                    "Class 12 passed with 80%+",
                    java.time.LocalDate.now().plusDays(45),
                    "https://scholarships.gov.in"
            );

            // 3. Trigger deadline reminder
            emailService.sendDeadlineReminder(
                    "Urgent: Swanath Scholarship Scheme Reminder",
                    "AICTE Swanath scholarship program for orphans/wards of deceased martyrs.",
                    "Orphan / Wards of armed forces deceased in action",
                    java.time.LocalDate.now().plusDays(5),
                    "https://www.aicte-india.org",
                    "tanvitaracharya@gmail.com"
            );

            return ResponseEntity.ok(ApiResponse.success("All test emails triggered successfully via SMTP.", "Emails Triggered"));
        } catch (Exception e) {
            log.error("Failed to trigger test emails: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("Failed to trigger test emails: " + e.getMessage()));
        }
    }

    private CompletableFuture<Void> checkUrlAsync(HttpClient client, String url, String description, List<LinkCheckResult> results) {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(4))
                        .build();

                HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
                int statusCode = response.statusCode();
                boolean isOk = statusCode >= 200 && statusCode < 400;
                synchronized (results) {
                    results.add(new LinkCheckResult(url, description, statusCode, null, isOk));
                }
            } catch (Exception e) {
                synchronized (results) {
                    results.add(new LinkCheckResult(url, description, 0, e.getMessage(), false));
                }
            }
        });
    }

    @lombok.Value
    private static class LinkCheckResult {
        String url;
        String description;
        int statusCode;
        String errorMessage;
        boolean ok;
    }
}
