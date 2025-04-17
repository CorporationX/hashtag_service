package school.faang.hashtagservice.exception;

public class KafkaConfigException extends RuntimeException {

    public KafkaConfigException(String message) {
        super(message);
    }

    public KafkaConfigException(String message, Object... args) {
      super(String.format(message, args));
    }
}
