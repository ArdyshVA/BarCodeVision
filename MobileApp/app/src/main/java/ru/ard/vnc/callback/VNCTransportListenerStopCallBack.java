package ru.ard.vnc.callback;

public interface VNCTransportListenerStopCallBack {

    /**
     * сообщает о том что поток, работающий с трансопртным протоколом реально закончил свою работу
     */
    void reallyStop(Class c);
}
