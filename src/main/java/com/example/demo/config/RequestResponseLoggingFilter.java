package com.example.demo.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    private static final int MAX_LOG_BODY_BYTES = 16 * 1024;

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password",
            "passwordHash",
            "currentPassword",
            "newPassword",
            "token",
            "accessToken",
            "refreshToken"
    );

    private final ObjectMapper objectMapper;

    public RequestResponseLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startNs = System.nanoTime();

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String requestId = getOrCreateRequestId(request);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        wrappedResponse.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long durationMs = (System.nanoTime() - startNs) / 1_000_000;

            String method = request.getMethod();
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            String fullPath = query == null ? uri : uri + "?" + query;

            int status = wrappedResponse.getStatus();

            String reqBody = extractBodyForLogging(wrappedRequest.getContentAsByteArray(), wrappedRequest.getContentType());
            String resBody = extractBodyForLogging(wrappedResponse.getContentAsByteArray(), wrappedResponse.getContentType());

            log.info("{} {} -> {} ({} ms) reqBody={} resBody={}", method, fullPath, status, durationMs, reqBody, resBody);

            MDC.remove(REQUEST_ID_MDC_KEY);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private String getOrCreateRequestId(HttpServletRequest request) {
        String incoming = request.getHeader(REQUEST_ID_HEADER);
        if (incoming != null && !incoming.isBlank()) {
            return incoming.trim();
        }
        return UUID.randomUUID().toString();
    }

    private String extractBodyForLogging(byte[] bodyBytes, String contentType) {
        if (bodyBytes == null || bodyBytes.length == 0) {
            return "";
        }

        String ct = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (!ct.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return "";
        }

        int len = Math.min(bodyBytes.length, MAX_LOG_BODY_BYTES);
        String raw = new String(bodyBytes, 0, len, StandardCharsets.UTF_8);
        if (bodyBytes.length > MAX_LOG_BODY_BYTES) {
            raw = raw + "...(truncated)";
        }

        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode redacted = redactJson(root);
            return objectMapper.writeValueAsString(redacted);
        } catch (Exception ignored) {
            return raw;
        }
    }

    private JsonNode redactJson(JsonNode node) {
        if (node == null) {
            return null;
        }

        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node.deepCopy();
            obj.fieldNames().forEachRemaining(name -> {
                JsonNode value = obj.get(name);
                if (isSensitiveField(name)) {
                    obj.put(name, "***");
                } else {
                    obj.set(name, redactJson(value));
                }
            });
            return obj;
        }

        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                ((com.fasterxml.jackson.databind.node.ArrayNode) node).set(i, redactJson(node.get(i)));
            }
            return node;
        }

        return node;
    }

    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        String normalized = fieldName.trim();
        return SENSITIVE_FIELDS.contains(normalized);
    }
}
