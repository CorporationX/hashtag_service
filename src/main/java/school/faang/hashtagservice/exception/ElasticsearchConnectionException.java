package school.faang.hashtagservice.exception;

public class ElasticsearchConnectionException extends RuntimeException {

    public ElasticsearchConnectionException(String message, Object... args) {
        super(String.format(message, args));
    }
}
