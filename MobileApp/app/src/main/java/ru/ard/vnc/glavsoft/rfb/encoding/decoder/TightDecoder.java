package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

import ru.ard.vnc.glavsoft.drawing.Renderer;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class TightDecoder extends Decoder {
    private static final int BASIC_FILTER = 0;
    static final int DECODERS_NUM = 4;
    private static final int FILL_TYPE = 8;
    private static final int FILTER_ID_MASK = 64;
    private static final int GRADIENT_FILTER = 2;
    private static final int JPEG_TYPE = 9;
    private static final int MIN_SIZE_TO_COMPRESS = 12;
    private static final int PALETTE_FILTER = 1;
    private static final int STREAM_ID_MASK = 48;
    private static Logger logger = Logger.getLogger("com.glavsoft.rfb.encoding.decoder");
    static final int tightZlibBufferSize = 512;
    private int decoderId;
    Inflater[] decoders;

    public TightDecoder() {
        reset();
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.glavsoft.drawing.Renderer.readCompactPixelColor(com.glavsoft.transport.Transport$Reader, boolean):int
     arg types: [com.glavsoft.transport.Transport$Reader, int]
     candidates:
      com.glavsoft.drawing.Renderer.readCompactPixelColor(byte[], int):int
      com.glavsoft.drawing.Renderer.readCompactPixelColor(com.glavsoft.transport.Transport$Reader, boolean):int */
    public void decode(Transport.Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int bytesPerPixel = renderer.getBytesPerPixelSignificant();
        int compControl = reader.readUInt8();
        resetDecoders(compControl);
        int compType = (compControl >> 4) & 15;
        switch (compType) {
            case 8:
                renderer.fillRect(renderer.readCompactPixelColor(reader, true), rect);
                return;
            case 9:
                if (bytesPerPixel != 3) {
                }
                processJpegType(reader, renderer, rect);
                return;
            default:
                if (compType <= 9) {
                    processBasicType(compControl, reader, renderer, rect);
                    return;
                }
                return;
        }
    }

    private void processBasicType(int compControl, Transport.Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        this.decoderId = (compControl & 48) >> 4;
        int filterId = 0;
        if ((compControl & 64) > 0) {
            filterId = reader.readUInt8();
        }
        int bytesPerCPixel = renderer.getBytesPerPixelSignificant();
        int lengthCurrentbpp = rect.width * bytesPerCPixel * rect.height;
        switch (filterId) {
            case 0:
                renderer.drawBytes(readTightData(lengthCurrentbpp, reader), 0, rect.x, rect.y, rect.width, rect.height, true, true);
                return;
            case 1:
                int paletteSize = reader.readUInt8() + 1;
                renderer.drawBytesWithPalette(readTightData(paletteSize == 2 ? rect.height * ((rect.width + 7) / 8) : rect.width * rect.height, reader), rect, readPalette(paletteSize, reader, renderer));
                return;
            case 2:
                byte[] buffer = readTightData(lengthCurrentbpp, reader);
                int stride = rect.width * bytesPerCPixel;
                for (int i = 0; i < rect.height; i++) {
                    for (int j = 0; j < rect.width * bytesPerCPixel; j++) {
                        int p = ((i + -1 < 0 ? 0 : buffer[((i - 1) * stride) + j] & 255) + (j - bytesPerCPixel < 0 ? 0 : buffer[((i * stride) + j) - bytesPerCPixel] & 255)) - ((j - bytesPerCPixel < 0 || i + -1 < 0) ? 0 : buffer[(((i - 1) * stride) + j) - bytesPerCPixel] & 255);
                        if (p < 0) {
                            p = 0;
                        }
                        if (p > 255) {
                            p = 255;
                        }
                        int i2 = (i * stride) + j;
                        buffer[i2] = (byte) (buffer[i2] + ((byte) p));
                    }
                }
                renderer.drawBytes(buffer, 0, rect.x, rect.y, rect.width, rect.height, true, true);
                return;
            default:
                return;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.glavsoft.drawing.Renderer.readCompactPixelColor(com.glavsoft.transport.Transport$Reader, boolean):int
     arg types: [com.glavsoft.transport.Transport$Reader, int]
     candidates:
      com.glavsoft.drawing.Renderer.readCompactPixelColor(byte[], int):int
      com.glavsoft.drawing.Renderer.readCompactPixelColor(com.glavsoft.transport.Transport$Reader, boolean):int */
    private int[] readPalette(int paletteSize, Transport.Reader reader, Renderer renderer) throws TransportException {
        int[] palette = new int[paletteSize];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = renderer.readCompactPixelColor(reader, true);
        }
        return palette;
    }

    private byte[] readTightData(int expectedLength, Transport.Reader reader) throws TransportException {
        if (expectedLength >= 12) {
            return readCompressedData(expectedLength, reader);
        }
        byte[] buffer = ByteBuffer.getInstance().getBuffer(expectedLength);
        reader.readBytes(buffer, 0, expectedLength);
        return buffer;
    }

    private byte[] readCompressedData(int expectedLength, Transport.Reader reader) throws TransportException {
        int rawDataLength = readCompactSize(reader);
        byte[] buffer = ByteBuffer.getInstance().getBuffer(expectedLength + rawDataLength);
        reader.readBytes(buffer, expectedLength, rawDataLength);
        if (this.decoders[this.decoderId] == null) {
            this.decoders[this.decoderId] = new Inflater();
        }
        Inflater decoder = this.decoders[this.decoderId];
        decoder.setInput(buffer, expectedLength, rawDataLength);
        try {
            decoder.inflate(buffer, 0, expectedLength);
            return buffer;
        } catch (DataFormatException e) {
            logger.throwing("TightDecoder", "readCompressedData", e);
            throw new TransportException("cannot inflate tight compressed data", e);
        }
    }

    private void processJpegType(Transport.Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int jpegBufferLength = readCompactSize(reader);
        byte[] bytes = ByteBuffer.getInstance().getBuffer(jpegBufferLength);
        reader.readBytes(bytes, 0, jpegBufferLength);
        renderer.drawJpegImage(bytes, 0, jpegBufferLength, rect);
    }

    private int readCompactSize(Transport.Reader reader) throws TransportException {
        int b = reader.readUInt8();
        int size = b & 127;
        if ((b & 128) == 0) {
            return size;
        }
        int b2 = reader.readUInt8();
        int size2 = size + ((b2 & 127) << 7);
        if ((b2 & 128) != 0) {
            return size2 + (reader.readUInt8() << 14);
        }
        return size2;
    }

    private void resetDecoders(int compControl) {
        for (int i = 0; i < 4; i++) {
            if (!((compControl & 1) == 0 || this.decoders[i] == null)) {
                this.decoders[i].reset();
            }
            compControl >>= 1;
        }
    }

    public void reset() {
        this.decoders = new Inflater[4];
    }
}
