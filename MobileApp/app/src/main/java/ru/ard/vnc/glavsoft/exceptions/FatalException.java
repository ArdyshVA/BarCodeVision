package ru.ard.vnc.glavsoft.exceptions;

public class FatalException extends CommonException {
    public FatalException(String message, Throwable e) {
        super(message, e);
    }
}
