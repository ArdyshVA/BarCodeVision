package ru.ard.vnc.glavsoft.rfb.client;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;

public class PointerEventMessage implements ClientToServerMessage {
    private final byte buttonMask;
    private final short x;
    private final short y;

    public PointerEventMessage(byte buttonMask2, short x2, short y2) {
        this.buttonMask = buttonMask2;
        this.x = x2;
        this.y = y2;
    }

    public void send(Transport.Writer writer) throws TransportException {
        writer.writeByte(5);
        writer.writeByte(this.buttonMask);
        writer.writeInt16(this.x);
        writer.writeInt16(this.y);
        writer.flush();
    }

    public String toString() {
        return "PointerEventMessage: [x: " + ((int) this.x) + ", y: " + ((int) this.y) + ", button-mask: " + ((int) this.buttonMask) + "]";
    }
}
