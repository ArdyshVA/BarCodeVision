package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

public class ByteBuffer {
    static final /* synthetic */ boolean $assertionsDisabled = (!ByteBuffer.class.desiredAssertionStatus());
    private static ByteBuffer instance = new ByteBuffer();
    private byte[] buffer = new byte[0];

    private ByteBuffer() {
    }

    public static ByteBuffer getInstance() {
        return instance;
    }

    public void correctBufferCapacity(int length) {
        if (!$assertionsDisabled && this.buffer == null) {
            throw new AssertionError();
        } else if (this.buffer.length < length) {
            this.buffer = new byte[length];
        }
    }

    public byte[] getBuffer(int length) {
        correctBufferCapacity(length);
        return this.buffer;
    }
}
