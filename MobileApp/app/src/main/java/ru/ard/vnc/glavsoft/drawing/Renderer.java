package ru.ard.vnc.glavsoft.drawing;

import androidx.core.view.MotionEventCompat;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.rfb.encoding.PixelFormat;
import ru.ard.vnc.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import ru.ard.vnc.glavsoft.transport.Transport;
import java.util.Arrays;

public abstract class Renderer {
    protected int bytesPerPixel;
    protected int bytesPerPixelSignificant;
    protected SoftCursor cursor;
    protected int height;
    protected PixelFormat pixelFormat;
    protected int[] pixels;
    protected Transport.Reader reader;
    protected int width;

    public abstract void drawJpegImage(byte[] bArr, int i, int i2, FramebufferUpdateRectangle framebufferUpdateRectangle);

    public void drawBytes(byte[] bytes, int x, int y, int width2, int height2) {
        drawBytes(bytes, 0, x, y, width2, height2, false);
    }

    public synchronized int drawBytes(byte[] bytes, int offset, int x, int y, int width2, int height2, boolean isCompressed) {
        return drawBytes(bytes, offset, x, y, width2, height2, isCompressed, false);
    }

    public synchronized int drawBytes(byte[] bytes, int offset, int x, int y, int width2, int height2, boolean isCompressed, boolean needSwap) {
        int i;
        i = offset;
        for (int ly = y; ly < y + height2; ly++) {
            int end = (this.width * ly) + x + width2;
            for (int pixelsOffset = (this.width * ly) + x; pixelsOffset < end; pixelsOffset++) {
                this.pixels[pixelsOffset] = readCompactPixelColor(bytes, i, needSwap);
                i += isCompressed ? this.bytesPerPixelSignificant : this.bytesPerPixel;
            }
        }
        return i - offset;
    }

    public synchronized void drawBytesWithPalette(byte[] buffer, FramebufferUpdateRectangle rect, int[] palette) {
        int i;
        if (palette.length == 2) {
            int i2 = (rect.y * this.width) + rect.x;
            int rowBytes = (rect.width + 7) / 8;
            for (int dy = 0; dy < rect.height; dy++) {
                int dx = 0;
                while (dx < rect.width / 8) {
                    byte b = buffer[(dy * rowBytes) + dx];
                    int n = 7;
                    int i3 = i2;
                    while (n >= 0) {
                        this.pixels[i3] = palette[(b >> n) & 1];
                        n--;
                        i3++;
                    }
                    dx++;
                    i2 = i3;
                }
                int n2 = 7;
                int i4 = i2;
                while (n2 >= 8 - (rect.width % 8)) {
                    this.pixels[i4] = palette[(buffer[(dy * rowBytes) + dx] >> n2) & 1];
                    n2--;
                    i4++;
                }
                i2 = i4 + (this.width - rect.width);
            }
        } else {
            int i5 = 0;
            int ly = rect.y;
            while (ly < rect.y + rect.height) {
                int lx = rect.x;
                while (true) {
                    i = i5;
                    if (lx >= rect.x + rect.width) {
                        break;
                    }
                    i5 = i + 1;
                    this.pixels[(this.width * ly) + lx] = palette[buffer[i] & 255];
                    lx++;
                }
                ly++;
                i5 = i;
            }
        }
    }

    public synchronized void copyRect(int srcX, int srcY, FramebufferUpdateRectangle dstRect) {
        int startSrcY;
        int endSrcY;
        int dstY;
        int deltaY;
        if (srcY > dstRect.y) {
            startSrcY = srcY;
            endSrcY = srcY + dstRect.height;
            dstY = dstRect.y;
            deltaY = 1;
        } else {
            startSrcY = (dstRect.height + srcY) - 1;
            endSrcY = srcY - 1;
            dstY = (dstRect.y + dstRect.height) - 1;
            deltaY = -1;
        }
        for (int y = startSrcY; y != endSrcY; y += deltaY) {
            System.arraycopy(this.pixels, (this.width * y) + srcX, this.pixels, (this.width * dstY) + dstRect.x, dstRect.width);
            dstY += deltaY;
        }
    }

    public void fillRect(int color, FramebufferUpdateRectangle rect) {
        fillRect(color, rect.x, rect.y, rect.width, rect.height);
    }

    public synchronized void fillRect(int color, int x, int y, int width2, int height2) {
        int sy = (this.width * y) + x;
        int ey = sy + (this.width * height2);
        int i = sy;
        while (i < ey) {
            Arrays.fill(this.pixels, i, i + width2, color);
            i += this.width;
        }
    }

