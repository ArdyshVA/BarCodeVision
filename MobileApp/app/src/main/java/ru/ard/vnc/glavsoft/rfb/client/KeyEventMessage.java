package ru.ard.vnc.glavsoft.rfb.client;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;

public class KeyEventMessage implements ClientToServerMessage {
    private final boolean downFlag;
    private final int key;

    public KeyEventMessage(int key2, boolean downFlag2) {
        this.downFlag = downFlag2;
        this.key = key2;
    }

    public void send(Transport.Writer writer) throws TransportException {
        int i;
        writer.writeByte(4);
        if (this.downFlag) {
            i = 1;
        } else {
            i = 0;
        }
        writer.writeByte(i);
        writer.writeInt16(0);
        writer.write(this.key);
        writer.flush();
    }

    public String toString() {
        return "[KeyEventMessage: [down-flag: " + this.downFlag + ", key: " + this.key + "(" + Integer.toHexString(this.key) + ")]";
    }
}
