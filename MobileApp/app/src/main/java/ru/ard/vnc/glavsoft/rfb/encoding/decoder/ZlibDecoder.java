package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

import ru.ard.vnc.glavsoft.drawing.Renderer;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;
import java.io.ByteArrayInputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ZlibDecoder extends Decoder {
    private Inflater decoder;

    public void decode(Transport.Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int zippedLength = (int) reader.readUInt32();
        int length = rect.width * rect.height * renderer.getBytesPerPixel();
        RawDecoder.getInstance().decode(new Transport.Reader(new ByteArrayInputStream(unzip(reader, zippedLength, length), zippedLength, length)), renderer, rect);
    }

    /* access modifiers changed from: protected */
    public byte[] unzip(Transport.Reader reader, int zippedLength, int length) throws TransportException {
        byte[] bytes = ByteBuffer.getInstance().getBuffer(zippedLength + length);
        reader.readBytes(bytes, 0, zippedLength);
        if (this.decoder == null) {
            this.decoder = new Inflater();
        }
        this.decoder.setInput(bytes, 0, zippedLength);
        try {
            this.decoder.inflate(bytes, zippedLength, length);
            return bytes;
        } catch (DataFormatException e) {
            throw new TransportException("cannot inflate Zlib data", e);
        }
    }

    public void reset() {
        this.decoder = null;
    }
}
