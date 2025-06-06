package lii.buildmaster.projecttracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI buildMasterProjectTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BuildMaster Project Tracker API")
                        .description("A comprehensive project management system for tracking projects, developers, and tasks with advanced features like audit logging and analytics.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("BuildMaster Development Team")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server")))
                .tags(List.of(
                        new Tag()
                                .name("Projects")
                                .description("Project management operations"),
                        new Tag()
                                .name("Developers")
                                .description("Developer management operations"),
                        new Tag()
                                .name("Tasks")
                                .description("Task management and assignment operations"),
                        new Tag()
                                .name("Health")
                                .description("Application health and status endpoints")));
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats());

        cacheManager.setCacheNames(List.of(
                "projects",
                "developers",
                "tasks",
                "projectStats",
                "developerStats",
                "taskStats"
        ));

        return cacheManager;
    }
}
