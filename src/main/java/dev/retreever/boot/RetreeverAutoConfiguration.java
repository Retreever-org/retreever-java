package dev.retreever.boot;

import dev.retreever.engine.RetreeverOrchestrator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configures all Retreever components using component scanning.
 */
@AutoConfiguration
@ComponentScan(basePackages = "dev.retreever")
public class RetreeverAutoConfiguration {

    @Bean
    public RetreeverOrchestrator orchestrator() {
        return new RetreeverOrchestrator();
    }
}
