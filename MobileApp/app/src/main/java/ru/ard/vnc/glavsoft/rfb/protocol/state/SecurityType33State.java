package ru.ard.vnc.glavsoft.rfb.protocol.state;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedSecurityTypeException;
import ru.ard.vnc.glavsoft.rfb.protocol.ProtocolContext;
import ru.ard.vnc.glavsoft.rfb.protocol.auth.AuthHandler;

public class SecurityType33State extends SecurityType37State {
    public SecurityType33State(ProtocolContext context) {
        super(context);
    }

    public void negotiateAboutSecurityType() throws TransportException, UnsupportedSecurityTypeException {
        this.logger.info("Get Security Type");
        int type = this.reader.readInt32();
        this.logger.info("Type received: " + type);
        if (type == 0) {
            throw new UnsupportedSecurityTypeException("exception at negotiateAboutSecurityType");
        }
        AuthHandler typeSelected = selectAuthHandler(new byte[]{(byte) (type & 255)}, this.context.getSettings().authCapabilities);
        if (typeSelected != null) {
            setUseSecurityResult(typeSelected);
            this.logger.info("Type accepted: " + typeSelected.getName());
            changeStateTo(new AuthenticationState(this.context, typeSelected));
            return;
        }
        throw new UnsupportedSecurityTypeException("No security types supported. Server sent '" + type + "' security type, but we do not support it.");
    }
}
