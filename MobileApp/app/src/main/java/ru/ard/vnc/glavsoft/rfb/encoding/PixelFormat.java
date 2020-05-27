package ru.ard.vnc.glavsoft.rfb.encoding;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.transport.Transport;
import org.apache.poi.ss.formula.ptg.UnionPtg;

public class PixelFormat {
    public byte bigEndianFlag;
    public byte bitsPerPixel;
    public short blueMax;
    public byte blueShift;
    public byte depth;
    public short greenMax;
    public byte greenShift;
    public short redMax;
    public byte redShift;
    public byte trueColourFlag;

    public void fill(Transport.Reader reader) throws TransportException {
        this.bitsPerPixel = reader.readByte();
        this.depth = reader.readByte();
        this.bigEndianFlag = reader.readByte();
        this.trueColourFlag = reader.readByte();
        this.redMax = reader.readInt16();
        this.greenMax = reader.readInt16();
        this.blueMax = reader.readInt16();
        this.redShift = reader.readByte();
        this.greenShift = reader.readByte();
        this.blueShift = reader.readByte();
        reader.readBytes(3);
    }

    public void send(Transport.Writer writer) throws TransportException {
        writer.write(this.bitsPerPixel);
        writer.write(this.depth);
        writer.write(this.bigEndianFlag);
        writer.write(this.trueColourFlag);
        writer.write(this.redMax);
        writer.write(this.greenMax);
        writer.write(this.blueMax);
        writer.write(this.redShift);
        writer.write(this.greenShift);
        writer.write(this.blueShift);
        writer.writeInt16(0);
        writer.writeByte(0);
    }

    public static PixelFormat set32bppPixelFormat(PixelFormat pixelFormat) {
        pixelFormat.bigEndianFlag = 0;
        pixelFormat.bitsPerPixel = 32;
        pixelFormat.blueMax = 255;
        pixelFormat.blueShift = 0;
        pixelFormat.greenMax = 255;
        pixelFormat.greenShift = 8;
        pixelFormat.redShift = UnionPtg.sid;
        pixelFormat.redMax = 255;
        pixelFormat.depth = 24;
        pixelFormat.trueColourFlag = 1;
        return pixelFormat;
    }

    public static PixelFormat create32bppPixelFormat() {
        return set32bppPixelFormat(new PixelFormat());
    }

    public String toString() {
        return "PixelFormat: [bits-per-pixel: " + String.valueOf((int) (this.bitsPerPixel & 255)) + ", depth: " + String.valueOf((int) (this.depth & 255)) + ", big-endian-flag: " + String.valueOf((int) (this.bigEndianFlag & 255)) + ", true-color-flag: " + String.valueOf((int) (this.trueColourFlag & 255)) + ", red-max: " + String.valueOf((int) (this.redMax & 65535)) + ", green-max: " + String.valueOf((int) (this.greenMax & 65535)) + ", blue-max: " + String.valueOf((int) (this.blueMax & 65535)) + ", red-shift: " + String.valueOf((int) (this.redShift & 255)) + ", green-shift: " + String.valueOf((int) (this.greenShift & 255)) + ", blue-shift: " + String.valueOf((int) (this.blueShift & 255)) + "]";
    }
}
