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
 * Service for handling OpenAI API interactions.
 * Manages prompt generation, chat completion requests, and image encoding for
 * AI-powered features.
 */
@Service
public class OpenAIPromptService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIPromptService.class);

    private final OpenAiService openAiService;

    /** The OpenAI model to use for chat completions */
    @Value("${openai.api.model:gpt-5-nano}")
    private String openaiModel;

    /**
     * Constructs an OpenAIPromptService with the OpenAI service.
     *
     * @param openAiService the OpenAI service for API interactions
     */
    @Autowired
    public OpenAIPromptService(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    /**
     * Sends a prompt to OpenAI and gets the response.
     * Includes a system message defining the AI's role as a bathroom designer.
     *
     * @param prompt the user prompt to send to OpenAI
     * @return the AI-generated response text
     * @throws RuntimeException if the API call fails
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
     * Loads an image from a URL or local path and converts it to base64.
     * Supports HTTP/HTTPS URLs, frontend public assets, and local file paths.
     *
     * @param imagePath the path or URL to the image
     * @return base64-encoded string of the image
     * @throws IOException if the image cannot be loaded or encoded
     */
    @SuppressWarnings("deprecation")
    public String loadImageAsBase64(String imagePath) throws IOException {
        logger.debug("Loading image from path: {}", imagePath);

        try {
            InputStream inputStream;

            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                URL url = new URL(imagePath);
                inputStream = url.openStream();
            } else if (imagePath.startsWith("/assets") || imagePath.startsWith("assets")) {
                Path currentPath = Paths.get("").toAbsolutePath();
                Path frontendPublicPath = currentPath.getParent().resolve("frontend").resolve("public");

                String assetPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
                Path fullPath = frontendPublicPath.resolve(assetPath);

                logger.debug("Attempting to load from frontend path: {}", fullPath);

                if (Files.exists(fullPath)) {
                    inputStream = Files.newInputStream(fullPath);
                } else {
                    Path relativePath = Paths.get("../frontend/public").resolve(assetPath);
                    if (Files.exists(relativePath)) {
                        inputStream = Files.newInputStream(relativePath);
                    } else {
                        throw new IOException("Image file not found at: " + fullPath + " or " + relativePath);
                    }
                }
            } else {
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