package ru.ard.vnc.glavsoft.rfb.protocol;

import ru.ard.vnc.glavsoft.rfb.CapabilityContainer;
import ru.ard.vnc.glavsoft.rfb.RfbCapabilityInfo;
import ru.ard.vnc.glavsoft.rfb.encoding.EncodingType;
import ru.ard.vnc.glavsoft.rfb.encoding.PixelFormat;
import ru.ard.vnc.glavsoft.rfb.protocol.auth.SecurityType;
import java.util.LinkedHashSet;

public class ProtocolSettings {
    private static final int DEFAULT_COMPRESSION_LEVEL = -6;
    private static final int DEFAULT_JPEG_QUALITY = -6;
    private static final EncodingType DEFAULT_PREFERRED_ENCODING = EncodingType.TIGHT;
    private boolean allowClipboardTransfer;
    private boolean allowCopyRect;
    public CapabilityContainer authCapabilities;
    public CapabilityContainer clientMessagesCapabilities;
    private int compressionLevel;
    public CapabilityContainer encodingTypesCapabilities;
    public LinkedHashSet<EncodingType> encodings;
    private boolean isTight;
    private int jpegQuality;
    private LocalPointer mouseCursorTrack;
    private PixelFormat pixelFormat;
    private EncodingType preferredEncoding;
    private String protocolVersion;
    private double scaling;
    public CapabilityContainer serverMessagesCapabilities;
    private boolean sharedFlag;
    private boolean showRemoteCursor;
    public CapabilityContainer tunnelingCapabilities;
    private boolean viewOnly;

    public static ProtocolSettings getDefaultSettings() {
        ProtocolSettings settings = new ProtocolSettings();
        settings.sharedFlag = true;
        settings.scaling = 1.0d;
        settings.viewOnly = false;
        settings.showRemoteCursor = true;
        settings.mouseCursorTrack = LocalPointer.ON;
        settings.tunnelingCapabilities = new CapabilityContainer();
        settings.authCapabilities = new CapabilityContainer();
        settings.initKnownAuthCapabilities(settings.authCapabilities);
        settings.serverMessagesCapabilities = new CapabilityContainer();
        settings.clientMessagesCapabilities = new CapabilityContainer();
        settings.encodingTypesCapabilities = new CapabilityContainer();
        settings.initKnownEncodingTypesCapabilities(settings.encodingTypesCapabilities);
        settings.preferredEncoding = DEFAULT_PREFERRED_ENCODING;
        settings.allowCopyRect = true;
        settings.compressionLevel = -6;
        settings.jpegQuality = -6;
        settings.refine();
        return settings;
    }

    private void initKnownAuthCapabilities(CapabilityContainer cc) {
        cc.addEnabled(SecurityType.NONE_AUTHENTICATION.getId(), RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.AUTHENTICATION_NO_AUTH);
        cc.addEnabled(SecurityType.VNC_AUTHENTICATION.getId(), RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.AUTHENTICATION_VNC_AUTH);
    }

    private void initKnownEncodingTypesCapabilities(CapabilityContainer cc) {
        cc.add(EncodingType.COPY_RECT.getId(), RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.ENCODING_COPYRECT);
        cc.add(EncodingType.HEXTILE.getId(), RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.ENCODING_HEXTILE);
        cc.add(EncodingType.ZLIB.getId(), RfbCapabilityInfo.VENDOR_TRIADA, RfbCapabilityInfo.ENCODING_ZLIB);
        cc.add(EncodingType.ZRLE.getId(), RfbCapabilityInfo.VENDOR_TRIADA, RfbCapabilityInfo.ENCODING_ZRLE);
        cc.add(EncodingType.RRE.getId(), RfbCapabilityInfo.VENDOR_STANDARD, RfbCapabilityInfo.ENCODING_RRE);
        cc.add(EncodingType.TIGHT.getId(), RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_TIGHT);
        cc.add(EncodingType.RICH_CURSOR.getId(), RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_RICH_CURSOR);
        cc.add(EncodingType.CURSOR_POS.getId(), RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_CURSOR_POS);
        cc.add(EncodingType.DESKTOP_SIZE.getId(), RfbCapabilityInfo.VENDOR_TIGHT, RfbCapabilityInfo.ENCODING_DESKTOP_SIZE);
    }

