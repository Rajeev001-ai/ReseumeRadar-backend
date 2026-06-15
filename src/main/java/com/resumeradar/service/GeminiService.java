package com.resumeradar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeminiService.class);

	public static final String MISSING_API_KEY_MESSAGE =
		"Gemini API key is missing. Please set GEMINI_API_KEY environment variable.";

	public static final String EMPTY_RESPONSE_MESSAGE = "Gemini returned empty response.";

	public static final String MODEL_UNAVAILABLE_MESSAGE =
		"Gemini model is unavailable. Please check gemini.model in application.properties.";

	public static final String INVALID_API_KEY_MESSAGE =
		"Gemini API key is invalid or Gemini API is not enabled for this key.";

	public static final String RATE_LIMIT_MESSAGE =
		"Gemini quota exceeded or rate limited. Please try again later.";

	private static final String FALLBACK_MODEL = "gemini-flash-latest";

	private final String geminiApiKey;

	private final String geminiApiUrl;

	private final String geminiModel;

	private final ObjectMapper objectMapper;

	private final HttpClient httpClient;

	public GeminiService(
		@Value("${gemini.api.key:}") String geminiApiKey,
		@Value("${gemini.api.url}") String geminiApiUrl,
		@Value("${gemini.model:gemini-1.5-flash-latest}") String geminiModel,
		ObjectMapper objectMapper
	) {
		this.geminiApiKey = geminiApiKey;
		this.geminiApiUrl = geminiApiUrl;
		this.geminiModel = geminiModel;
		this.objectMapper = objectMapper;
		this.httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();
	}

	public String generateContent(
		String prompt,
		String fallbackMessage,
		String operationName,
		Duration timeout
	) {
		if (geminiApiKey == null || geminiApiKey.isBlank()) {
			LOGGER.warn("{} failed: Gemini API key is missing", operationName);
			return MISSING_API_KEY_MESSAGE;
		}

		try {
			GeminiCallResult result = callGemini(geminiModel, prompt, operationName, timeout);
			if (result.statusCode() == 404 && !FALLBACK_MODEL.equals(geminiModel)) {
				LOGGER.warn(
					"{} received 404 for model={}. Retrying with model={}",
					operationName,
					geminiModel,
					FALLBACK_MODEL
				);
				result = callGemini(FALLBACK_MODEL, prompt, operationName, timeout);
			}

			if (result.statusCode() < 200 || result.statusCode() >= 300) {
				LOGGER.warn(
					"{} failed with Gemini HTTP status={} body={}",
					operationName,
					result.statusCode(),
					result.responseBody()
				);
				return resolveFailureMessage(result.statusCode(), fallbackMessage);
			}

			String generatedText = extractText(result.responseBody());
			if (generatedText.isBlank()) {
				LOGGER.warn("{} failed: Gemini returned empty response", operationName);
				return EMPTY_RESPONSE_MESSAGE;
			}

			LOGGER.info("{} Gemini response parsed successfully", operationName);
			return generatedText;
		} catch (Exception exception) {
			LOGGER.warn("{} Gemini request failed: {}", operationName, exception.getMessage(), exception);
			return fallbackMessage;
		}
	}

	private GeminiCallResult callGemini(
		String model,
		String prompt,
		String operationName,
		Duration timeout
	) throws Exception {
		String requestBody = objectMapper.writeValueAsString(createRequestBody(prompt));
		URI requestUri = createGeminiUri(model, true);
		LOGGER.info("{} Gemini request URL={}", operationName, createGeminiUri(model, false));

		HttpRequest request = HttpRequest.newBuilder()
			.uri(requestUri)
			.timeout(timeout)
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(requestBody))
			.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		LOGGER.info("{} Gemini HTTP status={}", operationName, response.statusCode());

		return new GeminiCallResult(response.statusCode(), response.body());
	}

	private String resolveFailureMessage(int statusCode, String fallbackMessage) {
		return switch (statusCode) {
			case 403 -> INVALID_API_KEY_MESSAGE;
			case 404 -> MODEL_UNAVAILABLE_MESSAGE;
			case 429 -> RATE_LIMIT_MESSAGE;
			default -> fallbackMessage;
		};
	}

	private URI createGeminiUri(String model, boolean includeApiKey) {
		String baseUrl = geminiApiUrl == null ? "" : geminiApiUrl.trim().replaceAll("/+$", "");
		if (baseUrl.contains(":generateContent")) {
			int modelsIndex = baseUrl.lastIndexOf("/models");
			if (modelsIndex >= 0) {
				baseUrl = baseUrl.substring(0, modelsIndex + "/models".length());
			}
		}
		String endpoint = baseUrl.endsWith("/models")
			? baseUrl + "/" + model + ":generateContent"
			: baseUrl + "/models/" + model + ":generateContent";
		String apiKeyValue = includeApiKey
			? URLEncoder.encode(geminiApiKey, StandardCharsets.UTF_8)
			: "REDACTED";

		return URI.create(endpoint + "?key=" + apiKeyValue);
	}

	private Map<String, Object> createRequestBody(String prompt) {
		return Map.of(
			"contents",
			List.of(Map.of("parts", List.of(Map.of("text", prompt))))
		);
	}

	private String extractText(String responseBody) throws Exception {
		JsonNode root = objectMapper.readTree(responseBody);
		JsonNode textNode = root.path("candidates")
			.path(0)
			.path("content")
			.path("parts")
			.path(0)
			.path("text");

		if (textNode.isMissingNode()) {
			return "";
		}

		return textNode.asText("").trim();
	}

	private record GeminiCallResult(int statusCode, String responseBody) {
	}
}
