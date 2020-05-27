package ru.ard.vnc.glavsoft.rfb.protocol.state;

import ru.ard.vnc.glavsoft.rfb.protocol.ProtocolContext;
import ru.ard.vnc.glavsoft.rfb.protocol.auth.AuthHandler;
import ru.ard.vnc.glavsoft.rfb.protocol.auth.SecurityType;

public class SecurityType37State extends SecurityTypeState {
    public SecurityType37State(ProtocolContext context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void setUseSecurityResult(AuthHandler type) {
        if (SecurityType.NONE_AUTHENTICATION == type.getType()) {
            type.setUseSecurityResult(false);
        }
    }
}
