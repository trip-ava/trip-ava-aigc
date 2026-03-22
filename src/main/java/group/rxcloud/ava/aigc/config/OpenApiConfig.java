package group.rxcloud.ava.aigc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration
 * Access documentation at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI avaOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("AVA AI Travel Guide API")
                        .description("""
                                AVA (AI Virtual Assistant) is an AI-powered digital human travel guide.

                                ## Features
                                - **Chat**: Text-based conversation with AVA AI
                                - **Voice**: Speech-to-text and text-to-speech capabilities
                                - **Authentication**: JWT-based user authentication
                                - **Chat History**: Persistent conversation history
                                - **Monitoring**: System health and performance metrics

                                ## Authentication
                                Most endpoints require Bearer token authentication.
                                Use `/api/auth/login` to obtain tokens.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AVA Team")
                                .email("team@ava-travel.ai"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development"),
                        new Server().url("https://api.ava-travel.ai").description("Production")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token")));
    }
}
