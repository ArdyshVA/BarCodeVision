package ru.ard.vnc.glavsoft.exceptions;

public class AuthenticationFailedException extends ProtocolException {
    private String reason;

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, String reason2) {
        super(message);
        this.reason = reason2;
    }

    public String getReason() {
        return this.reason;
    }
}
