package lii.buildmaster.projecttracker.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RoleDataInitializer {

    private final DataInitializationService initializationService;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            initializationService.initialize();
            log.info("✅ Database initialization complete.");
        };
    }
}