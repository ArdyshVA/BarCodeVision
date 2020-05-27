package ru.ard.vnc.glavsoft.rfb.protocol;

import ru.ard.vnc.glavsoft.rfb.encoding.PixelFormat;
import ru.ard.vnc.glavsoft.rfb.protocol.state.ProtocolState;
import ru.ard.vnc.glavsoft.transport.Transport;
import java.util.logging.Logger;

public interface ProtocolContext {
    void changeStateTo(ProtocolState protocolState);

    int getFbHeight();

    int getFbWidth();

    Logger getLogger();

    String getPasswordRetriever();

    PixelFormat getPixelFormat();

    Transport.Reader getReader();

    ProtocolSettings getSettings();

    Transport.Writer getWriter();
}
