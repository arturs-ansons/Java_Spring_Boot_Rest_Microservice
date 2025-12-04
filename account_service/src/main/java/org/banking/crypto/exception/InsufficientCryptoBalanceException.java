package org.banking.crypto.exception;

public class InsufficientCryptoBalanceException extends RuntimeException {
    public InsufficientCryptoBalanceException(String message) {
        super(message);
    }
}
