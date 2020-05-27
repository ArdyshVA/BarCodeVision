package ru.ard.vnc.glavsoft.rfb.protocol;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;

import ru.ard.vnc.callback.VNCTransportListenerStopCallBack;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.rfb.client.ClientToServerMessage;
import ru.ard.vnc.glavsoft.transport.Transport;

public class SenderTask implements Runnable {
    private volatile boolean isRunning = false;
    private final MessageQueue queue;
    private final Transport.Writer writer;
    private VNCTransportListenerStopCallBack stopCallBack;

    public SenderTask(MessageQueue messageQueue, Transport.Writer writer2, VNCTransportListenerStopCallBack stopCallBack) {
        this.queue = messageQueue;
        this.writer = writer2;
        this.stopCallBack = stopCallBack;
    }

    public void run() {
        this.isRunning = true;
        while (this.isRunning) {
            try {
                ClientToServerMessage message = this.queue.get();
                if (message != null) {
                    message.send(this.writer);
                }
            } catch (InterruptedException e) {
            } catch (TransportException e2) {
                if (!(this.isRunning) /*|| this.sessionManagerRef == null || this.sessionManagerRef.get() == null)*/) {
//                    this.sessionManagerRef.get().stopTasksAndRunNewSession();
                }
                stop();
            } catch (Throwable te) {
                te.printStackTrace(new PrintWriter(new StringWriter()));
                if (!(this.isRunning)/*|| this.sessionManagerRef == null || this.sessionManagerRef.get() == null)*/) {
//                    this.sessionManagerRef.get().stopTasksAndRunNewSession();
                }
                stop();
            }
        }
        stopCallBack.reallyStop(SenderTask.class);
    }

    public void stop() {
        this.isRunning = false;
    }
}
