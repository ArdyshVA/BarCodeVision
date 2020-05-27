package ru.ard.vnc.glavsoft.rfb.client;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.rfb.encoding.EncodingType;
import ru.ard.vnc.glavsoft.transport.Transport;
import java.util.Set;

public class SetEncodingsMessage implements ClientToServerMessage {
    private final Set<EncodingType> encodings;

    public SetEncodingsMessage(Set<EncodingType> set) {
        this.encodings = set;
    }

    public void send(Transport.Writer writer) throws TransportException {
        writer.writeByte(2);
        writer.writeByte(0);
        writer.writeInt16(this.encodings.size());
        for (EncodingType enc : this.encodings) {
            writer.writeInt32(enc.getId());
        }
        writer.flush();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("SetEncodingsMessage: [encodings: ");
        for (EncodingType enc : this.encodings) {
            sb.append(enc.name()).append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.append(']').toString();
    }
}
