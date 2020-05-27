package ru.ard.vnc.glavsoft.rfb;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;

public class RfbCapabilityInfo {
    public static final String AUTHENTICATION_NO_AUTH = "NOAUTH__";
    public static final String AUTHENTICATION_VNC_AUTH = "VNCAUTH_";
    public static final String ENCODING_COPYRECT = "COPYRECT";
    public static final String ENCODING_CURSOR_POS = "POINTPOS";
    public static final String ENCODING_DESKTOP_SIZE = "NEWFBSIZ";
    public static final String ENCODING_HEXTILE = "HEXTILE_";
    public static final String ENCODING_RICH_CURSOR = "RCHCURSR";
    public static final String ENCODING_RRE = "RRE_____";
    public static final String ENCODING_TIGHT = "TIGHT___";
    public static final String ENCODING_ZLIB = "ZLIB____";
    public static final String ENCODING_ZRLE = "ZRLE____";
    public static final String TUNNELING_NO_TUNNELING = "NOTUNNEL";
    public static final String VENDOR_STANDARD = "STDV";
    public static final String VENDOR_TIGHT = "TGHT";
    public static final String VENDOR_TRIADA = "TRDV";
    private int code;
    private boolean enable;
    private String nameSignature;
    private String vendorSignature;

    public RfbCapabilityInfo(int code2, String vendorSignature2, String nameSignature2) {
        this.code = code2;
        this.vendorSignature = vendorSignature2;
        this.nameSignature = nameSignature2;
        this.enable = true;
    }

    public RfbCapabilityInfo(Transport.Reader reader) throws TransportException {
        this.code = reader.readInt32();
        try {
            this.vendorSignature = reader.readString(4);
            this.nameSignature = reader.readString(8);
        } catch (Exception e) {
            throw new TransportException(null);
        }
    }

    public boolean equals(Object otherObj) {
        if (this == otherObj) {
            return true;
        }
        if (otherObj == null) {
            return false;
        }
        if (getClass() != otherObj.getClass()) {
            return false;
        }
        RfbCapabilityInfo other = (RfbCapabilityInfo) otherObj;
        if (this.code != other.code || !this.vendorSignature.equals(other.vendorSignature) || !this.nameSignature.equals(other.nameSignature)) {
            return false;
        }
        return true;
    }

    public void setEnable(boolean enable2) {
        this.enable = enable2;
    }

    public int getCode() {
        return this.code;
    }

    public String getVendorSignature() {
        return this.vendorSignature;
    }

    public String getNameSignature() {
        return this.nameSignature;
    }

    public boolean isEnabled() {
        return this.enable;
    }

    public String toString() {
        return "RfbCapabilityInfo: [code: " + this.code + ", vendor: " + this.vendorSignature + ", name: " + this.nameSignature + "]";
    }
}
