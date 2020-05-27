package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

import ru.ard.vnc.glavsoft.drawing.Renderer;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;

public class RichCursorDecoder extends Decoder {
    private static RichCursorDecoder instance = new RichCursorDecoder();

    private RichCursorDecoder() {
    }

    public static RichCursorDecoder getInstance() {
        return instance;
    }

    public void decode(Transport.Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int bytesPerPixel = renderer.getBytesPerPixel();
        int length = rect.width * rect.height * bytesPerPixel;
        if (length != 0) {
            byte[] buffer = ByteBuffer.getInstance().getBuffer(length);
            reader.readBytes(buffer, 0, length);
            int scanLine = (int) Math.floor((double) ((rect.width + 7) / 8));
            byte[] bitmask = new byte[(rect.height * scanLine)];
            reader.readBytes(bitmask, 0, bitmask.length);
            int[] cursorPixels = new int[(rect.width * rect.height)];
            for (int y = 0; y < rect.height; y++) {
                for (int x = 0; x < rect.width; x++) {
                    int offset = (rect.width * y) + x;
                    cursorPixels[offset] = isBitSet(bitmask[(y * scanLine) + (x / 8)], x % 8) ? -16777216 | renderer.readCompactPixelColor(buffer, offset * bytesPerPixel) : 0;
                }
            }
            renderer.createCursor(cursorPixels, rect);
        }
    }

    private boolean isBitSet(byte aByte, int index) {
        return ((1 << (7 - index)) & aByte) > 0;
    }
}
