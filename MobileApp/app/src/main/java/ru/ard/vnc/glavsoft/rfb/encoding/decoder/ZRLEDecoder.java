package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

import ru.ard.vnc.glavsoft.drawing.Renderer;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;
import java.util.logging.Logger;

public class ZRLEDecoder extends ZlibDecoder {
    static final /* synthetic */ boolean $assertionsDisabled = (!ZRLEDecoder.class.desiredAssertionStatus());
    private static final int DEFAULT_TILE_SIZE = 64;
    private final Logger logger = Logger.getLogger("com.glavsoft.rfb.encoding");

    public void decode(Transport.Reader reader, Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
        int offset=0;
        int offset2;
        int zippedLength = (int) reader.readUInt32();
        byte[] bytes = unzip(reader, zippedLength, rect.width * rect.height * renderer.getBytesPerPixel());
        int offset3 = zippedLength;
        int maxX = rect.x + rect.width;
        int maxY = rect.y + rect.height;
        int[] palette = new int[128];
        int tileY = rect.y;
        while (tileY < maxY) {
            int tileHeight = Math.min(maxY - tileY, 64);
            int tileX = rect.x;
            while (true) {
                offset2 = offset;
                if (tileX >= maxX) {
                    break;
                }
                int tileWidth = Math.min(maxX - tileX, 64);
                int offset4 = offset2 + 1;
                int subencoding = bytes[offset2] & 255;
                boolean isRle = (subencoding & 128) != 0;
                int paletteSize = subencoding & 127;
                offset = offset4 + readPalette(bytes, offset4, renderer, palette, paletteSize);
                if (1 == subencoding) {
                    renderer.fillRect(palette[0], tileX, tileY, tileWidth, tileHeight);
                } else if (isRle) {
                    if (paletteSize == 0) {
                        offset += decodePlainRle(bytes, offset, renderer, tileX, tileY, tileWidth, tileHeight);
                    } else {
                        offset += decodePaletteRle(bytes, offset, renderer, palette, tileX, tileY, tileWidth, tileHeight, paletteSize);
                    }
                } else if (paletteSize == 0) {
                    offset += decodeRaw(bytes, offset, renderer, tileX, tileY, tileWidth, tileHeight);
                } else {
                    offset += decodePacked(bytes, offset, renderer, palette, paletteSize, tileX, tileY, tileWidth, tileHeight);
                }
                tileX += 64;
            }
            tileY += 64;
            offset3 = offset2;
        }
    }

    private int decodePlainRle(byte[] bytes, int offset, Renderer renderer, int tileX, int tileY, int tileWidth, int tileHeight) {
        int index;
        int bytesPerCPixel = renderer.getBytesPerPixelSignificant();
        byte[] decodedBytes = new byte[(tileWidth * tileHeight * bytesPerCPixel)];
        int decodedOffset = 0;
        int decodedEnd = tileWidth * tileHeight * bytesPerCPixel;
        int index2 = offset;
        while (decodedOffset < decodedEnd) {
            int color = renderer.readCompactPixelColor(bytes, index2);
            int index3 = index2 + bytesPerCPixel;
            int rlength = 1;
            while (true) {
                rlength += bytes[index3] & 255;
                index = index3 + 1;
                if ((bytes[index3] & 255) != 255) {
                    break;
                }
                index3 = index;
            }
            if ($assertionsDisabled || rlength * bytesPerCPixel <= decodedEnd - decodedOffset) {
                renderer.putPixelsIntoByteArray(decodedBytes, decodedOffset, rlength, color);
                decodedOffset += rlength * bytesPerCPixel;
                index2 = index;
            } else {
                throw new AssertionError();
            }
        }
        renderer.drawBytes(decodedBytes, 0, tileX, tileY, tileWidth, tileHeight, true);
        return index2 - offset;
    }

