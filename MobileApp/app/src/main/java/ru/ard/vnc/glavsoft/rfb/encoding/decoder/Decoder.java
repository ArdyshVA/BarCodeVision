package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

import ru.ard.vnc.glavsoft.drawing.Renderer;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;

public abstract class Decoder {
    public abstract void decode(Transport.Reader reader, Renderer renderer, FramebufferUpdateRectangle framebufferUpdateRectangle) throws TransportException;

    public void reset() {
    }
}
