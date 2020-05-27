package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

import ru.ard.vnc.glavsoft.drawing.Renderer;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;

public class RawDecoder extends Decoder {
    private static RawDecoder instance = new RawDecoder();

    public static RawDecoder getInstance() {
        return instance;
    }

    private RawDecoder() {
    }

    public void decode(Transport.Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        decode(reader, renderer, rect.x, rect.y, rect.width, rect.height);
    }

    public void decode(Transport.Reader reader, Renderer renderer, int x, int y, int width, int height) throws TransportException {
        int length = width * height * renderer.getBytesPerPixel();
        byte[] bytes = ByteBuffer.getInstance().getBuffer(length);
        reader.readBytes(bytes, 0, length);
        renderer.drawBytes(bytes, x, y, width, height);
    }
}
