package ru.ard.vnc.glavsoft.exceptions;

public class CryptoException extends FatalException {
    public CryptoException(String message, Throwable exception) {
        super(message, exception);
    }
}
