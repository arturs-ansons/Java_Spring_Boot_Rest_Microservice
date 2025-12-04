package org.banking.crypto.exception;

public class InsufficientFiatBalanceException extends RuntimeException {
    public InsufficientFiatBalanceException(String message) {
        super(message);
    }
}
