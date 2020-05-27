package ru.ard.vnc.glavsoft.transport;

import ru.ard.vnc.glavsoft.exceptions.ClosedConnectionException;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import com.google.android.gms.common.api.Api;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class Transport {
    private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    /* access modifiers changed from: private */
    public static final Logger logger = Logger.getLogger("com.glavsoft.transport");

    public static class Reader {
        private DataInputStream is;

        public Reader(InputStream is2) {
            this.is = new DataInputStream(new BufferedInputStream(is2));
        }

        public byte readByte() throws TransportException {
            try {
                return this.is.readByte();
            } catch (EOFException e) {
                ClosedConnectionException closedConnectionException = new ClosedConnectionException(e);
                Transport.logger.throwing(getClass().getName(), "public byte readByte()", closedConnectionException);
                throw closedConnectionException;
            } catch (IOException e2) {
                TransportException transportException = new TransportException("Cannot read byte", e2);
                Transport.logger.throwing(getClass().getName(), "public byte readByte()", transportException);
                throw transportException;
            }
        }

        public int readUInt8() throws TransportException {
            return readByte() & 255;
        }

        public int readUInt16() throws TransportException {
            return readInt16() & 65535;
        }

        public short readInt16() throws TransportException {
            try {
                return this.is.readShort();
            } catch (EOFException e) {
                ClosedConnectionException closedConnectionException = new ClosedConnectionException(e);
                Transport.logger.throwing(getClass().getName(), "public short readInt16()", closedConnectionException);
                throw closedConnectionException;
            } catch (IOException e2) {
                TransportException transportException = new TransportException("Cannot read int16", e2);
                Transport.logger.throwing(getClass().getName(), "public short readInt16()", transportException);
                throw transportException;
            }
        }

        public long readUInt32() throws TransportException {
            return ((long) readInt32()) & 4294967295L;
        }

        public int readInt32() throws TransportException {
            try {
                return this.is.readInt();
            } catch (EOFException e) {
                ClosedConnectionException closedConnectionException = new ClosedConnectionException(e);
                Transport.logger.throwing(getClass().getName(), "public int readInt32()", closedConnectionException);
                throw closedConnectionException;
            } catch (IOException e2) {
                TransportException transportException = new TransportException("Cannot read int16", e2);
                Transport.logger.throwing(getClass().getName(), "public int readInt32()", transportException);
                throw transportException;
            }
        }

        public String readString(int length) throws Exception {
            return new String(readBytes(length), "ISO_8859_1");
        }

        public String readString() throws Exception {
            return readString(readInt32() & /*Api.BaseClientBuilder.API_PRIORITY_OTHER*/2147483647);
        }

        public byte[] readBytes(int length) throws TransportException {
            return readBytes(new byte[length], 0, length);
        }

        public byte[] readBytes(byte[] b, int offset, int length) throws TransportException {
            try {
                this.is.readFully(b, offset, length);
                return b;
            } catch (EOFException e) {
                ClosedConnectionException closedConnectionException = new ClosedConnectionException(e);
                Transport.logger.throwing(getClass().getName(), "public byte[] readBytes(int length)", closedConnectionException);
                throw closedConnectionException;
            } catch (IOException e2) {
                TransportException transportException = new TransportException("Cannot read " + length + " bytes array", e2);
                Transport.logger.throwing(getClass().getName(), "public byte[] readBytes(int length)", transportException);
                throw transportException;
            }
        }
    }

    public static class Writer {
        private DataOutputStream os;

        public Writer(OutputStream os2) {
            this.os = new DataOutputStream(os2);
        }

        public void flush() throws TransportException {
            try {
                this.os.flush();
            } catch (IOException e) {
                TransportException transportException = new TransportException("Cannot flush output stream", e);
                Transport.logger.throwing(getClass().getName(), "public void flush()", transportException);
                throw transportException;
            }
        }

        public void writeByte(int b) throws TransportException {
            write((byte) (b & 255));
        }

        public void write(byte b) throws TransportException {
            try {
                this.os.writeByte(b);
            } catch (IOException e) {
                TransportException transportException = new TransportException("Cannot write byte", e);
                Transport.logger.throwing(getClass().getName(), "public void write(byte b)", transportException);
                throw transportException;
            }
        }

        public void writeInt16(int sh) throws TransportException {
            write((short) (65535 & sh));
        }

        public void write(short sh) throws TransportException {
            try {
                this.os.writeShort(sh);
            } catch (IOException e) {
                TransportException transportException = new TransportException("Cannot write short", e);
                Transport.logger.throwing(getClass().getName(), "public void write(short sh)", transportException);
                throw transportException;
            }
        }

        public void writeInt32(int i) throws TransportException {
            write(i);
        }

        public void write(int i) throws TransportException {
            try {
                this.os.writeInt(i);
            } catch (IOException e) {
                TransportException transportException = new TransportException("Cannot write int", e);
                Transport.logger.throwing(getClass().getName(), "public void write(int i)", transportException);
                throw transportException;
            }
        }

        public void write(byte[] b) throws TransportException {
            write(b, 0, b.length);
        }

        public void write(String s) throws Exception {
            write(s.getBytes("ISO_8859_1"));
        }

        public void write(byte[] b, int length) throws TransportException {
            write(b, 0, length);
        }

        public void write(byte[] b, int offset, int length) throws TransportException {
            try {
                this.os.write(b, offset, length <= b.length ? length : b.length);
            } catch (IOException e) {
                TransportException transportException = new TransportException("Cannot write " + length + " bytes", e);
                Transport.logger.throwing(getClass().getName(), "public void write(byte[] b, int offset, int length)", transportException);
                throw transportException;
            }
        }
    }
}
