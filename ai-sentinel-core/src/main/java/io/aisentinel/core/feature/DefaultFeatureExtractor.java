package io.aisentinel.core.feature;

import io.aisentinel.core.model.RequestContext;
import io.aisentinel.core.model.RequestFeatures;
import io.aisentinel.core.store.BaselineStore;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default feature extractor using privacy-safe features:
 * requestsPerWindow, endpointEntropy, tokenAgeSeconds, parameterCount,
 * payloadSizeBytes, headerFingerprintHash, ipBucket.
 */
public final class DefaultFeatureExtractor implements FeatureExtractor {

    private final BaselineStore requestCountStore;
    private final Map<String, int[]> endpointHistory;
    private static final int HISTORY_SIZE = 16;

    public DefaultFeatureExtractor(BaselineStore requestCountStore) {
        this.requestCountStore = requestCountStore;
        this.endpointHistory = new ConcurrentHashMap<>();
    }

    @Override
    public RequestFeatures extract(HttpServletRequest request, String identityHash, RequestContext ctx) {
        long now = System.currentTimeMillis();
        String endpoint = normalizeEndpoint(request.getRequestURI());

        int requestsPerWindow = requestCountStore.incrementAndGet(identityHash + "|" + endpoint);
        double endpointEntropy = computeEndpointEntropy(identityHash, endpoint);
        double tokenAgeSeconds = extractTokenAgeSeconds(request);
        int parameterCount = request.getParameterMap().size();
        long payloadSizeBytes = extractPayloadSize(request);
        long headerFingerprintHash = computeHeaderFingerprint(request);
        int ipBucket = extractIpBucket(request);

        return RequestFeatures.builder()
            .identityHash(identityHash)
            .endpoint(endpoint)
            .timestampMillis(now)
            .requestsPerWindow(requestsPerWindow)
            .endpointEntropy(endpointEntropy)
            .tokenAgeSeconds(tokenAgeSeconds)
            .parameterCount(parameterCount)
            .payloadSizeBytes(payloadSizeBytes)
            .headerFingerprintHash(headerFingerprintHash)
            .ipBucket(ipBucket)
            .build();
    }

    private String normalizeEndpoint(String uri) {
        if (uri == null || uri.isEmpty()) return "/";
        return uri.length() > 256 ? uri.substring(0, 256) : uri;
    }

    private double computeEndpointEntropy(String identityHash, String endpoint) {
        int[] history = endpointHistory.computeIfAbsent(identityHash, k -> new int[HISTORY_SIZE]);
        int hash = Math.abs(endpoint.hashCode() % HISTORY_SIZE);
        history[hash]++;
        int total = 0;
        for (int c : history) total += c;
        if (total == 0) return 0;
        double entropy = 0;
        for (int c : history) {
            if (c > 0) {
                double p = (double) c / total;
                entropy -= p * Math.log(p + 1e-10);
            }
        }
        return entropy;
    }

    private double extractTokenAgeSeconds(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null) return -1;
        String issuedStr = request.getHeader("X-Token-Issued-At");
        if (issuedStr == null) return -1;
        try {
            long issued = Long.parseLong(issuedStr.trim()) * 1000L;
            return (System.currentTimeMillis() - issued) / 1000.0;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long extractPayloadSize(HttpServletRequest request) {
        String cl = request.getHeader("Content-Length");
        if (cl == null) return 0;
        try {
            return Long.parseLong(cl.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long computeHeaderFingerprint(HttpServletRequest request) {
        Map<String, String> h = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) return 0;
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name != null && !name.equalsIgnoreCase("Authorization")) {
                String v = request.getHeader(name);
                h.put(name.toLowerCase(), v != null ? Integer.toString(v.length()) : "0");
            }
        }
        return h.hashCode();
    }

    private int extractIpBucket(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (ip == null) return 0;
        if (ip.contains(":")) {
            return ip.hashCode() & 0x7FFF_FFFF;
        }
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            try {
                int a = Integer.parseInt(parts[0]) & 0xFF;
                int b = Integer.parseInt(parts[1]) & 0xFF;
                int c = Integer.parseInt(parts[2]) & 0xFF;
                return (a << 16) | (b << 8) | c;
            } catch (NumberFormatException e) { /* fall through */ }
        }
        return ip.hashCode() & 0x7FFF_FFFF;
    }
}
