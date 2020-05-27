package ru.ard.vnc.glavsoft.rfb.protocol.state;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedSecurityTypeException;
import ru.ard.vnc.glavsoft.rfb.CapabilityContainer;
import ru.ard.vnc.glavsoft.rfb.protocol.ProtocolContext;
import ru.ard.vnc.glavsoft.rfb.protocol.auth.AuthHandler;
import ru.ard.vnc.glavsoft.rfb.protocol.auth.SecurityType;
import ru.ard.vnc.glavsoft.utils.Strings;

public class SecurityTypeState extends ProtocolState {
    public SecurityTypeState(ProtocolContext context) {
        super(context);
    }

    public void negotiateAboutSecurityType() throws TransportException, UnsupportedSecurityTypeException {
        int secTypesNum = this.reader.readUInt8();
        if (secTypesNum == 0) {
            throw new UnsupportedSecurityTypeException("exception at negotiateAboutSecurityType");
        }
        byte[] secTypes = this.reader.readBytes(secTypesNum);
        this.logger.info("Security Types received (" + secTypesNum + "): " + Strings.toString(secTypes));
        AuthHandler typeSelected = selectAuthHandler(secTypes, this.context.getSettings().authCapabilities);
        setUseSecurityResult(typeSelected);
        this.writer.writeByte(typeSelected.getId());
        this.logger.info("Security Type accepted: " + typeSelected.getName());
        changeStateTo(new AuthenticationState(this.context, typeSelected));
    }

    public static AuthHandler selectAuthHandler(byte[] secTypes, CapabilityContainer authCapabilities) throws UnsupportedSecurityTypeException {
        AuthHandler typeSelected;
        for (byte type : secTypes) {
            if (SecurityType.TIGHT_AUTHENTICATION.getId() == (type & 255) && (typeSelected = SecurityType.implementedSecurityTypes.get(Integer.valueOf(SecurityType.TIGHT_AUTHENTICATION.getId()))) != null) {
                return typeSelected;
            }
        }
        for (byte type2 : secTypes) {
            AuthHandler typeSelected2 = SecurityType.implementedSecurityTypes.get(Integer.valueOf(type2 & 255));
            if (typeSelected2 != null && authCapabilities.isSupported(typeSelected2.getId())) {
                return typeSelected2;
            }
        }
        throw new UnsupportedSecurityTypeException("No security types supported. Server sent '" + Strings.toString(secTypes) + "' security types, but we do not support any of their.");
    }

    /* access modifiers changed from: protected */
    public void setUseSecurityResult(AuthHandler typeSelected) {
    }
}