    private int decodePaletteRle(byte[] bytes, int offset, Renderer renderer, int[] palette, int tileX, int tileY, int tileWidth, int tileHeight, int paletteSize) {
        int index;
        int bytesPerCPixel = renderer.getBytesPerPixelSignificant();
        byte[] decodedBytes = new byte[(tileWidth * tileHeight * bytesPerCPixel)];
        int decodedOffset = 0;
        int decodedEnd = tileWidth * tileHeight * bytesPerCPixel;
        int index2 = offset;
        while (decodedOffset < decodedEnd) {
            int index3 = index2 + 1;
            byte b = bytes[index2];
            int color = palette[b & Byte.MAX_VALUE];
            int rlength = 1;
            if ((b & 128) != 0) {
                while (true) {
                    rlength += bytes[index3] & 255;
                    index = index3 + 1;
                    if (bytes[index3] != -1) {
                        break;
                    }
                    index3 = index;
                }
                index3 = index;
            }
            if ($assertionsDisabled || rlength * bytesPerCPixel <= decodedEnd - decodedOffset) {
                if (rlength * bytesPerCPixel > decodedEnd - decodedOffset) {
                    this.logger.severe("rlength: " + rlength + " must be: " + ((decodedEnd - decodedOffset) / bytesPerCPixel));
                    rlength = (decodedEnd - decodedOffset) / bytesPerCPixel;
                }
                renderer.putPixelsIntoByteArray(decodedBytes, decodedOffset, rlength, color);
                decodedOffset += rlength * bytesPerCPixel;
                index2 = index3;
            } else {
                throw new AssertionError();
            }
        }
        renderer.drawBytes(decodedBytes, 0, tileX, tileY, tileWidth, tileHeight, true);
        return index2 - offset;
    }

    private int decodePacked(byte[] bytes, int offset, Renderer renderer, int[] palette, int paletteSize, int tileX, int tileY, int tileWidth, int tileHeight) {
        int packedOffset;
        byte[] decodedBytes = new byte[(tileWidth * tileHeight * renderer.getBytesPerPixelSignificant())];
        int bitsPerPalletedPixel = paletteSize > 16 ? 8 : paletteSize > 4 ? 4 : paletteSize > 2 ? 2 : 1;
        int packedOffset2 = offset;
        int decodedOffset = 0;
        int i = 0;
        while (i < tileHeight) {
            int decodedRowEnd = decodedOffset + (renderer.getBytesPerPixelSignificant() * tileWidth);
            byte b = 0;
            int bitsRemain = 0;
            int packedOffset3 = packedOffset2;
            while (decodedOffset < decodedRowEnd) {
                if (bitsRemain == 0) {
                    packedOffset = packedOffset3 + 1;
                    b = bytes[packedOffset3];
                    bitsRemain = 8;
                } else {
                    packedOffset = packedOffset3;
                }
                bitsRemain -= bitsPerPalletedPixel;
                decodedOffset += renderer.putPixelIntoByteArray(decodedBytes, decodedOffset, palette[(b >> bitsRemain) & ((1 << bitsPerPalletedPixel) - 1) & 127]);
                packedOffset3 = packedOffset;
            }
            i++;
            packedOffset2 = packedOffset3;
        }
        renderer.drawBytes(decodedBytes, 0, tileX, tileY, tileWidth, tileHeight, true);
        return packedOffset2 - offset;
    }

    private int decodeRaw(byte[] bytes, int offset, Renderer renderer, int tileX, int tileY, int tileWidth, int tileHeight) throws TransportException {
        return renderer.drawBytes(bytes, offset, tileX, tileY, tileWidth, tileHeight, true);
    }

    private int readPalette(byte[] bytes, int offset, Renderer renderer, int[] palette, int paletteSize) {
        for (int i = 0; i < paletteSize; i++) {
            palette[i] = renderer.readCompactPixelColor(bytes, (renderer.getBytesPerPixelSignificant() * i) + offset);
        }
        return renderer.getBytesPerPixelSignificant() * paletteSize;
    }
}
