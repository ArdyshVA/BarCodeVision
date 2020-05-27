package ru.ard.vnc.glavsoft.rfb.protocol.state;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.rfb.encoding.ServerInitMessage;
import ru.ard.vnc.glavsoft.rfb.protocol.ProtocolContext;

public class InitState extends ProtocolState {
    public InitState(ProtocolContext context) {
        super(context);
    }

    public ServerInitMessage clientAndServerInit() throws TransportException {
        ServerInitMessage serverInitMessage = getServerInitMessage();
        this.context.getSettings().enableAllEncodingCaps();
        return serverInitMessage;
    }

    /* access modifiers changed from: protected */
    public ServerInitMessage getServerInitMessage() throws TransportException {
        this.writer.write(this.context.getSettings().getSharedFlag());
        try {
            return new ServerInitMessage(this.reader);
        } catch (Exception e) {
            throw new TransportException(null);
        }
    }
}