    public int readPixelColor(Transport.Reader reader2) throws TransportException {
        int color = readCompactPixelColor(reader2);
        if (4 == this.bytesPerPixel) {
            reader2.readByte();
        }
        return color;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.glavsoft.drawing.Renderer.readCompactPixelColor(com.glavsoft.transport.Transport$Reader, boolean):int
     arg types: [com.glavsoft.transport.Transport$Reader, int]
     candidates:
      com.glavsoft.drawing.Renderer.readCompactPixelColor(byte[], int):int
      com.glavsoft.drawing.Renderer.readCompactPixelColor(com.glavsoft.transport.Transport$Reader, boolean):int */
    public int readCompactPixelColor(Transport.Reader reader2) throws TransportException {
        return readCompactPixelColor(reader2, false);
    }

    public int readCompactPixelColor(Transport.Reader reader2, boolean needSwap) throws TransportException {
        int c = this.bytesPerPixelSignificant;
        int color = 0;
        do {
            color = (color << 8) | reader2.readUInt8();
            c--;
        } while (c > 0);
        if (needSwap && this.pixelFormat.bigEndianFlag == 0) {
            color = swapColorBytes(color);
        }
        return convertColor(color);
    }

    public int readCompactPixelColor(byte[] bytes, int offset) {
        return readCompactPixelColor(bytes, offset, false);
    }

    public int readCompactPixelColor(byte[] bytes, int offset, boolean needSwap) {
        int c = this.bytesPerPixelSignificant;
        int color = 0;
        while (true) {
            int offset2 = offset + 1;
            color = (color << 8) | (bytes[offset] & 255);
            c--;
            if (c <= 0) {
                break;
            }
            offset = offset2;
        }
        if (needSwap && this.pixelFormat.bigEndianFlag == 0) {
            color = swapColorBytes(color);
        }
        return convertColor(color);
    }

    private int swapColorBytes(int rawColor) {
        if (3 == this.bytesPerPixelSignificant) {
            return ((rawColor >> 16) & 255) | (rawColor & MotionEventCompat.ACTION_POINTER_INDEX_MASK) | ((rawColor << 16) & 16711680);
        }
        if (2 == this.bytesPerPixelSignificant) {
            return ((rawColor >> 8) & 255) | ((rawColor << 8) & MotionEventCompat.ACTION_POINTER_INDEX_MASK);
        }
        return rawColor;
    }

    private int convertColor(int rawColor) {
        return ((rawColor >> this.pixelFormat.redShift) & this.pixelFormat.redMax) | (((rawColor >> this.pixelFormat.greenShift) & this.pixelFormat.greenMax) << 8) | (((rawColor >> this.pixelFormat.blueShift) & this.pixelFormat.blueMax) << 16);
    }

    public int getBytesPerPixel() {
        return this.bytesPerPixel;
    }

    public int getBytesPerPixelSignificant() {
        return this.bytesPerPixelSignificant;
    }

    public int putPixelIntoByteArray(byte[] decodedBytes, int decodedOffset, int color) {
        putPixelsIntoByteArray(decodedBytes, decodedOffset, 1, color);
        return this.bytesPerPixelSignificant;
    }

    public void putPixelsIntoByteArray(byte[] decodedBytes, int decodedOffset, int rlength, int color) {
        int decodedOffset2;
        int decodedOffset3;
        byte r = (byte) ((color >> 16) & 255);
        byte g = (byte) ((color >> 8) & 255);
        byte b = (byte) (color & 255);
        if (3 != this.bytesPerPixelSignificant) {
            int newColor = ((this.pixelFormat.redMax & r) << this.pixelFormat.redShift) | ((this.pixelFormat.greenMax & g) << this.pixelFormat.greenShift) | ((this.pixelFormat.blueMax & b) << this.pixelFormat.blueShift);
            int rlength2 = rlength;
            decodedOffset2 = decodedOffset;
            while (true) {
                int rlength3 = rlength2 - 1;
                if (rlength2 <= 0) {
                    break;
                }
                if (2 == this.bytesPerPixelSignificant) {
                    decodedOffset3 = decodedOffset2 + 1;
                    decodedBytes[decodedOffset2] = (byte) ((newColor >> 8) & 255);
                } else {
                    decodedOffset3 = decodedOffset2;
                }
                decodedOffset2 = decodedOffset3 + 1;
                decodedBytes[decodedOffset3] = (byte) (newColor & 255);
                rlength2 = rlength3;
            }
        } else {
            while (true) {
                int rlength4 = rlength;
                decodedOffset2 = decodedOffset;
                rlength = rlength4 - 1;
                if (rlength4 <= 0) {
                    break;
                }
                int decodedOffset4 = decodedOffset2 + 1;
                decodedBytes[decodedOffset2] = b;
                int decodedOffset5 = decodedOffset4 + 1;
                decodedBytes[decodedOffset4] = g;
                decodedOffset = decodedOffset5 + 1;
                decodedBytes[decodedOffset5] = r;
            }
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void createCursor(int[] cursorPixels, FramebufferUpdateRectangle rect) throws TransportException {
        synchronized (this.cursor) {
            this.cursor.createCursor(cursorPixels, rect.x, rect.y, rect.width, rect.height);
        }
    }

    public void decodeCursorPosition(FramebufferUpdateRectangle rect) {
        synchronized (this.cursor) {
            this.cursor.updatePosition(rect.x, rect.y);
        }
    }
}
