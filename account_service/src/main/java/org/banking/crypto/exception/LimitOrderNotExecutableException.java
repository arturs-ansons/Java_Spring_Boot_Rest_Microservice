package org.banking.crypto.exception;

public class LimitOrderNotExecutableException extends RuntimeException {
    public LimitOrderNotExecutableException(String message) {
        super(message);
    }
}
