package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

import ru.ard.vnc.glavsoft.drawing.Renderer;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;

public class RREDecoder extends Decoder {
    public void decode(Transport.Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int numOfSubrectangles = reader.readInt32();
        renderer.fillRect(renderer.readPixelColor(reader), rect);
        for (int i = 0; i < numOfSubrectangles; i++) {
            int color = renderer.readPixelColor(reader);
            int x = reader.readUInt16();
            int y = reader.readUInt16();
            int width = reader.readUInt16();
            int height = reader.readUInt16();
            renderer.fillRect(color, rect.x + x, rect.y + y, width, height);
        }
    }
}
