package io.github.lucasssoh.boussole.server.api;

import io.github.lucasssoh.boussole.core.learn.ContentNotFoundException;
import io.github.lucasssoh.boussole.core.learn.LearningRuleException;
import io.github.lucasssoh.boussole.core.learn.check.ResponseMismatchException;
import io.github.lucasssoh.boussole.core.device.PairingRefusedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns what the use cases refuse into HTTP statuses, with the reason as the message. */
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(ContentNotFoundException.class)
    ResponseEntity<ApiError> notFound(ContentNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(LearningRuleException.class)
    ResponseEntity<ApiError> refused(LearningRuleException e) {
        return error(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(PairingRefusedException.class)
    ResponseEntity<ApiError> pairingRefused(PairingRefusedException e) {
        return error(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(ResponseMismatchException.class)
    ResponseEntity<ApiError> mismatch(ResponseMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, e);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, RuntimeException e) {
        return ResponseEntity.status(status).body(new ApiError(e.getMessage()));
    }
}
