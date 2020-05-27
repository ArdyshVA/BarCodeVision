package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.rfb.encoding.EncodingType;
import ru.ard.vnc.glavsoft.transport.Transport;

public class FramebufferUpdateRectangle {
    private EncodingType encodingType;
    public int height;
    public int width;
    public int x;
    public int y;

    public FramebufferUpdateRectangle() {
    }

    public FramebufferUpdateRectangle(int x2, int y2, int w, int h) {
        this.x = x2;
        this.y = y2;
        this.width = w;
        this.height = h;
    }

    public void fill(Transport.Reader reader) throws TransportException {
        this.x = reader.readUInt16();
        this.y = reader.readUInt16();
        this.width = reader.readUInt16();
        this.height = reader.readUInt16();
        this.encodingType = EncodingType.byId(reader.readInt32());
    }

    public EncodingType getEncodingType() {
        return this.encodingType;
    }

    public String toString() {
        return "FramebufferUpdateRect: [x: " + this.x + ", y: " + this.y + ", width: " + this.width + ", height: " + this.height + ", encodingType: " + this.encodingType + "]";
    }
}
