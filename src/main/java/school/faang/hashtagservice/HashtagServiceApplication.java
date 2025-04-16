package school.faang.hashtagservice;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class HashtagServiceApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HashtagServiceApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }
}
