package school.faang.hashtagservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import school.faang.hashtagservice.dto.error.ErrorResponse;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
            HashtagNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleExceptionWithStatusNotFound(Exception e) {
        return ResponseEntity.status(NOT_FOUND).body(getErrorResponse(e));
    }

    @ExceptionHandler({
            JsonDeserializationException.class
    })
    public ResponseEntity<ErrorResponse> handleExceptionWithStatusBadRequest(Exception e) {
        return ResponseEntity.status(BAD_REQUEST).body(getErrorResponse(e));
    }

    private ErrorResponse getErrorResponse(Exception e) {
        log.error("{}", e.toString());
        return ErrorResponse.builder()
                .message(e.getMessage())
                .build();
    }
}
