package ru.ard.vnc.glavsoft.rfb.protocol.auth;

import ru.ard.vnc.glavsoft.exceptions.FatalException;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedSecurityTypeException;
import ru.ard.vnc.glavsoft.rfb.CapabilityContainer;
import ru.ard.vnc.glavsoft.rfb.RfbCapabilityInfo;
import ru.ard.vnc.glavsoft.rfb.protocol.state.SecurityTypeState;
import ru.ard.vnc.glavsoft.transport.Transport;
import java.util.logging.Logger;

public class TightAuthentication extends AuthHandler {
    public SecurityType getType() {
        return SecurityType.TIGHT_AUTHENTICATION;
    }

    public boolean authenticate(Transport.Reader reader, Transport.Writer writer, CapabilityContainer authCaps, String passwordRetriever) throws TransportException, FatalException, UnsupportedSecurityTypeException {
        initTunnelling(reader, writer);
        initAuthorization(reader, writer, authCaps, passwordRetriever);
        return true;
    }

    private void initTunnelling(Transport.Reader reader, Transport.Writer writer) throws TransportException {
        long tunnelsCount = reader.readUInt32();
        if (tunnelsCount > 0) {
            for (int i = 0; ((long) i) < tunnelsCount; i++) {
                Logger.getLogger("com.glavsoft.rfb.protocol.auth").fine(new RfbCapabilityInfo(reader).toString());
            }
            writer.writeInt32(0);
        }
    }

    private void initAuthorization(Transport.Reader reader, Transport.Writer writer, CapabilityContainer authCaps, String passwordRetriever) throws UnsupportedSecurityTypeException, TransportException, FatalException {
        AuthHandler authHandler;
        int authCount = reader.readInt32();
        byte[] cap = new byte[authCount];
        for (int i = 0; i < authCount; i++) {
            RfbCapabilityInfo rfbCapabilityInfo = new RfbCapabilityInfo(reader);
            cap[i] = (byte) rfbCapabilityInfo.getCode();
            Logger.getLogger("com.glavsoft.rfb.protocol.auth").fine(rfbCapabilityInfo.toString());
        }
        if (authCount > 0) {
            authHandler = SecurityTypeState.selectAuthHandler(cap, authCaps);
            int i2 = 0;
            while (true) {
                if (i2 >= authCount) {
                    break;
                } else if (authCaps.isSupported(cap[i2])) {
                    writer.writeInt32(cap[i2]);
                    break;
                } else {
                    i2++;
                }
            }
        } else {
            authHandler = SecurityType.getAuthHandlerById(SecurityType.NONE_AUTHENTICATION.getId());
        }
        Logger.getLogger("com.glavsoft.rfb.protocol.auth").info("Auth capability accepted: " + super.getName());
//        super.authenticate(reader, writer, authCaps, passwordRetriever);
    }
}
