package com.bathforge.service.ai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Base64;

/**
 * Service for handling OpenAI API interactions
 */
@Service
public class OpenAIPromptService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIPromptService.class);

    private final OpenAiService openAiService;

    @Value("${openai.api.model:gpt-5-nano}")
    private String openaiModel;

    @Autowired
    public OpenAIPromptService(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    /**
     * Send prompt to OpenAI and get response
     */
    public String generateDesignFromPrompt(String prompt) {
        logger.info("Sending prompt to OpenAI: {}", prompt.substring(0, Math.min(100, prompt.length())));

        try {
            List<ChatMessage> messages = Arrays.asList(
                    new ChatMessage("system",
                            "You are an expert bathroom designer with extensive knowledge of interior design, "
                                    + "3D spatial planning, and product recommendations. Focus on creating practical, "
                                    + "aesthetic, and functional bathroom layouts that consider room dimensions, "
                                    + "product placement, color harmony, and user requirements."),
                    new ChatMessage("user", prompt));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(openaiModel)
                    .messages(messages)
                    .build();

            String response = openAiService.createChatCompletion(request)
                    .getChoices().get(0).getMessage().getContent();

            logger.info("Received OpenAI response with {} characters", response.length());
            return response;

        } catch (Exception e) {
            logger.error("Error calling OpenAI API: ", e);
            throw new RuntimeException("Failed to generate design from OpenAI: " + e.getMessage(), e);
        }
    }

    /**
     * Load image from URL or local path and convert to base64
     */
    @SuppressWarnings("deprecation")
    public String loadImageAsBase64(String imagePath) throws IOException {
        logger.debug("Loading image from path: {}", imagePath);

        try {
            InputStream inputStream;

            // Check if it's a URL (starts with http:// or https://)
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                URL url = new URL(imagePath);
                inputStream = url.openStream();
            }
            // Check if it's an assets path (frontend resources)
            else if (imagePath.startsWith("/assets") || imagePath.startsWith("assets")) {
                // Try to construct path to frontend public folder
                // Assuming backend and frontend are sibling directories
                Path currentPath = Paths.get("").toAbsolutePath();
                Path frontendPublicPath = currentPath.getParent().resolve("frontend").resolve("public");

                String assetPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
                Path fullPath = frontendPublicPath.resolve(assetPath);

                logger.debug("Attempting to load from frontend path: {}", fullPath);

                if (Files.exists(fullPath)) {
                    inputStream = Files.newInputStream(fullPath);
                } else {
                    // Try relative to current directory (in case structure is different)
                    Path relativePath = Paths.get("../frontend/public").resolve(assetPath);
                    if (Files.exists(relativePath)) {
                        inputStream = Files.newInputStream(relativePath);
                    } else {
                        throw new IOException("Image file not found at: " + fullPath + " or " + relativePath);
                    }
                }
            }
            // Otherwise treat as file system path
            else {
                Path path = Paths.get(imagePath);
                inputStream = Files.newInputStream(path);
            }

            try (inputStream) {
                byte[] imageBytes = inputStream.readAllBytes();
                String base64 = Base64.getEncoder().encodeToString(imageBytes);
                logger.debug("Successfully loaded and encoded image ({} bytes)", imageBytes.length);
                return base64;
            }
        } catch (IOException e) {
            logger.error("Failed to load image from path: {}", imagePath, e);
            throw e;
        }
    }
}