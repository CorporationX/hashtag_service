package school.faang.hashtag_service;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class HashtagServiceApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(HashtagServiceApp.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }
}