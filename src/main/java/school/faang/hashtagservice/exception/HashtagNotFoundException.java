package school.faang.hashtagservice.exception;

public class HashtagNotFoundException extends RuntimeException {

    public HashtagNotFoundException(String message) {
        super(message);
    }

    public HashtagNotFoundException(String message, Object... args) {
        super(String.format(message, args));
    }
}
