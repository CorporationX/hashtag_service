package school.faang.hashtagservice.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Hashtag service API",
                description = "API для хранения хэш-тегов и их интеграции с постами"
        ),
        servers = {
                @Server(
                        url = "http://localhost:8090",
                        description = "Локальный сервер"
                )
        }
)
public class OpenApiConfig {
}
