package ru.ard.vnc.glavsoft.rfb.protocol.state;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.rfb.encoding.ServerInitMessage;
import ru.ard.vnc.glavsoft.rfb.protocol.ProtocolContext;
import ru.ard.vnc.glavsoft.rfb.protocol.ProtocolSettings;

public class InitTightState extends InitState {
    public InitTightState(ProtocolContext context) {
        super(context);
    }

    public ServerInitMessage clientAndServerInit() throws TransportException {
        ServerInitMessage serverInitMessage = getServerInitMessage();
        int nServerMessageTypes = this.reader.readUInt16();
        int nClientMessageTypes = this.reader.readUInt16();
        int nEncodingTypes = this.reader.readUInt16();
        this.reader.readUInt16();
        ProtocolSettings settings = this.context.getSettings();
        settings.serverMessagesCapabilities.read(this.reader, nServerMessageTypes);
        settings.clientMessagesCapabilities.read(this.reader, nClientMessageTypes);
        settings.encodingTypesCapabilities.read(this.reader, nEncodingTypes);
        return serverInitMessage;
    }
}
