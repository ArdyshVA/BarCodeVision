package ru.ard.vnc.glavsoft.rfb.client;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;

public class FramebufferUpdateRequestMessage implements ClientToServerMessage {
    private final int height;
    private final boolean incremental;
    private final int width;
    private final int x;
    private final int y;

    public FramebufferUpdateRequestMessage(int x2, int y2, int width2, int height2, boolean incremental2) {
        this.x = x2;
        this.y = y2;
        this.width = width2;
        this.height = height2;
        this.incremental = incremental2;
    }

    public void send(Transport.Writer writer) throws TransportException {
        writer.writeByte(3);
        writer.writeByte(this.incremental ? 1 : 0);
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
        writer.writeInt16(this.width);
        writer.writeInt16(this.height);
        writer.flush();
    }

    public String toString() {
        return "FramebufferUpdateRequestMessage: [x: " + this.x + " y: " + this.y + " width: " + this.width + " height: " + this.height + " incremental: " + this.incremental + "]";
    }
}
