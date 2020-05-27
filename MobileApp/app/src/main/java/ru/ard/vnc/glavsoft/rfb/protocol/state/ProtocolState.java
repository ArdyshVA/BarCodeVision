package ru.ard.vnc.glavsoft.rfb.protocol.state;
import java.util.logging.Logger;

import ru.ard.vnc.glavsoft.exceptions.AuthenticationFailedException;
import ru.ard.vnc.glavsoft.exceptions.FatalException;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedProtocolVersionException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedSecurityTypeException;
import ru.ard.vnc.glavsoft.rfb.encoding.ServerInitMessage;
import ru.ard.vnc.glavsoft.rfb.protocol.ProtocolContext;
import ru.ard.vnc.glavsoft.transport.Transport;

public abstract class ProtocolState {
    protected ProtocolContext context;
    protected Logger logger;
    protected Transport.Reader reader;
    protected Transport.Writer writer;

    public ProtocolState(ProtocolContext context2) {
        this.context = context2;
        this.logger = context2.getLogger();
        this.reader = context2.getReader();
        this.writer = context2.getWriter();
    }

    /* access modifiers changed from: protected */
    public void changeStateTo(ProtocolState state) {
        this.context.changeStateTo(state);
    }

    public void handshake() throws TransportException, UnsupportedProtocolVersionException {
        throw new IllegalStateException();
    }

    public void negotiateAboutSecurityType() throws TransportException, UnsupportedSecurityTypeException {
        throw new IllegalStateException();
    }

    public void authenticate() throws TransportException, AuthenticationFailedException, FatalException, UnsupportedSecurityTypeException {
        throw new IllegalStateException();
    }

    public ServerInitMessage clientAndServerInit() throws TransportException {
        throw new IllegalStateException();
    }
}
