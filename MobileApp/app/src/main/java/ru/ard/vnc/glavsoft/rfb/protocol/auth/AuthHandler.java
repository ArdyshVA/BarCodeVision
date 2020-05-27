package ru.ard.vnc.glavsoft.rfb.protocol.auth;

import ru.ard.vnc.glavsoft.exceptions.FatalException;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedSecurityTypeException;
import ru.ard.vnc.glavsoft.rfb.CapabilityContainer;
import ru.ard.vnc.glavsoft.transport.Transport;

public abstract class AuthHandler {
    protected boolean useSecurityResult = true;

    public abstract boolean authenticate(Transport.Reader reader, Transport.Writer writer, CapabilityContainer capabilityContainer, String str) throws TransportException, FatalException, UnsupportedSecurityTypeException;

    public abstract SecurityType getType();

    public int getId() {
        return getType().getId();
    }

    public String getName() {
        return getType().name();
    }

    public boolean useSecurityResult() {
        return this.useSecurityResult;
    }

    public void setUseSecurityResult(boolean enabled) {
        this.useSecurityResult = enabled;
    }
}
