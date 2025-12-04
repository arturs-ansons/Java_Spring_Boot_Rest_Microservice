package org.banking.crypto.exception;

public class TradingNotAllowedException extends RuntimeException {
    public TradingNotAllowedException(String message) {
        super(message);
    }
}
