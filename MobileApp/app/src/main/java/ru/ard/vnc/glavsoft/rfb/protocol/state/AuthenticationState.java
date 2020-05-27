package ru.ard.vnc.glavsoft.rfb.protocol.state;

import ru.ard.vnc.glavsoft.exceptions.AuthenticationFailedException;
import ru.ard.vnc.glavsoft.exceptions.FatalException;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedSecurityTypeException;
import ru.ard.vnc.glavsoft.rfb.protocol.ProtocolContext;
import ru.ard.vnc.glavsoft.rfb.protocol.auth.AuthHandler;

public class AuthenticationState extends ProtocolState {
    private static final int AUTH_RESULT_OK = 0;
    private final AuthHandler authHandler;

    public AuthenticationState(ProtocolContext context, AuthHandler authHandler2) {
        super(context);
        this.authHandler = authHandler2;
    }

    public void authenticate() throws TransportException, AuthenticationFailedException, FatalException, UnsupportedSecurityTypeException {
        boolean isTight = this.authHandler.authenticate(this.reader, this.writer, this.context.getSettings().authCapabilities, this.context.getPasswordRetriever());
        if (this.authHandler.useSecurityResult()) {
            checkSecurityResult();
        }
        changeStateTo(isTight ? new InitTightState(this.context) : new InitState(this.context));
        this.context.getSettings().setTight(isTight);
    }

    /* access modifiers changed from: protected */
    public void checkSecurityResult() throws TransportException, AuthenticationFailedException {
        if (this.reader.readInt32() != 0) {
            try {
                throw new AuthenticationFailedException(this.reader.readString());
            } catch (Exception e) {
                throw new AuthenticationFailedException("Authentication failed");
            }
        }
    }
}
