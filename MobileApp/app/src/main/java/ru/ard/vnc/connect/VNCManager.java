package ru.ard.vnc.connect;

import ru.ard.vnc.callback.VNCConnectErrorCallBack;
import ru.ard.vnc.callback.VNCTransportListenerStopCallBack;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.rfb.KeyEventListener;
import ru.ard.vnc.glavsoft.rfb.client.KeyEventMessage;
import ru.ard.vnc.glavsoft.rfb.encoding.decoder.DecodersContainer;
import ru.ard.vnc.glavsoft.rfb.protocol.MessageQueue;
import ru.ard.vnc.glavsoft.rfb.protocol.Protocol;
import ru.ard.vnc.glavsoft.rfb.protocol.ProtocolSettings;
import ru.ard.vnc.glavsoft.rfb.protocol.ReceiverTask;
import ru.ard.vnc.glavsoft.rfb.protocol.SenderTask;
import ru.ard.vnc.glavsoft.transport.Transport;

import java.io.IOException;
import java.net.Socket;

public class VNCManager implements VNCTransportListenerStopCallBack {
//    private ReceiverTask receiverTask;
    private MessageQueue senderQueue;
    private SenderTask senderTask;
    private Protocol workingProtocol;
    private Socket workingSocket;

    private boolean isClosing = false;

    /**
     * В отдельном потоке настраивает соединение, возвращает статус работы: ошибка с расшифровкой или успех
     */
    public void reopenConnection(final String password, final String ip, final int port, final VNCConnectErrorCallBack errorCallBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                closeConnection();
                connect(password, ip, port, errorCallBack);
            }
        }).start();
    }

    private void connect(String password, String ip, int port, VNCConnectErrorCallBack errorCallBack) {
        try {
            DecodersContainer decoders = new DecodersContainer();
            ProtocolSettings settings = ProtocolSettings.getDefaultSettings();

            workingSocket = connectToHost(ip, port);
            if (workingSocket == null) {
                throw new Exception("CONNECTION ERROR: Socket is null");
            }
            decoders.initDecodersWhenNeeded(settings.encodings);
            Transport.Reader reader = new Transport.Reader(workingSocket.getInputStream());
            Transport.Writer writer = new Transport.Writer(workingSocket.getOutputStream());
            workingProtocol = new Protocol(reader, writer, password, settings);
            workingProtocol.handshake();
            workingProtocol.negotiateAboutSecurityType();
            workingProtocol.authenticate();
            //дикий костыль. Типо ждем когда сервер очухается и сможет нормально работать по протоколу
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            workingProtocol.clientAndServerInit(); // после перезапуска может выбрасывать ошибку

            //while (!initConnection(workingProtocol)) {} // делаем попытки восстановить общение с сервером. После переподключения он спевра на отвечает как нужно.

            workingProtocol.set32bppPixelFormat();
            settings.setPixelFormat(workingProtocol.getPixelFormat());
            senderQueue = new MessageQueue();
            workingProtocol.startNormalHandling(senderQueue);
            decoders.resetDecoders();
            senderTask = new SenderTask(senderQueue, writer, this);
            new Thread(senderTask).start();
//            receiverTask = new ReceiverTask(
//                    reader, workingProtocol.getFbWidth(),
//                    workingProtocol.getFbHeight(), workingProtocol.getPixelFormat(),
//                    null, null,
//                    decoders, senderQueue, this
//
//            );
//            new Thread(receiverTask).start();
        } catch (Exception e) {
            //отдаем ошибку на обработку обработчику
            errorCallBack.onConnectError(e);
        }
    }

    private boolean initConnection(Protocol workingProtocol) {
        try {
            workingProtocol.clientAndServerInit(); // после перезапуска может выбрасывать ошибку
        } catch (TransportException e) {
            e.getMessage();
            return false;
        }
        return true;
    }

    /**
     * В отдельном потоке производит отключение соединения
     */
    public void closeConnection() {
        isClosing = true;

        if (senderTask != null) {
            senderTask.stop();
        }
//        if (receiverTask != null) {
//            receiverTask.stop();
//        }

        while (senderTask != null /*|| receiverTask != null*/) {
            //ожидаем когда потоки закончат свою работу и сообщат об этом в коллбэк reallyStop
        }
        //теперь можно безопасно разрывать соединение
        if (workingSocket != null && workingSocket.isConnected()) {
            try {
                workingSocket.close();
            } catch (IOException e) {
            }
        }
        isClosing = false;
    }


    public void sendString(String str) throws Exception {
        for (int i = 0; i < str.length(); i++) {
            senderQueue.put(new KeyEventMessage(str.charAt(i), true));
            if (str.charAt(i) == ' ' /*|| str.charAt(i) == ' '*/) {
                senderQueue.put(new KeyEventMessage(33, true));
                senderQueue.put(new KeyEventMessage(KeyEventListener.K_BACK_SPACE, true));
            } else {
                senderQueue.put(new KeyEventMessage(32, true));
                senderQueue.put(new KeyEventMessage(KeyEventListener.K_BACK_SPACE, true));
            }
        }
    }

    private Socket connectToHost(String hostName, int portNumber) throws Exception {
        Socket socket = new Socket(hostName, portNumber);
        return socket;
    }

    public void sendCharTAB() throws Exception {
        senderQueue.put(new KeyEventMessage(KeyEventListener.K_TAB, true));
    }

    public void sendCharENTER() throws Exception {
        senderQueue.put(new KeyEventMessage(KeyEventListener.K_ENTER, true));
    }

    @Override
    public void reallyStop(Class c) {
        if (c == SenderTask.class) {
            senderTask = null;
            if (!isClosing) {
                //поток остановлен из-за ошибки, его надо пересоздать
                try {
                    Transport.Writer writer = new Transport.Writer(workingSocket.getOutputStream());
                    senderTask = new SenderTask(senderQueue, writer, this);
                    new Thread(senderTask).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//        if (c == ReceiverTask.class) {
//            receiverTask = null;
//            if (!isClosing) {
//                //поток остановлен из-за ошибки, его надо пересоздать
//                try {
//                    Transport.Reader reader = new Transport.Reader(workingSocket.getInputStream());
//                    DecodersContainer decoders = new DecodersContainer();
//                    receiverTask = new ReceiverTask(
//                            reader, workingProtocol.getFbWidth(),
//                            workingProtocol.getFbHeight(), workingProtocol.getPixelFormat(),
//                            null, null,
//                            decoders, senderQueue, this
//
//                    );
//                    new Thread(receiverTask).start();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        //в процессе останова были, значит процесс закончен, остановлены обы потока чтения/записи
        if (isClosing && senderTask == null /*&& receiverTask == null*/) {
            isClosing = false;
        }
    }
}
