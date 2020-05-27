package ru.ard.vnc.glavsoft.exceptions;

public class ClosedConnectionException extends TransportException {
    public ClosedConnectionException(Throwable exception) {
        super(exception);
    }
}
