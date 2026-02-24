package io.aisentinel.core.store;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class BaselineStoreTest {

    @Test
    void incrementAndGetCountsRequests() {
        var store = new BaselineStore(Duration.ofMinutes(1), 1000);
        assertThat(store.incrementAndGet("k1")).isEqualTo(1);
        assertThat(store.incrementAndGet("k1")).isEqualTo(2);
        assertThat(store.incrementAndGet("k2")).isEqualTo(1);
        assertThat(store.get("k1")).isEqualTo(2);
    }
}
