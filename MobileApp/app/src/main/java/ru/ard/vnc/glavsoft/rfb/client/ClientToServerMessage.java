package ru.ard.vnc.glavsoft.rfb.client;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;

public interface ClientToServerMessage {
    public static final byte CLIENT_CUT_TEXT = 6;
    public static final byte FRAMEBUFFER_UPDATE_REQUEST = 3;
    public static final byte KEY_EVENT = 4;
    public static final byte POINTER_EVENT = 5;
    public static final byte SET_ENCODINGS = 2;
    public static final byte SET_PIXEL_FORMAT = 0;

    void send(Transport.Writer writer) throws TransportException;
}
