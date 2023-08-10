package com.smpp.clientDestination;

import org.jsmpp.PDUStringException;
import org.jsmpp.session.BindRequest;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.SMPPServerSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ClientDestinationSmpp {
    private static final Logger log = LoggerFactory.getLogger(ClientDestinationSmpp.class);
    private SMPPServerSessionListener serverSessionListener;
    private SMPPServerSession smppServerSession;
    private final int _PORT = 2776;

    public void run() {
        try {
            this.serverSessionListener = new SMPPServerSessionListener(_PORT);

            log.info("Client B started at port {}", _PORT);

            this.serverSessionListener.setMessageReceiverListener(new ServerMessageReceiverListenerImpl());

            this.smppServerSession = serverSessionListener.accept();

            try {
                BindRequest bindRequest = smppServerSession.waitForBind(30000);

                bindRequest.accept(bindRequest.getSystemId());

            } catch (TimeoutException e) {
                log.error(e.getMessage());
            } catch (PDUStringException e) {
                log.error(e.getMessage());
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
