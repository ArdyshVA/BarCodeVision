package ru.ard.vnc.glavsoft.rfb.client;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;

public class ClientCutTextMessage implements ClientToServerMessage {
    private final String text;

    public ClientCutTextMessage(String text2) {
        this.text = text2;
    }

    public void send(Transport.Writer writer) throws TransportException {
        writer.write((byte) 6);
        writer.writeByte(0);
        writer.writeInt16(0);
        writer.write(this.text.length());
        try {
            writer.write(this.text);
            writer.flush();
        } catch (Exception e) {
            throw new TransportException(null);
        }
    }
}
