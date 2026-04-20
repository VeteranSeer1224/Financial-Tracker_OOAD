package com.finance.tracker.frontend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

public class ApiClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String baseUrl;

    public ApiClient(String baseUrl) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public JsonNode get(String path) throws IOException, InterruptedException {
        return sendJson("GET", path, null);
    }

    public JsonNode post(String path, Object body) throws IOException, InterruptedException {
        return sendJson("POST", path, body);
    }

    public JsonNode put(String path, Object body) throws IOException, InterruptedException {
        return sendJson("PUT", path, body);
    }

    public JsonNode patch(String path, Object body) throws IOException, InterruptedException {
        return sendJson("PATCH", path, body);
    }

    public JsonNode delete(String path) throws IOException, InterruptedException {
        return sendJson("DELETE", path, null);
    }

    public byte[] getBytes(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(buildUrl(path)))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        throw new IOException(parseErrorMessage(response.statusCode(), new String(response.body(), StandardCharsets.UTF_8)));
    }

    public String withQuery(String path, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return path;
        }
        StringJoiner joiner = new StringJoiner("&");
        queryParams.forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                joiner.add(URLEncoder.encode(key, StandardCharsets.UTF_8)
                        + "="
                        + URLEncoder.encode(value, StandardCharsets.UTF_8));
            }
        });
        String queryString = joiner.toString();
        if (queryString.isBlank()) {
            return path;
        }
        return path + "?" + queryString;
    }

    public String pretty(JsonNode jsonNode) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    private JsonNode sendJson(String method, String path, Object body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(buildUrl(path))).timeout(Duration.ofSeconds(30));
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            String payload = objectMapper.writeValueAsString(body);
            builder.method(method, HttpRequest.BodyPublishers.ofString(payload))
                    .header("Content-Type", "application/json");
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body() == null ? "" : response.body();
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (responseBody.isBlank()) {
                return objectMapper.createObjectNode().put("message", "Success");
            }
            return objectMapper.readTree(responseBody);
        }
        throw new IOException(parseErrorMessage(response.statusCode(), responseBody));
    }

    private String parseErrorMessage(int statusCode, String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "Request failed with status " + statusCode;
        }
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            if (node.has("error")) {
                return node.get("error").asText() + " (HTTP " + statusCode + ")";
            }
            if (node.has("message")) {
                return node.get("message").asText() + " (HTTP " + statusCode + ")";
            }
        } catch (Exception ignored) {
            // Fall back to raw response body when not JSON.
        }
        return responseBody + " (HTTP " + statusCode + ")";
    }

    private String buildUrl(String path) {
        String sanitizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return sanitizedBase + normalizedPath;
    }
}
