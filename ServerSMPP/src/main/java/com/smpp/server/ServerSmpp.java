package com.smpp.server;

import org.jsmpp.PDUStringException;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ServerSmpp implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ServerSmpp.class);
    private SMPPServerSessionListener serverSessionListener;
    private final ServerSmppConfiguration _CONFIGURATION;
    public static boolean serverIsRunning = true;
    private SMPPSession clientSession = new SMPPSession();
    private SMPPServerSession smppServerSession;

    public ServerSmpp(ServerSmppConfiguration configuration) {
        _CONFIGURATION = configuration;
    }

    @Override
    public void run() {
        try {
            this.serverSessionListener = new SMPPServerSessionListener(this._CONFIGURATION.getServerPort());

            log.info("Server started:\n" +
                     "id = {}\n" +
                     "serverName = {}\n" +
                     "serverPort = {}\n" +
                     "waitBind = {}\n" +
                     "poolSize = {}\n" +
                     "timeout = {}\n" +
                     "nextPartWaiting = {}\n" +
                     "processorDegree = {}\n" +
                     "interfaceVersion = {}\n" +
                     "serviceType = {}\n" +
                     "concatinateType = {}\n" +
                     "sendDLRPerSec = {}",
                    _CONFIGURATION.getId(),
                    _CONFIGURATION.getServerName(),
                    _CONFIGURATION.getServerPort(),
                    _CONFIGURATION.getWaitBind(),
                    _CONFIGURATION.getPoolSize(),
                    _CONFIGURATION.getTimeout(),
                    _CONFIGURATION.getNextPartWaiting(),
                    _CONFIGURATION.getProcessorDegree(),
                    _CONFIGURATION.getInterfaceVersion(),
                    _CONFIGURATION.getServiceType(),
                    _CONFIGURATION.getConcatinateType(),
                    _CONFIGURATION.getSendDLRPerSec()
            );

            this.serverSessionListener.setMessageReceiverListener(new ForServerMessageReceiverListener(this.clientSession));
            this.clientSession.setMessageReceiverListener(new ForDestinationClientMessageReceiverListener(this.smppServerSession));
            this.serverSessionListener.setTimeout(6000000);
            this.clientSession.setTransactionTimer(600000L);

            while (ServerSmpp.serverIsRunning) {
                this.smppServerSession = serverSessionListener.accept();

                BindRequest bindRequest = smppServerSession.waitForBind(_CONFIGURATION.getTimeout());

                this.clientSession.connectAndBind(
                        "192.168.89.181",
                        2776,
                        new BindParameter(
                                bindRequest.getBindType(),
                                bindRequest.getSystemId(),
                                bindRequest.getPassword(),
                                bindRequest.getSystemType(),
                                TypeOfNumber.INTERNATIONAL,
                                NumberingPlanIndicator.UNKNOWN,
                                null
                        )
                );

                log.info(
                        "Started connection for to {} from IP {}:\n" +
                        "systemId: {}\n" +
                        "systemType: {}\n" +
                        "NP: {}\n" +
                        "TON: {}\n" +
                        "interfaceVersion: {}",
                        _CONFIGURATION.getServerName(), smppServerSession.getInetAddress().getHostAddress(),
                        bindRequest.getSystemId(),
                        bindRequest.getSystemType(),
                        bindRequest.getAddrNpi(),
                        bindRequest.getAddrTon(),
                        bindRequest.getInterfaceVersion()
                );

                switch (bindRequest.getBindType()) {
                    case BIND_RX:
                    case BIND_TX:
                    case BIND_TRX:
                        try {
                            bindRequest.accept(bindRequest.getSystemId(), bindRequest.getInterfaceVersion());
                            log.info("Accepting bind for session {}, interface version {}", smppServerSession.getSessionId(), bindRequest.getInterfaceVersion());

                        } catch (PDUStringException e) {
                            log.error("Failed to create session {}", e);
                        }
                        break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
