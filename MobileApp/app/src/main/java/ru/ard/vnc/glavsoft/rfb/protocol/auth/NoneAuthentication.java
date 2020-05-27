package ru.ard.vnc.glavsoft.rfb.protocol.auth;

import ru.ard.vnc.glavsoft.rfb.CapabilityContainer;
import ru.ard.vnc.glavsoft.transport.Transport;

public class NoneAuthentication extends AuthHandler {
    public boolean authenticate(Transport.Reader reader, Transport.Writer writer, CapabilityContainer authCaps, String passwordRetriever) {
        return false;
    }

    public SecurityType getType() {
        return SecurityType.NONE_AUTHENTICATION;
    }
}
