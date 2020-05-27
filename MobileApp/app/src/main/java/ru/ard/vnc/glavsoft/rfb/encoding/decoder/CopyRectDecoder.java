package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

import ru.ard.vnc.glavsoft.drawing.Renderer;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;

public class CopyRectDecoder extends Decoder {
    public void decode(Transport.Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        renderer.copyRect(reader.readUInt16(), reader.readUInt16(), rect);
    }
}
