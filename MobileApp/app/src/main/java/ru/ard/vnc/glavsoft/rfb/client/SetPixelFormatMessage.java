package ru.ard.vnc.glavsoft.rfb.client;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.rfb.encoding.PixelFormat;
import ru.ard.vnc.glavsoft.transport.Transport;

public class SetPixelFormatMessage implements ClientToServerMessage {
    private final PixelFormat pixelFormat;

    public SetPixelFormatMessage(PixelFormat pixelFormat2) {
        this.pixelFormat = pixelFormat2;
    }

    public void send(Transport.Writer writer) throws TransportException {
        writer.writeByte(0);
        writer.writeInt16(0);
        writer.writeByte(0);
        this.pixelFormat.send(writer);
        writer.flush();
    }
}
