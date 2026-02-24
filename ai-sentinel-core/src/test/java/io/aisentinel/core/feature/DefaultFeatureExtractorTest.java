package io.aisentinel.core.feature;

import io.aisentinel.core.model.RequestContext;
import io.aisentinel.core.model.RequestFeatures;
import io.aisentinel.core.store.BaselineStore;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DefaultFeatureExtractorTest {

    private DefaultFeatureExtractor extractor;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        BaselineStore store = new BaselineStore(Duration.ofMinutes(1), 1000);
        extractor = new DefaultFeatureExtractor(store);
        request = mock(HttpServletRequest.class);
    }

    @Test
    void extractReturnsFeatures() {
        when(request.getRequestURI()).thenReturn("/api/hello");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getHeader("Content-Length")).thenReturn(null);

        RequestFeatures f = extractor.extract(request, "hash123", new RequestContext());

        assertThat(f.identityHash()).isEqualTo("hash123");
        assertThat(f.endpoint()).isEqualTo("/api/hello");
        assertThat(f.parameterCount()).isEqualTo(0);
        assertThat(f.toArray()).hasSize(7);
    }
}