    public byte getSharedFlag() {
        return (byte) (this.sharedFlag ? 1 : 0);
    }

    public boolean isShared() {
        return this.sharedFlag;
    }

    public void setSharedFlag(boolean sharedFlag2) {
        this.sharedFlag = sharedFlag2;
    }

    public double getScaling() {
        return this.scaling;
    }

    public void setScaling(double scaling2) {
        this.scaling = scaling2;
    }

    public boolean isViewOnly() {
        return this.viewOnly;
    }

    public void setViewOnly(boolean viewOnly2) {
        this.viewOnly = viewOnly2;
    }

    public void enableAllEncodingCaps() {
        this.encodingTypesCapabilities.setAllEnable(true);
    }

    private void refine() {
        this.encodings = new LinkedHashSet<>();
        if (EncodingType.RAW_ENCODING != this.preferredEncoding) {
            this.encodings.add(this.preferredEncoding);
            this.encodings.addAll(EncodingType.ordinaryEncodings);
            if (this.compressionLevel > 0 && this.compressionLevel < 10) {
                this.encodings.add(EncodingType.byId(EncodingType.COMPRESS_LEVEL_0.getId() + this.compressionLevel));
            }
            if (this.jpegQuality > 0 && this.jpegQuality < 10) {
                this.encodings.add(EncodingType.byId(EncodingType.JPEG_QUALITY_LEVEL_0.getId() + this.jpegQuality));
            }
            if (this.allowCopyRect) {
                this.encodings.add(EncodingType.COPY_RECT);
            }
        }
        switch (this.mouseCursorTrack) {
            case OFF:
                this.showRemoteCursor = false;
                return;
            case HIDE:
                this.showRemoteCursor = false;
                this.encodings.add(EncodingType.RICH_CURSOR);
                this.encodings.add(EncodingType.CURSOR_POS);
                return;
            default:
                this.showRemoteCursor = true;
                this.encodings.add(EncodingType.RICH_CURSOR);
                this.encodings.add(EncodingType.CURSOR_POS);
                return;
        }
    }

    public void setPixelFormat(PixelFormat pixelFormat2) {
        this.pixelFormat = pixelFormat2;
    }

    public PixelFormat getPixelFormat() {
        return this.pixelFormat;
    }

    public void setPreferredEncoding(EncodingType preferredEncoding2) {
        this.preferredEncoding = preferredEncoding2;
        refine();
    }

    public EncodingType getPreferredEncoding() {
        return this.preferredEncoding;
    }

    public void setAllowCopyRect(boolean allowCopyRect2) {
        this.allowCopyRect = allowCopyRect2;
        refine();
    }

    public boolean isAllowCopyRect() {
        return this.allowCopyRect;
    }

    public boolean isShowRemoteCursor() {
        return this.showRemoteCursor;
    }

    public void setMouseCursorTrack(LocalPointer mouseCursorTrack2) {
        this.mouseCursorTrack = mouseCursorTrack2;
        refine();
    }

    public LocalPointer getMouseCursorTrack() {
        return this.mouseCursorTrack;
    }

    public void setCompressionLevel(int compressionLevel2) {
        this.compressionLevel = compressionLevel2;
        refine();
    }

    public int getCompressionLevel() {
        return this.compressionLevel;
    }

    public void setJpegQuality(int jpegQuality2) {
        this.jpegQuality = jpegQuality2;
        refine();
    }

    public int getJpegQuality() {
        return this.jpegQuality;
    }

    public void setAllowClipboardTransfer(boolean allowClipboardTransfer2) {
        this.allowClipboardTransfer = allowClipboardTransfer2;
    }

    public boolean isAllowClipboardTransfer() {
        return this.allowClipboardTransfer;
    }

    public void setTight(boolean isTight2) {
        this.isTight = isTight2;
    }

    public boolean isTight() {
        return this.isTight;
    }

    public void setProtocolVersion(String protocolVersion2) {
        this.protocolVersion = protocolVersion2;
    }

    public String getProtocolVersion() {
        return this.protocolVersion;
    }
}
