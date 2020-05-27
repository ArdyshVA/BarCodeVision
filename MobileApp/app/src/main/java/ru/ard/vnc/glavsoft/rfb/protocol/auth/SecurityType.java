package ru.ard.vnc.glavsoft.rfb.protocol.auth;

import ru.ard.vnc.glavsoft.exceptions.UnsupportedSecurityTypeException;
import java.util.LinkedHashMap;
import java.util.Map;

public enum SecurityType {
    NONE_AUTHENTICATION(1),
    VNC_AUTHENTICATION(2),
    TIGHT_AUTHENTICATION(16);
    
    public static Map<Integer, AuthHandler> implementedSecurityTypes = new LinkedHashMap<Integer, AuthHandler>() {
        /* class com.glavsoft.rfb.protocol.auth.SecurityType.AnonymousClass1 */

        {
            put(Integer.valueOf(SecurityType.TIGHT_AUTHENTICATION.getId()), new TightAuthentication());
            put(Integer.valueOf(SecurityType.VNC_AUTHENTICATION.getId()), new VncAuthentication());
            put(Integer.valueOf(SecurityType.NONE_AUTHENTICATION.getId()), new NoneAuthentication());
        }
    };
    private int id;

    private SecurityType(int id2) {
        this.id = id2;
    }

    public int getId() {
        return this.id;
    }

    public static AuthHandler getAuthHandlerById(int id2) throws UnsupportedSecurityTypeException {
        AuthHandler typeSelected = implementedSecurityTypes.get(Integer.valueOf(id2));
        if (typeSelected != null) {
            return typeSelected;
        }
        throw new UnsupportedSecurityTypeException("Not supported: " + id2);
    }
}
