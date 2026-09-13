package com.janaai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.janaai.service.TranslationService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TranslationServiceImpl implements TranslationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Map<String, String>> staticDictionary = new HashMap<>();

    @Value("${app.ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String geminiBaseUrl;

    @Value("${app.ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    private static final String CACHE_FILE_PATH = "src/main/resources/translations_cache.json";

    @PostConstruct
    public void init() {
        // Build static dictionary for high performance and offline reliability
        buildStaticDictionary();

        try {
            File file = new File(CACHE_FILE_PATH);
            if (file.exists()) {
                Map<String, String> loaded = objectMapper.readValue(file, new TypeReference<Map<String, String>>() {});
                cache.putAll(loaded);
                log.info("Successfully loaded {} translation cache entries from {}", cache.size(), CACHE_FILE_PATH);
            } else {
                log.info("No translation cache file found at {}. Starting empty.", CACHE_FILE_PATH);
            }
        } catch (Exception e) {
            log.error("Failed to load translation cache file: {}", e.getMessage());
        }
    }

    private void buildStaticDictionary() {
        // Kannada Static Translations
        Map<String, String> knMap = new HashMap<>();
        knMap.put("education", "ಶಿಕ್ಷಣ");
        knMap.put("agriculture", "ಕೃಷಿ");
        knMap.put("healthcare", "ಆರೋಗ್ಯ ಸೇವೆ");
        knMap.put("finance", "ಹಣಕಾಸು");
        knMap.put("employment", "ಉದ್ಯೋಗ");
        knMap.put("senior citizens", "ಹಿರಿಯ ನಾಗರಿಕರು");
        knMap.put("social welfare", "ಸಮಾಜ ಕಲ್ಯಾಣ");
        knMap.put("women empowerment", "ಮಹಿಳಾ ಸಬಲೀಕರಣ");
        knMap.put("housing", "ವಸತಿ");
        knMap.put("rural development", "ಗ್ರಾಮೀಣಾಭಿವೃದ್ಧಿ");
        
        knMap.put("central", "ಕೇಂದ್ರ");
        knMap.put("andhra pradesh", "ಆಂಧ್ರಪ್ರದೇಶ");
        knMap.put("kerala", "ಕೇರಳ");
        knMap.put("karnataka", "ಕರ್ನಾಟಕ");
        knMap.put("tamil nadu", "ತಮಿಳುನಾಡು");
        knMap.put("maharashtra", "ಮಹಾರಾಷ್ಟ್ರ");
        knMap.put("rajasthan", "ರಾಜಸ್ಥಾನ");
        knMap.put("uttar pradesh", "ಉತ್ತರ ಪ್ರದೇಶ");
        knMap.put("madhya pradesh", "ಮಧ್ಯಪ್ರದೇಶ");
        knMap.put("west bengal", "ಪಶ್ಚಿಮ ಬಂಗಾಳ");
        knMap.put("bihar", "ಬಿಹಾರ");
        knMap.put("punjab", "ಪಂಜಾಬ್");
        knMap.put("gujarat", "ಗುಜರಾತ್");
        knMap.put("delhi", "ದೆಹಲಿ");
        knMap.put("assam", "ಅಸ್ಸಾಂ");
        knMap.put("odisha", "ಒಡಿಸ್ಸಾ");

        knMap.put("any", "ಯಾವುದಾದರೂ");
        knMap.put("undergraduate", "ಪದವಿ");
        knMap.put("postgraduate", "ಸ್ನಾತಕೋತ್ತರ");
        knMap.put("class 10", "ಹತ್ತನೇ ತರಗತಿ");
        knMap.put("class 12", "ಹನ್ನೆರಡನೇ ತರಗತಿ");
        knMap.put("diploma", "ಡಿಪ್ಲೊಮಾ");
        knMap.put("phd", "ಪಿಎಚ್‌ಡಿ");
        knMap.put("iti", "ಐಟಿಐ");
        knMap.put("class 8", "ಎಂಟನೇ ತರಗತಿ");

        knMap.put("all", "ಎಲ್ಲರಿಗೂ");
        knMap.put("male", "ಪುರುಷ");
        knMap.put("female", "ಮಹಿಳೆ");
        knMap.put("other", "ಇತರೆ");

        staticDictionary.put("kn", knMap);

        // Hindi Static Translations
        Map<String, String> hiMap = new HashMap<>();
        hiMap.put("education", "शिक्षा");
        hiMap.put("agriculture", "कृषि");
        hiMap.put("healthcare", "स्वास्थ्य सेवा");
        hiMap.put("finance", "वित्त");
        hiMap.put("employment", "रोजगार");
        hiMap.put("senior citizens", "वरिष्ठ नागरिक");
        hiMap.put("social welfare", "समाज कल्याण");
        hiMap.put("women empowerment", "महिला सशक्तिकरण");
        hiMap.put("housing", "आवास");
        hiMap.put("rural development", "ग्रामीण विकास");

        hiMap.put("central", "केंद्रीय");
        hiMap.put("andhra pradesh", "आंध्र प्रदेश");
        hiMap.put("kerala", "केरल");
        hiMap.put("karnataka", "कर्नाटक");
        hiMap.put("tamil nadu", "तमिलनाडु");
        hiMap.put("maharashtra", "महाराष्ट्र");
        hiMap.put("rajasthan", "राजस्थान");
        hiMap.put("uttar pradesh", "उत्तर प्रदेश");
        hiMap.put("madhya pradesh", "मध्य प्रदेश");
        hiMap.put("west bengal", "पश्चिम बंगाल");
        hiMap.put("bihar", "बिहार");
        hiMap.put("punjab", "पंजाब");
        hiMap.put("gujarat", "गुजरात");
        hiMap.put("delhi", "दिल्ली");
        hiMap.put("assam", "असम");
        hiMap.put("odisha", "ओडिशा");

        hiMap.put("any", "कोई भी");
        hiMap.put("undergraduate", "स्नातक");
        hiMap.put("postgraduate", "स्नातकोत्तर");
        hiMap.put("class 10", "कक्षा 10");
        hiMap.put("class 12", "कक्षा 12");
        hiMap.put("diploma", "डिप्लोमा");
        hiMap.put("phd", "पीएचडी");
        hiMap.put("iti", "आईटीआई");
        hiMap.put("class 8", "कक्षा 8");

        hiMap.put("all", "सभी");
        hiMap.put("male", "पुरुष");
        hiMap.put("female", "महिला");
        hiMap.put("other", "अन्य");

        staticDictionary.put("hi", hiMap);
    }

    private synchronized void saveCacheToFile() {
        try {
            File file = new File(CACHE_FILE_PATH);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, cache);
            log.debug("Saved translation cache containing {} entries.", cache.size());
        } catch (IOException e) {
            log.error("Failed to save translation cache to file: {}", e.getMessage());
        }
    }

    @Override
    public String translate(String text, String targetLang) {
        if (text == null || text.isBlank() || targetLang == null || "en".equalsIgnoreCase(targetLang)) {
            return text;
        }

        String targetLangLower = targetLang.toLowerCase();
        String textTrimmed = text.trim();

        // 1. Check static dictionary first
        Map<String, String> dict = staticDictionary.get(targetLangLower);
        if (dict != null) {
            String dictMatch = dict.get(textTrimmed.toLowerCase());
            if (dictMatch != null) {
                return dictMatch;
            }
        }

        // 2. Check dynamic persistent cache
        String cacheKey = targetLangLower + ":" + textTrimmed;
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        // 3. Fallback to Gemini API translation
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = geminiApiKey;
        }

        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("GEMINI_API_KEY")) {
            log.warn("Gemini API key not configured for TranslationService. Returning original text.");
            return text;
        }

        String langName = "kn".equalsIgnoreCase(targetLang) ? "Kannada" : "hi".equalsIgnoreCase(targetLang) ? "Hindi" : null;
        if (langName == null) {
            return text;
        }

        int maxRetries = 5;
        int delayMs = 1500;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String url = geminiBaseUrl + "/models/" + geminiModel + ":generateContent?key=" + apiKey;
                String prompt = "Translate the following text to " + langName + ". Respond ONLY with the translated text, do not include any introductions, explanations, notes, or extra characters. Text: " + text;
                
                Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                        Map.of("parts", List.of(
                            Map.of("text", prompt)
                        ))
                    )
                );

                Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
                if (response != null) {
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<String, Object> candidate = candidates.get(0);
                        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                        if (content != null) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                String translatedText = (String) parts.get(0).get("text");
                                if (translatedText != null) {
                                    translatedText = translatedText.trim();
                                    cache.put(cacheKey, translatedText);
                                    saveCacheToFile();
                                    return translatedText;
                                }
                            }
                        }
                    }
                }
                break;
            } catch (Exception e) {
                log.warn("Gemini translation attempt {} failed for '{}': {}. Retrying in {}ms...", 
                         attempt, text.length() > 20 ? text.substring(0, 20) + "..." : text, e.getMessage(), delayMs);
                if (attempt == maxRetries) {
                    log.error("All translation attempts failed for targetLang {}: {}", targetLang, e.getMessage());
                    break;
                }
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                delayMs *= 2;
            }
        }

        return text;
    }
}
