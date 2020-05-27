package ru.ard.vnc.glavsoft.rfb;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.rfb.encoding.EncodingType;
import ru.ard.vnc.glavsoft.transport.Transport;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

public class CapabilityContainer {
    private final Map<Integer, RfbCapabilityInfo> caps = new HashMap();

    public void add(RfbCapabilityInfo capabilityInfo) {
        this.caps.put(Integer.valueOf(capabilityInfo.getCode()), capabilityInfo);
    }

    public void add(int code, String vendor, String name) {
        this.caps.put(Integer.valueOf(code), new RfbCapabilityInfo(code, vendor, name));
    }

    public void addEnabled(int code, String vendor, String name) {
        RfbCapabilityInfo capability = new RfbCapabilityInfo(code, vendor, name);
        capability.setEnable(true);
        this.caps.put(Integer.valueOf(code), capability);
    }

    public void setEnable(int id, boolean enable) {
        RfbCapabilityInfo c = this.caps.get(Integer.valueOf(id));
        if (c != null) {
            c.setEnable(enable);
        }
    }

    public void setAllEnable(boolean enable) {
        for (RfbCapabilityInfo c : this.caps.values()) {
            c.setEnable(enable);
        }
    }

    public Collection<EncodingType> getEnabledEncodingTypes() {
        Collection<EncodingType> types = new LinkedList<>();
        for (RfbCapabilityInfo c : this.caps.values()) {
            if (c.isEnabled()) {
                types.add(EncodingType.byId(c.getCode()));
            }
        }
        return types;
    }

    public void read(Transport.Reader reader, int count) throws TransportException {
        while (true) {
            int count2 = count;
            count = count2 - 1;
            if (count2 > 0) {
                RfbCapabilityInfo capInfoReceived = new RfbCapabilityInfo(reader);
                Logger.getLogger("com.glavsoft.rfb").fine(capInfoReceived.toString());
                RfbCapabilityInfo myCapInfo = this.caps.get(Integer.valueOf(capInfoReceived.getCode()));
                if (myCapInfo != null) {
                    myCapInfo.setEnable(true);
                }
            } else {
                return;
            }
        }
    }

    public boolean isSupported(int code) {
        RfbCapabilityInfo myCapInfo = this.caps.get(Integer.valueOf(code));
        if (myCapInfo != null) {
            return myCapInfo.isEnabled();
        }
        return false;
    }

    public boolean isSupported(RfbCapabilityInfo rfbCapabilityInfo) {
        return isSupported(rfbCapabilityInfo.getCode());
    }
}
