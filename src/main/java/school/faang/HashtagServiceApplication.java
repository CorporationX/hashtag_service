package school.faang;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class HashtagServiceApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HashtagServiceApplication.class)
                .run(args);
    }
}