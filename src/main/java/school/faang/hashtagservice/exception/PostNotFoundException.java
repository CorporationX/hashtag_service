package school.faang.hashtagservice.exception;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(String message, Object... args) {
        super(String.format(message, args));
    }
}
