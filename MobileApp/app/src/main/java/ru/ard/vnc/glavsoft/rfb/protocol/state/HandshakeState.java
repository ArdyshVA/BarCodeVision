package ru.ard.vnc.glavsoft.rfb.protocol.state;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedProtocolVersionException;
import ru.ard.vnc.glavsoft.rfb.protocol.ProtocolContext;

import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedProtocolVersionException;
import ru.ard.vnc.glavsoft.rfb.protocol.ProtocolContext;

public class HandshakeState extends ProtocolState {
    static final String PROTOCOL_STRING_3_3 = "RFB 003.003\n";
    static final String PROTOCOL_STRING_3_7 = "RFB 003.007\n";
    static final String PROTOCOL_STRING_3_8 = "RFB 003.008\n";
    private static final int PROTOCOL_STRING_LENGTH = 12;

    public HandshakeState(ProtocolContext context) {
        super(context);
    }

    public void handshake() throws TransportException, UnsupportedProtocolVersionException {
        try {
            String protocolName = this.reader.readString(12);
            this.logger.info("Protocol: " + protocolName);
            if (PROTOCOL_STRING_3_3.equals(protocolName)) {
                changeStateTo(new SecurityType33State(this.context));
                this.context.getSettings().setProtocolVersion("3.3");
            } else if (PROTOCOL_STRING_3_7.equals(protocolName)) {
                changeStateTo(new SecurityType37State(this.context));
                this.context.getSettings().setProtocolVersion("3.7");
            } else if (PROTOCOL_STRING_3_8.equals(protocolName)) {
                changeStateTo(new SecurityTypeState(this.context));
                this.context.getSettings().setProtocolVersion("3.8");
            } else {
                throw new UnsupportedProtocolVersionException("Unsupported protocol version: " + protocolName);
            }
            try {
                this.writer.write(protocolName);
                this.logger.info("Protocol version: " + this.context.getSettings().getProtocolVersion());
            } catch (Exception e) {
                throw new UnsupportedProtocolVersionException(null);
            }
        } catch (Exception e2) {
            throw new UnsupportedProtocolVersionException(null);
        }
    }
}
