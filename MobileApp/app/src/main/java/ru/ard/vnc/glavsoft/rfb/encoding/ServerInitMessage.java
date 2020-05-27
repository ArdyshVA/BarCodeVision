package ru.ard.vnc.glavsoft.rfb.encoding;

import ru.ard.vnc.glavsoft.transport.Transport;

public class ServerInitMessage {
    protected int frameBufferHeight;
    protected int frameBufferWidth;
    protected String name;
    protected PixelFormat pixelFormat;

    public ServerInitMessage(Transport.Reader reader) throws Exception {
        this.frameBufferWidth = reader.readUInt16();
        this.frameBufferHeight = reader.readUInt16();
        this.pixelFormat = new PixelFormat();
        this.pixelFormat.fill(reader);
        this.name = reader.readString();
    }

    protected ServerInitMessage() {
    }

    public int getFrameBufferWidth() {
        return this.frameBufferWidth;
    }

    public int getFrameBufferHeight() {
        return this.frameBufferHeight;
    }

    public PixelFormat getPixelFormat() {
        return this.pixelFormat;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "ServerInitMessage: [name: " + this.name + ", framebuffer-width: " + String.valueOf(this.frameBufferWidth) + ", framebuffer-height: " + String.valueOf(this.frameBufferHeight) + ", server-pixel-format: " + this.pixelFormat + "]";
    }
}
