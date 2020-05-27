package ru.ard.vnc.glavsoft.rfb.protocol;

import ru.ard.vnc.glavsoft.exceptions.AuthenticationFailedException;
import ru.ard.vnc.glavsoft.exceptions.FatalException;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedProtocolVersionException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedSecurityTypeException;
import ru.ard.vnc.glavsoft.rfb.client.FramebufferUpdateRequestMessage;
import ru.ard.vnc.glavsoft.rfb.client.SetEncodingsMessage;
import ru.ard.vnc.glavsoft.rfb.client.SetPixelFormatMessage;
import ru.ard.vnc.glavsoft.rfb.encoding.PixelFormat;
import ru.ard.vnc.glavsoft.rfb.encoding.ServerInitMessage;
import ru.ard.vnc.glavsoft.rfb.protocol.state.HandshakeState;
import ru.ard.vnc.glavsoft.rfb.protocol.state.ProtocolState;
import ru.ard.vnc.glavsoft.transport.Transport;
import java.util.logging.Logger;

import ru.ard.vnc.glavsoft.exceptions.AuthenticationFailedException;
import ru.ard.vnc.glavsoft.exceptions.FatalException;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedProtocolVersionException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedSecurityTypeException;
import ru.ard.vnc.glavsoft.rfb.encoding.PixelFormat;
import ru.ard.vnc.glavsoft.rfb.encoding.ServerInitMessage;
import ru.ard.vnc.glavsoft.rfb.protocol.state.HandshakeState;
import ru.ard.vnc.glavsoft.rfb.protocol.state.ProtocolState;
import ru.ard.vnc.glavsoft.transport.Transport;

public class Protocol implements ProtocolContext {
    private int fbHeight;
    private int fbWidth;
    private final Logger logger = Logger.getLogger("com.glavsoft.rfb.protocol");
    private final String passwordRetriever;
    private PixelFormat pixelFormat;
    private final Transport.Reader reader;
    private ServerInitMessage serverInitMessage;
    private final ProtocolSettings settings;
    private ProtocolState state;
    private final Transport.Writer writer;

    public void process() throws UnsupportedProtocolVersionException, TransportException, UnsupportedSecurityTypeException, AuthenticationFailedException, FatalException {
        handshake();
        negotiateAboutSecurityType();
        authenticate();
        clientAndServerInit();
    }

    public Protocol(Transport.Reader reader2, Transport.Writer writer2, String passwordRetriever2, ProtocolSettings settings2) {
        this.reader = reader2;
        this.writer = writer2;
        this.passwordRetriever = passwordRetriever2;
        this.settings = settings2;
        this.state = new HandshakeState(this);
    }

    public void changeStateTo(ProtocolState state2) {
        this.state = state2;
    }

    public void handshake() throws TransportException, UnsupportedProtocolVersionException {
        this.state.handshake();
    }

    public void negotiateAboutSecurityType() throws UnsupportedSecurityTypeException, TransportException {
        this.state.negotiateAboutSecurityType();
    }

    public void authenticate() throws TransportException, AuthenticationFailedException, FatalException, UnsupportedSecurityTypeException {
        this.state.authenticate();
    }

    public void clientAndServerInit() throws TransportException {
        this.serverInitMessage = this.state.clientAndServerInit();
        this.logger.fine(this.serverInitMessage.toString());
        this.pixelFormat = this.serverInitMessage.getPixelFormat();
        this.fbWidth = this.serverInitMessage.getFrameBufferWidth();
        this.fbHeight = this.serverInitMessage.getFrameBufferHeight();
    }

    public void set32bppPixelFormat() {
        PixelFormat.set32bppPixelFormat(this.pixelFormat);
    }

    public PixelFormat getPixelFormat() {
        return this.pixelFormat;
    }

    public String getRemoteDesktopName() {
        return this.serverInitMessage.getName();
    }

    public int getFbWidth() {
        return this.fbWidth;
    }

    public void setFbWidth(int fbWidth2) {
        this.fbWidth = fbWidth2;
    }

    public int getFbHeight() {
        return this.fbHeight;
    }

    public void setFbHeight(int fbHeight2) {
        this.fbHeight = fbHeight2;
    }

    public String getPasswordRetriever() {
        return this.passwordRetriever;
    }

    public ProtocolSettings getSettings() {
        return this.settings;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Transport.Writer getWriter() {
        return this.writer;
    }

    public Transport.Reader getReader() {
        return this.reader;
    }

    public void startNormalHandling(MessageQueue senderQueue) {
        senderQueue.put(new SetPixelFormatMessage(this.pixelFormat));
        this.logger.fine("sent: " + this.pixelFormat.toString());
        SetEncodingsMessage encodingsMessage = new SetEncodingsMessage(this.settings.encodings);
        senderQueue.put(encodingsMessage);
        this.logger.fine("sent: " + encodingsMessage.toString());
        FramebufferUpdateRequestMessage frambufferUpdateRequestMessage = new FramebufferUpdateRequestMessage(0, 0, this.fbWidth, this.fbHeight, false);
        senderQueue.put(frambufferUpdateRequestMessage);
        this.logger.fine("sent: " + frambufferUpdateRequestMessage);
    }
}
