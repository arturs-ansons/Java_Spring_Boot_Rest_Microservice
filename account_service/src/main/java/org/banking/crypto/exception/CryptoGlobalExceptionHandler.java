package org.banking.crypto.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class CryptoGlobalExceptionHandler {

    private ResponseEntity<Map<String, String>> buildResponse(String message, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler({
            TradingNotAllowedException.class,
            InsufficientFiatBalanceException.class,
            InsufficientCryptoBalanceException.class,
            CryptoNotFoundException.class,
            CryptoPriceException.class,
            LimitOrderNotExecutableException.class
    })
    public ResponseEntity<Map<String, String>> handleCryptoExceptions(RuntimeException ex) {

        HttpStatus status;

        // Customize HTTP status based on exception type
        if (ex instanceof TradingNotAllowedException) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex instanceof InsufficientFiatBalanceException || ex instanceof InsufficientCryptoBalanceException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (ex instanceof CryptoNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof CryptoPriceException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof LimitOrderNotExecutableException) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return buildResponse(ex.getMessage(), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Unexpected crypto error occurred", ex);
        return buildResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
