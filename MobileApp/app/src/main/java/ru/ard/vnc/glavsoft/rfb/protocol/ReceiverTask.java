package ru.ard.vnc.glavsoft.rfb.protocol;

import ru.ard.vnc.callback.VNCTransportListenerStopCallBack;
import ru.ard.vnc.glavsoft.exceptions.CommonException;
import ru.ard.vnc.glavsoft.exceptions.ProtocolException;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.rfb.ClipboardController;
import ru.ard.vnc.glavsoft.rfb.IRepaintController;
import ru.ard.vnc.glavsoft.rfb.client.FramebufferUpdateRequestMessage;
import ru.ard.vnc.glavsoft.rfb.encoding.EncodingType;
import ru.ard.vnc.glavsoft.rfb.encoding.PixelFormat;
import ru.ard.vnc.glavsoft.rfb.encoding.decoder.DecodersContainer;
import ru.ard.vnc.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import ru.ard.vnc.glavsoft.transport.Transport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class ReceiverTask implements Runnable {
    private static final byte BELL = 2;
    private static final byte FRAMEBUFFER_UPDATE = 0;
    private static final byte SERVER_CUT_TEXT = 3;
    private static final byte SET_COLOR_MAP_ENTRIES = 1;
    private static Logger logger = Logger.getLogger("com.glavsoft.rfb.protocol.ReceiverTask");
    private final ClipboardController clipboardController;
    private final DecodersContainer decoders;
    private FramebufferUpdateRequestMessage fullscreenFbUpdateIncrementalRequest;
    private volatile boolean isRunning = false;
    private final PixelFormat pixelFormat;
    private final MessageQueue queue;
    private final Transport.Reader reader;
    private final IRepaintController repaintController;
    private VNCTransportListenerStopCallBack stopCallBack;

    public ReceiverTask(
            Transport.Reader reader2,
            int fbWidth, int fbHeight,
            PixelFormat pixelFormat2, IRepaintController repaintController2,
            ClipboardController clipboardController2, DecodersContainer decoders2,
            MessageQueue messageQueue,
            VNCTransportListenerStopCallBack stopCallBack) {
        this.reader = reader2;
        this.repaintController = repaintController2;
        this.clipboardController = clipboardController2;
        this.pixelFormat = pixelFormat2;
        this.decoders = decoders2;
        this.queue = messageQueue;
        this.fullscreenFbUpdateIncrementalRequest = new FramebufferUpdateRequestMessage(0, 0, fbWidth, fbHeight, true);
        this.stopCallBack = stopCallBack;
    }

    public void run() {
        this.isRunning = true;
        while (this.isRunning) {
            try {
                byte messageId = this.reader.readByte();
                switch (messageId) {
                    case 0:
                        framebufferUpdateMessage();
                        break;
                    case 1:
                        logger.severe("Server message SetColorMapEntries is not implemented.");
                        break;
                    case 2:
                        logger.fine("Server message: Bell");
                        System.out.print("\u00007");
                        System.out.flush();
                        break;
                    case 3:
                        logger.fine("Server message: CutText (3)");
                        serverCutText();
                        break;
                    default:
                        logger.severe("Unsupported server message. Id = " + ((int) messageId));
                        break;
                }
            } catch (TransportException e) {
                if (!this.isRunning) {
//                    this.sessionManager.stopTasksAndRunNewSession();
                    stop();
                }

                stop();
            } catch (ProtocolException e2) {
                if (!this.isRunning) {
//                    this.sessionManager.stopTasksAndRunNewSession();
                    stop();
                }

                stop();
            } catch (CommonException e3) {
                if (!this.isRunning) {
//                    this.sessionManager.stopTasksAndRunNewSession();
                    stop();
                }
                stop();
            } catch (Throwable te) {
                te.printStackTrace(new PrintWriter(new StringWriter()));
                if (!this.isRunning) {
//                    this.sessionManager.stopTasksAndRunNewSession();
                    stop();
                }

                stop();
            }
        }
        stopCallBack.reallyStop(ReceiverTask.class);
    }

    private void serverCutText() throws TransportException {
        this.reader.readByte();
        this.reader.readInt16();
    }

    public void framebufferUpdateMessage() throws CommonException {
        FramebufferUpdateRectangle rect;
        this.reader.readByte();
        int numberOfRectangles = this.reader.readUInt16();
        while (true) {
            int numberOfRectangles2 = numberOfRectangles;
            numberOfRectangles = numberOfRectangles2 - 1;
            if (numberOfRectangles2 > 0) {
                rect = new FramebufferUpdateRectangle();
                rect.fill(this.reader);
                if (this.decoders.getDecoderByType(rect.getEncodingType()) != null) {
                    this.repaintController.repaintBitmap(rect);
                } else if (rect.getEncodingType() == EncodingType.RICH_CURSOR) {
                    this.repaintController.repaintCursor();
                } else if (rect.getEncodingType() == EncodingType.CURSOR_POS) {
                    this.repaintController.repaintCursor();
                } else if (rect.getEncodingType() != EncodingType.DESKTOP_SIZE) {
                    throw new CommonException("Unprocessed encoding: " + rect.toString());
                } else if (rect.width > 0 && rect.height > 0) {
                    this.fullscreenFbUpdateIncrementalRequest = new FramebufferUpdateRequestMessage(0, 0, rect.width, rect.height, true);
                    this.queue.put(new FramebufferUpdateRequestMessage(0, 0, rect.width, rect.height, false));
                }
            } else {
                this.queue.put(this.fullscreenFbUpdateIncrementalRequest);
                return;
            }
        }
//        throw new ProtocolException("Server sent wrong Desctop Size: one of new desctop size dimensions is less or equals to zero (" + rect.width + Operators.MULTIPLICATION + rect.height + ").");
    }

    public void stop() {
        this.isRunning = false;
    }
}
