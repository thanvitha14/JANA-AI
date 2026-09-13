package com.janaai.service.impl;

import com.janaai.dto.chat.ChatConversationResponse;
import com.janaai.entity.ChatMessage;
import com.janaai.entity.User;
import com.janaai.repository.ChatMessageRepository;
import com.janaai.service.ChatService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.ai.provider:gemini}")
    private String aiProvider;

    @Value("${app.ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String geminiBaseUrl;

    @Value("${app.ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Value("${app.ai.openai.api-key:}")
    private String openaiApiKey;

    @Value("${app.ai.openai.base-url:https://api.openai.com/v1}")
    private String openaiBaseUrl;

    @Value("${app.ai.openai.model:gpt-4o-mini}")
    private String openaiModel;

    public ChatServiceImpl(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public ChatMessage sendMessage(User user, String message, String conversationId, String language) {
        ChatMessage chat = new ChatMessage();
        chat.setUserMessage(message);
        chat.setUserId(user.getId());
        chat.setConversationId(conversationId == null ? UUID.randomUUID().toString() : conversationId);

        String aiResponse = getAIResponse(message, language);
        chat.setAiResponse(aiResponse);
        chat.setCreatedAt(LocalDateTime.now());

        return chatMessageRepository.save(chat);
    }

    private boolean isGeneralQuery(String message) {
        if (message == null) return true;
        String msgLower = message.toLowerCase();
        
        List<String> domainKeywords = List.of(
            "scheme", "scholarship", "college", "school", "matric", "postgraduate", "undergraduate", "grant",
            "legal", "law", "court", "advocate", "justice", "rights", "hospital", "clinic", "medical", "doctor",
            "healthcare", "health", "ngo", "non-profit", "charity", "foundation", "emergency", "police", 
            "ambulance", "fire", "helpline", "rescue", "welfare", "goverment", "government", "central", "state",
            "pmkisan", "awas yojana", "lado protsahan", "women", "gender", "female", "girl", "farm", "subsidy",
            "agriculture", "kisan", "land", "jandhan", "jan dhan", "bank", "finance", "card", "pmjdy", "account",
            "database", "users", "profile"
        );
        
        for (String keyword : domainKeywords) {
            if (msgLower.contains(keyword)) {
                return false;
            }
        }
        return true;
    }

    private String getAIResponse(String message, String language) {
        if (!isGeneralQuery(message)) {
            log.info("Domain-specific JANA AI query detected: '{}'. Routing to offline response logic.", message);
            return getSimulatedResponse(message, language);
        }

        if ("gemini".equalsIgnoreCase(aiProvider)) {
            String apiKey = System.getenv("GEMINI_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                apiKey = geminiApiKey;
            }

            if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("GEMINI_API_KEY")) {
                log.info("Gemini API key detected: SUCCESS (length: {})", apiKey.length());
            } else {
                log.warn("Gemini API key detected: FAILED (Not configured)");
                log.info("Falling back to simulated response due to missing API key.");
                return getSimulatedResponse(message, language);
            }

            log.info("Gemini Model name configured: {}", geminiModel);

            try {
                String url = geminiBaseUrl + "/models/" + geminiModel + ":generateContent?key=" + apiKey;
                
                // Multilingual prompt formatting instruction prefix
                String systemInstruction = "";
                if ("kn".equalsIgnoreCase(language)) {
                    systemInstruction = "You must respond ONLY in simple, correct, and readable Kannada (ಕನ್ನಡ). Translate any schemes, benefits, and instructions to Kannada. Do not use English text in the response except for official names or link URLs. User query: ";
                } else if ("hi".equalsIgnoreCase(language)) {
                    systemInstruction = "You must respond ONLY in simple, correct, and readable Hindi (हिन्दी). Translate any schemes, benefits, and instructions to Hindi. Do not use English text in the response except for official names or link URLs. User query: ";
                } else {
                    systemInstruction = "You must respond in English. User query: ";
                }
                
                Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                        Map.of("parts", List.of(
                            Map.of("text", systemInstruction + message)
                        ))
                    )
                );
                
                log.info("Executing Gemini API HTTP request to url: {}/models/{}:generateContent", geminiBaseUrl, geminiModel);
                org.springframework.http.ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, requestBody, Map.class);
                
                log.info("Gemini API HTTP Status code: {}", responseEntity.getStatusCode());
                Map<String, Object> response = responseEntity.getBody();
                
                if (response != null) {
                    log.info("Starting parsing of Gemini API JSON response...");
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<String, Object> candidate = candidates.get(0);
                        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                        if (content != null) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                String text = (String) parts.get(0).get("text");
                                log.info("Successfully parsed response text from Gemini API.");
                                return text;
                            }
                        }
                    }
                    log.warn("Gemini API response structure parsed, but no text content was found.");
                } else {
                    log.warn("Gemini API returned an empty response body.");
                }
            } catch (Exception e) {
                log.error("Exception occurred while calling Gemini API. Printing full stack trace:", e);
            }
            log.info("Falling back to simulated response due to API call failure or empty response.");
            return getSimulatedResponse(message, language);
        } else if ("openai".equalsIgnoreCase(aiProvider)) {
            if (openaiApiKey == null || openaiApiKey.trim().isEmpty() || openaiApiKey.contains("OPENAI_API_KEY")) {
                log.info("OpenAI API key is not configured. Falling back to simulated response.");
                return getSimulatedResponse(message, language);
            }
            try {
                String url = openaiBaseUrl + "/chat/completions";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(openaiApiKey);

                String systemInstruction = "";
                if ("kn".equalsIgnoreCase(language)) {
                    systemInstruction = "You must respond ONLY in simple, correct, and readable Kannada (ಕನ್ನಡ). Translate any schemes, benefits, and instructions to Kannada. Do not use English text in the response except for official names or link URLs. User query: ";
                } else if ("hi".equalsIgnoreCase(language)) {
                    systemInstruction = "You must respond ONLY in simple, correct, and readable Hindi (हिन्दी). Translate any schemes, benefits, and instructions to Hindi. Do not use English text in the response except for official names or link URLs. User query: ";
                } else {
                    systemInstruction = "You must respond in English. User query: ";
                }

                Map<String, Object> requestBody = Map.of(
                    "model", openaiModel,
                    "messages", List.of(
                        Map.of("role", "user", "content", systemInstruction + message)
                    )
                );

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
                if (response != null) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        Map<String, Object> msg = (Map<String, Object>) choice.get("message");
                        if (msg != null) {
                            return (String) msg.get("content");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error calling OpenAI API: " + e.getMessage() + ". Falling back to simulated response.");
            }
        }
        
        return getSimulatedResponse(message, language);
    }

    private String getSimulatedResponse(String message, String language) {
        if ("kn".equalsIgnoreCase(language)) {
            return getSimulatedResponseKn(message);
        } else if ("hi".equalsIgnoreCase(language)) {
            return getSimulatedResponseHi(message);
        }
        return getSimulatedResponseEn(message);
    }

    private String getSimulatedResponseEn(String message) {
        String msgLower = message.toLowerCase();
        if (msgLower.contains("women") || msgLower.contains("gender") || msgLower.contains("female") || msgLower.contains("girl")) {
            return "### JANA AI Offline Assistance (Women Empowerment Schemes)\n\n" +
                   "Here are some schemes focused on women empowerment and education:\n\n" +
                   "1. **Lado Protsahan Yojana (Rajasthan)**: Offers financial incentives of up to ₹1,00,000 in savings bonds for girl students completing their education from weaker sections.\n\n" +
                   "2. **Pradhan Mantri Matru Vandana Yojana**: Direct benefit transfer for pregnant/lactating mothers to compensate for wage loss.\n\n" +
                   "3. **Support to Training and Employment Programme for Women (STEP)**: Provides skills to women to support employment/entrepreneurship.\n\n" +
                   "To view all available schemes, please check our **Government Schemes** tab!";
        } else if (msgLower.contains("farm") || msgLower.contains("subsidy") || msgLower.contains("agriculture") || msgLower.contains("kisan") || msgLower.contains("land")) {
            return "### JANA AI Offline Assistance (Agriculture & Farming Subsidy)\n\n" +
                   "Here are active agriculture initiatives:\n\n" +
                   "1. **PM Kisan Samman Nidhi**: Direct income support of ₹6,00,000 per year in three equal installments to small and landholding farmer families.\n\n" +
                   "2. **Pradhan Mantri Fasal Bima Yojana (PMFBY)**: Crop insurance scheme for farmers to safeguard against natural calamities.\n\n" +
                   "3. **Subsidies on Farm Machinery**: State-level subsidies on tractors, drip irrigation, and harvesting equipment.\n\n" +
                   "To search and filter agriculture schemes, navigate to the **Government Schemes** tab!";
        } else if (msgLower.contains("scholarship") || msgLower.contains("college") || msgLower.contains("school") || msgLower.contains("matric") || msgLower.contains("postgraduate") || msgLower.contains("undergraduate") || msgLower.contains("grant")) {
            return "### JANA AI Offline Assistance (Scholarship Services)\n\n" +
                   "We found several scholarship schemes in our database:\n\n" +
                   "1. **Post Matric Scholarship Scheme for SC Students**: 100% tuition fee reimbursement and maintenance allowance for SC students pursuing professional undergraduate/postgraduate degrees.\n\n" +
                   "2. **National Means Cum Merit Scholarship**: Merit-based grant of ₹12,000 per year for Class 9 to 12 government school students with family income under ₹3.5 LPA.\n\n" +
                   "3. **Central Sector Scheme of Scholarship**: Offers up to ₹20,000 per year to college/university students in the top 20th percentile of their Class 12 boards.\n\n" +
                   "Check the **Scholarship Finder** page for full eligibility criteria and direct application portals!";
        } else if (msgLower.contains("jandhan") || msgLower.contains("jan dhan") || msgLower.contains("bank") || msgLower.contains("finance") || msgLower.contains("card") || msgLower.contains("pmjdy") || msgLower.contains("account")) {
            return "### JANA AI Offline Assistance (Financial & Banking Services)\n\n" +
                   "1. **Pradhan Mantri Jan Dhan Yojana (PMJDY)**: National financial inclusion mission providing zero-balance savings accounts, accidental insurance cover of ₹2 Lakh, and overdraft facilities of up to ₹10,000.\n\n" +
                   "2. **PM Kisan Credit Card (KCC)**: Easy access to short-term credit and loans for farmers at subsidized interest rates.\n\n" +
                   "3. **Atal Pension Yojana (APY)**: Guaranteed monthly pension scheme for citizens in the unorganized sector aged 18 to 40 years.";
        }

        return "### JANA AI Smart Assistant\n\n" +
               "Hello! I am JANA AI, your personal guide to government social programs, legal aid, and scholarship programs. \n\n" +
               "I can help you look up specific information on schemes like:\n" +
               "- **Pradhan Mantri Jan Dhan Yojana (PMJDY)** (Financial Inclusion)\n" +
               "- **Ayushman Bharat PM-JAY** (Healthcare)\n" +
               "- **PM Kisan Samman Nidhi** (Agriculture)\n" +
               "- **Lado Protsahan Yojana** (Rajasthan state women welfare)\n" +
               "- **SC/ST/OBC Post-Matric Scholarships** (Education)\n\n" +
               "Feel free to ask questions like: *'How do I apply for PM Jan Dhan?'*, *'What scholarships are available for college students?'*, or *'Show me women empowerment schemes'*. You can also browse them directly in the **Government Schemes** or **Scholarship Finder** navigation tabs!";
    }

    private String getSimulatedResponseKn(String message) {
        String msgLower = message.toLowerCase();
        if (msgLower.contains("women") || msgLower.contains("gender") || msgLower.contains("female") || msgLower.contains("girl")) {
            return "### JANA AI ಆಫ್‌ಲೈನ್ ನೆರವು (ಮಹಿಳಾ ಸಬಲೀಕರಣ ಯೋಜನೆಗಳು)\n\n" +
                   "ಮಹಿಳಾ ಸಬಲೀಕರಣ ಮತ್ತು ಶಿಕ್ಷಣದ ಮೇಲೆ ಕೇಂದ್ರೀಕರಿಸಿದ ಕೆಲವು ಯೋಜನೆಗಳು ಇಲ್ಲಿವೆ:\n\n" +
                   "1. **ಲಾಡೋ ಪ್ರೋತ್ಸಾಹ ಯೋಜನೆ (ರಾಜಸ್ಥಾನ)**: ದುರ್ಬಲ ವರ್ಗಗಳ ಹೆಣ್ಣು ಮಕ್ಕಳು ತಮ್ಮ ಶಿಕ್ಷಣವನ್ನು ಪೂರ್ಣಗೊಳಿಸಿದರೆ ಉಳಿತಾಯ ಬಾಂಡ್‌ಗಳಲ್ಲಿ ₹1,00,000 ವರೆಗೆ ಆರ್ಥಿಕ ಪ್ರೋತ್ಸಾಹವನ್ನು ನೀಡುತ್ತದೆ.\n\n" +
                   "2. **ಪ್ರಧಾನ ಮಂತ್ರಿ ಮಾತೃ ವಂದನಾ ಯೋಜನೆ**: ಗರ್ಭಿಣಿಯರು/ಹಾಲುಣಿಸುವ ತಾಯಂದಿರಿಗೆ ವೇತನ ನಷ್ಟವನ್ನು ಸರಿದೂಗಿಸಲು ನೇರ ಲಾಭ ವರ್ಗಾವಣೆ.\n\n" +
                   "3. **ಮಹಿಳೆಯರ ತರಬೇತಿ ಮತ್ತು ಉದ್ಯೋಗ ಬೆಂಬಲ ಕಾರ್ಯಕ್ರಮ (STEP)**: ಉದ್ಯೋಗ/ಉದ್ಯಮಶೀಲತೆಯನ್ನು ಬೆಂಬಲಿಸಲು ಮಹಿಳೆಯರಿಗೆ ಕೌಶಲ್ಯಗಳನ್ನು ಒದಗಿಸುತ್ತದೆ.\n\n" +
                   "ಲಭ್ಯವಿರುವ ಎಲ್ಲಾ ಯೋಜನೆಗಳನ್ನು ವೀಕ್ಷಿಸಲು, ದಯವಿಟ್ಟು **ಸರ್ಕಾರಿ ಯೋಜನೆಗಳು** ಟ್ಯಾಬ್ ಪರಿಶೀಲಿಸಿ!";
        } else if (msgLower.contains("farm") || msgLower.contains("subsidy") || msgLower.contains("agriculture") || msgLower.contains("kisan") || msgLower.contains("land")) {
            return "### JANA AI ಆಫ್‌ಲೈನ್ ನೆರವು (ಕೃಷಿ ಮತ್ತು ಕೃಷಿ ಸಹಾಯಧನ)\n\n" +
                   "ಸಕ್ರಿಯ ಕೃಷಿ ಉಪಕ್ರಮಗಳು ಇಲ್ಲಿವೆ:\n\n" +
                   "1. **ಪಿಎಂ ಕಿಸಾನ್ ಸಮ್ಮಾನ್ ನಿಧಿ**: ಸಣ್ಣ ಮತ್ತು ಭೂಹಿಡುವಳಿ ರೈತ ಕುಟುಂಬಗಳಿಗೆ ವರ್ಷಕ್ಕೆ ₹6,000 ನೇರ ಆದಾಯ ಬೆಂಬಲ.\n\n" +
                   "2. **ಪ್ರಧಾನ ಮಂತ್ರಿ ಫಸಲ್ ಬಿಮಾ ಯೋಜನೆ (PMFBY)**: ನೈಸರ್ಗಿಕ ವಿಕೋಪಗಳ ವಿರುದ್ಧ ರೈತರನ್ನು ರಕ್ಷಿಸಲು ಬೆಳೆ ವಿಮೆ ಯೋಜನೆ.\n\n" +
                   "3. **ಕೃಷಿ ಯಂತ್ರೋಪಕರಣಗಳ ಮೇಲಿನ ಸಹಾಯಧನ**: ಟ್ರ್ಯಾಕ್ಟರ್‌ಗಳು, ಹನಿ ನೀರಾವರಿ ಮತ್ತು ಕೊಯ್ಲು ಉಪಕರಣಗಳ ಮೇಲೆ ರಾಜ್ಯ ಮಟ್ಟದ ಸಹಾಯಧನಗಳು.\n\n" +
                   "ಕೃಷಿ ಯೋಜನೆಗಳನ್ನು ಹುಡುಕಲು ಮತ್ತು ಫಿಲ್ಟರ್ ಮಾಡಲು, **ಸರ್ಕಾರಿ ಯೋಜನೆಗಳು** ಟ್ಯಾಬ್‌ಗೆ ನ್ಯಾವಿಗೇಟ್ ಮಾಡಿ!";
        } else if (msgLower.contains("scholarship") || msgLower.contains("college") || msgLower.contains("school") || msgLower.contains("matric") || msgLower.contains("postgraduate") || msgLower.contains("undergraduate") || msgLower.contains("grant")) {
            return "### JANA AI ಆಫ್‌ಲೈನ್ ನೆರವು (ಶಿಷ್ಯವೇತನ ಸೇವೆಗಳು)\n\n" +
                   "ನಮ್ಮ ಡೇಟಾಬೇಸ್‌ನಲ್ಲಿ ಹಲವಾರು ಶಿಷ್ಯವೇತನ ಯೋಜನೆಗಳನ್ನು ನಾವು ಕಂಡುಕೊಂಡಿದ್ದೇವೆ:\n\n" +
                   "1. **ಪರಿಶಿಷ್ಟ ಜಾತಿ ವಿದ್ಯಾರ್ಥಿಗಳಿಗೆ ಮೆಟ್ರಿಕ್ ನಂತರದ ಶಿಷ್ಯವೇತನ ಯೋಜನೆ**: ವೃತ್ತಿಪರ ಪದವಿ/ಸ್ನಾತಕೋತ್ತರ ಪದವಿಗಳನ್ನು ಪಡೆಯುವ ಎಸ್‌ಸಿ ವಿದ್ಯಾರ್ಥಿಗಳಿಗೆ 100% ಬೋಧನಾ ಶುಲ್ಕ ಮರುಪಾವತಿ.\n\n" +
                   "2. **ರಾಷ್ಟ್ರೀಯ ಸಾಧನ ಮತ್ತು ಯೋಗ್ಯತೆ ಶಿಷ್ಯವೇತನ**: ಕುಟುಂಬದ ಆದಾಯ ₹3.5 ಲಕ್ಷಕ್ಕಿಂತ ಕಡಿಮೆ ಇರುವ 9 ರಿಂದ 12 ನೇ ತರಗತಿ ವಿದ್ಯಾರ್ಥಿಗಳಿಗೆ ವರ್ಷಕ್ಕೆ ₹12,000 ರ ಮೆರಿಟ್ ಆಧಾರಿತ ಅನುದಾನ.\n\n" +
                   "3. **ಶಿಷ್ಯವೇತನದ ಕೇಂದ್ರ ವಲಯದ ಯೋಜನೆ**: ಕಾಲೇಜು/ವಿಶ್ವವಿದ್ಯಾನಿಲಯದ ವಿದ್ಯಾರ್ಥಿಗಳಿಗೆ ಅವರ 12 ನೇ ತರಗತಿ ಬೋರ್ಡ್‌ಗಳಲ್ಲಿ ಉನ್ನತ ಸ್ಥಾನದಲ್ಲಿರುವವರಿಗೆ ವರ್ಷಕ್ಕೆ ₹20,000 ವರೆಗೆ ನೀಡುತ್ತದೆ.\n\n" +
                   "ಪೂರ್ಣ ಅರ್ಹತೆಯ ಮಾನದಂಡಗಳಿಗಾಗಿ **ಶಿಷ್ಯವೇತನ** ಪುಟವನ್ನು ಪರಿಶೀಲಿಸಿ!";
        } else if (msgLower.contains("jandhan") || msgLower.contains("jan dhan") || msgLower.contains("bank") || msgLower.contains("finance") || msgLower.contains("card") || msgLower.contains("pmjdy") || msgLower.contains("account")) {
            return "### JANA AI ಆಫ್‌ಲೈನ್ ನೆರವು (ಹಣಕಾಸು ಮತ್ತು ಬ್ಯಾಂಕಿಂಗ್ ಸೇವೆಗಳು)\n\n" +
                   "1. **ಪ್ರಧಾನ ಮಂತ್ರಿ ಜನ ಧನ ಯೋಜನೆ (PMJDY)**: ಶೂನ್ಯ-ಬಾಕಿ ಉಳಿತಾಯ ಖಾತೆಗಳು, ₹2 ಲಕ್ಷದ ಅಪಘಾತ ವಿಮೆ ಮತ್ತು ₹10,000 ವರೆಗೆ ಓವರ್‌ಡ್ರಾಫ್ಟ್ ಸೌಲಭ್ಯಗಳನ್ನು ಒದಗಿಸುವ ಹಣಕಾಸು ಒಳಗೊಳ್ಳುವಿಕೆ ಮಿಷನ್.\n\n" +
                   "2. **ಪಿಎಂ ಕಿಸಾನ್ ಕ್ರೆಡಿಟ್ ಕಾರ್ಡ್ (KCC)**: ಸಬ್ಸಿಡಿ ದರದಲ್ಲಿ ರೈತರಿಗೆ ಸುಲಭ ಸಾಲದ ಪ್ರವೇಶ.\n\n" +
                   "3. **ಅಟಲ್ ಪೆನ್ಷನ್ ಯೋಜನೆ (APY)**: 18 ರಿಂದ 40 ವರ್ಷ ವಯಸ್ಸಿನ ಅಸಂಘಟಿತ ವಲಯದ ನಾಗರಿಕರಿಗೆ ಖಾತರಿಪಡಿಸಿದ ಮಾಸಿಕ ಪಿಂಚಣಿ ಯೋಜನೆ.";
        }

        return "### JANA AI ಸ್ಮಾರ್ಟ್ ಸಹಾಯಕಿ\n\n" +
               "ನಮಸ್ಕಾರ! ನಾನು ಜನ ಎಐ, ಸರ್ಕಾರಿ ಸಾಮಾಜಿಕ ಕಾರ್ಯಕ್ರಮಗಳು, ಕಾನೂನು ನೆರವು ಮತ್ತು ಶಿಷ್ಯವೇತನ ಕಾರ್ಯಕ್ರಮಗಳಿಗೆ ನಿಮ್ಮ ವೈಯಕ್ತಿಕ ಮಾರ್ಗದರ್ಶಿ. \n\n" +
               "ಯೋಜನೆಗಳ ಬಗ್ಗೆ ನಿರ್ದಿಷ್ಟ ಮಾಹಿತಿಯನ್ನು ಹುಡುಕಲು ನಾನು ನಿಮಗೆ ಸಹಾಯ ಮಾಡಬಲ್ಲೆ:\n" +
               "- **ಪ್ರಧಾನ ಮಂತ್ರಿ ಜನ ಧನ ಯೋಜನೆ (PMJDY)** (ಹಣಕಾಸು ಒಳಗೊಳ್ಳುವಿಕೆ)\n" +
               "- **ಆಯುಷ್ಮಾನ್ ಭಾರತ್ PM-JAY** (ಆರೋಗ್ಯ ರಕ್ಷಣೆ)\n" +
               "- **ಪಿಎಂ ಕಿಸಾನ್ ಸಮ್ಮಾನ್ ನಿಧಿ** (ಕೃಷಿ)\n" +
               "- **ಲಾಡೋ ಪ್ರೋತ್ಸಾಹ ಯೋಜನೆ** (ರಾಜಸ್ಥಾನ ರಾಜ್ಯದ ಮಹಿಳಾ ಕಲ್ಯಾಣ)\n" +
               "- **ಮೆಟ್ರಿಕ್ ನಂತರದ ಶಿಷ್ಯವೇತನಗಳು** (ಶಿಕ್ಷಣ)\n\n" +
               "ಪ್ರಶ್ನೆಗಳನ್ನು ಕೇಳಲು ಮುಕ್ತವಾಗಿರಿ: *'ಮೆಟ್ರಿಕ್ ನಂತರದ ಶಿಷ್ಯವೇತನಗಳು ಯಾವುವು?'* ಅಥವಾ *'ಮಹಿಳಾ ಸಬಲೀಕರಣ ಯೋಜನೆಗಳನ್ನು ತೋರಿಸಿ'*.";
    }

    private String getSimulatedResponseHi(String message) {
        String msgLower = message.toLowerCase();
        if (msgLower.contains("women") || msgLower.contains("gender") || msgLower.contains("female") || msgLower.contains("girl")) {
            return "### JANA AI ऑफ़लाइन सहायता (महिला सशक्तिकरण योजनाएं)\n\n" +
                   "महिला सशक्तिकरण और शिक्षा पर केंद्रित कुछ योजनाएं यहां दी गई हैं:\n\n" +
                   "1. **लाडो प्रोत्साहन योजना (राजस्थान)**: कमजोर वर्गों की छात्राओं को शिक्षा पूरी करने पर बचत बांड में ₹1,00,000 तक की वित्तीय सहायता।\n\n" +
                   "2. **प्रधानमंत्री मातृ वंदना योजना**: गर्भवती/स्तनपान कराने वाली माताओं के लिए वेतन हानि की भरपाई के लिए प्रत्यक्ष लाभ हस्तांतरण।\n\n" +
                   "3. **महिलाओं के लिए प्रशिक्षण और रोजगार सहायता कार्यक्रम (STEP)**: रोजगार/उद्यमशीलता का समर्थन करने के लिए कौशल प्रदान करता है।\n\n" +
                   "सभी उपलब्ध योजनाओं को देखने के लिए, कृपया **सरकारी योजनाएं** टैब देखें!";
        } else if (msgLower.contains("farm") || msgLower.contains("subsidy") || msgLower.contains("agriculture") || msgLower.contains("kisan") || msgLower.contains("land")) {
            return "### JANA AI ऑफ़लाइन सहायता (कृषि और कृषि सब्सिडी)\n\n" +
                   "सक्रिय कृषि पहल यहां दी गई हैं:\n\n" +
                   "1. **पीएम किसान सम्मान निधि**: छोटे और सीमांत किसान परिवारों को प्रति वर्ष ₹6,000 की प्रत्यक्ष आय सहायता।\n\n" +
                   "2. **प्रधानमंत्री फसल बीमा योजना (PMFBY)**: प्राकृतिक आपदाओं से किसानों की रक्षा के लिए फसल बीमा योजना।\n\n" +
                   "3. **कृषि मशीनरी पर सब्सिडी**: ट्रैक्टर, ड्रिप सिंचाई और कटाई उपकरणों पर राज्य स्तरीय सब्सिडी।\n\n" +
                   "कृषि योजनाओं को खोजने और फ़िल्टर करने के लिए, **सरकारी योजनाएं** टैब पर जाएं!";
        } else if (msgLower.contains("scholarship") || msgLower.contains("college") || msgLower.contains("school") || msgLower.contains("matric") || msgLower.contains("postgraduate") || msgLower.contains("undergraduate") || msgLower.contains("grant")) {
            return "### JANA AI ऑफ़लाइन सहायता (छात्रवृत्ति सेवाएं)\n\n" +
                   "हमें अपने डेटाबेस में कई छात्रवृत्ति योजनाएं मिली हैं:\n\n" +
                   "1. **एससी छात्रों के लिए पोस्ट मैट्रिक छात्रवृत्ति योजना**: व्यावसायिक स्नातक/स्नातकोत्तर डिग्री प्राप्त करने वाले अनुसूचित जाति के छात्रों के लिए 100% शिक्षण शुल्क प्रतिपूर्ति।\n\n" +
                   "2. **नेशनल मीन्स कम मेरिट स्कॉलरशिप**: ₹3.5 लाख से कम पारिवारिक आय वाले कक्षा 9 से 12 के छात्रों के लिए ₹12,000 प्रति वर्ष का योग्यता-आधारित अनुदान।\n\n" +
                   "3. **छात्रवृत्ति की केंद्रीय क्षेत्र योजना**: कॉलेज/विश्वविद्यालय के छात्रों को उनकी कक्षा 12 की बोर्ड परीक्षाओं में शीर्ष पर रहने वालों को ₹20,000 प्रति वर्ष तक की सहायता प्रदान करती है।\n\n" +
                   "पूर्ण पात्रता मानदंडों के लिए **छात्रवृत्ति** पृष्ठ देखें!";
        } else if (msgLower.contains("jandhan") || msgLower.contains("jan dhan") || msgLower.contains("bank") || msgLower.contains("finance") || msgLower.contains("card") || msgLower.contains("pmjdy") || msgLower.contains("account")) {
            return "### JANA AI ऑफ़लाइन सहायता (वित्तीय और बैंकिंग सेवाएं)\n\n" +
                   "1. **प्रधानमंत्री जन धन योजना (PMJDY)**: शून्य-शेष बचत खाते, ₹2 लाख का दुर्घटना बीमा और ₹10,000 तक की ओवरड्राफ्ट सुविधा प्रदान करने वाला वित्तीय समावेशन मिशन।\n\n" +
                   "2. **पीएम किसान क्रेडिट कार्ड (KCC)**: रियायती दरों पर किसानों को आसान ऋण की सुविधा।\n\n" +
                   "3. **अटल पेंशन योजना (APY)**: 18 से 40 वर्ष की आयु के असंगठित क्षेत्र के नागरिकों के लिए गारंटीकृत मासिक पेंशन योजना।";
        }

        return "### JANA AI स्मार्ट सहायक\n\n" +
               "नमस्ते! मैं JANA AI हूँ, सरकारी सामाजिक कार्यक्रमों, कानूनी सहायता और छात्रवृत्ति कार्यक्रमों के लिए आपका व्यक्तिगत मार्गदर्शक। \n\n" +
               "मैं आपको विशिष्ट योजनाओं की जानकारी खोजने में मदद कर सकता हूँ जैसे:\n" +
               "- **प्रधानमंत्री जन धन योजना (PMJDY)** (वित्तीय समावेशन)\n" +
               "- **आयुष्मान भारत PM-JAY** (स्वास्थ्य सेवा)\n" +
               "- **पीएम किसान सम्मान निधि** (कृषि)\n" +
               "- **लाडो प्रोत्साहन योजना** (राजस्थान राज्य महिला कल्याण)\n" +
               "- **पोस्ट-मैट्रिक छात्रवृत्तियां** (शिक्षा)\n\n" +
               "बेझिझक प्रश्न पूछें जैसे: *'पोस्ट-मैट्रिक छात्रवृत्तियां क्या हैं?'* या *'महिला सशक्तिकरण योजनाएं दिखाएं'*।";
    }

    @Override
    public List<ChatConversationResponse> getUserConversations(User user) {
        List<ChatMessage> userMessages = chatMessageRepository.findByUserId(user.getId());
        if (userMessages == null || userMessages.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<ChatMessage>> grouped = userMessages.stream()
                .filter(chat -> chat.getConversationId() != null)
                .collect(Collectors.groupingBy(ChatMessage::getConversationId));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String conversationId = entry.getKey();
                    List<ChatMessage> msgs = entry.getValue();

                    msgs.sort(Comparator.comparing(ChatMessage::getCreatedAt));

                    String title = msgs.get(0).getUserMessage();
                    if (title.length() > 40) {
                        title = title.substring(0, 37) + "...";
                    }

                    ChatMessage lastMsg = msgs.get(msgs.size() - 1);
                    String preview = lastMsg.getAiResponse();
                    if (preview.length() > 60) {
                        preview = preview.substring(0, 57) + "...";
                    }

                    return ChatConversationResponse.builder()
                            .conversationId(conversationId)
                            .title(title)
                            .lastMessagePreview(preview)
                            .createdAt(lastMsg.getCreatedAt())
                            .build();
                })
                .sorted(Comparator.comparing(ChatConversationResponse::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<ChatMessage> getConversationHistory(User user, String conversationId) {
        return chatMessageRepository.findByConversationId(conversationId);
    }

    @Override
    public void deleteConversation(User user, String conversationId) {
        List<ChatMessage> chats = chatMessageRepository.findByConversationId(conversationId);
        chatMessageRepository.deleteAll(chats);
    }
}