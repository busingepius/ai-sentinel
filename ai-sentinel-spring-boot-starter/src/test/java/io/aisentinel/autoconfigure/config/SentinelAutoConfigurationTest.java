package io.aisentinel.autoconfigure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import io.aisentinel.autoconfigure.web.SentinelFilter;

import static org.assertj.core.api.Assertions.assertThat;

class SentinelAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SentinelAutoConfiguration.class));

    @Test
    void autoConfigurationCreatesSentinelBeansWhenEnabled() {
        contextRunner
            .withPropertyValues("ai.sentinel.enabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(io.aisentinel.core.SentinelPipeline.class);
                assertThat(context).hasSingleBean(SentinelFilter.class);
            });
    }

    @Test
    void propertiesAreAvailable() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(SentinelProperties.class));
    }
}
