package com.smpp.server.session;

import com.smpp.server.ServerSmpp;
import com.smpp.server.ServerSmppConfiguration;
import lombok.Getter;
import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.session.BindRequest;
import org.jsmpp.session.SMPPServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class SmppWaitBindTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ServerSmpp.class);
    private final ServerSmppConfiguration configuration;
    private final SMPPServerSession smppServerSession;
    @Getter
    private static ConcurrentHashMap<String, String> activeSessions = new ConcurrentHashMap<>();

    public SmppWaitBindTask(ServerSmppConfiguration configuration, SMPPServerSession smppServerSession) {
        this.configuration = configuration;
        this.smppServerSession = smppServerSession;
    }

    @Override
    public void run() {
        try {
            BindRequest bindRequest = smppServerSession.waitForBind(configuration.getTimeout());

            log.info(
                    "Started connection for to {} from IP {}:\n" +
                    "systemId: {}\n" +
                    "systemType: {}\n" +
                    "NP: {}\n" +
                    "TON: {}\n" +
                    "interfaceVersion: {}",
                    configuration.getServerName(),
                    smppServerSession.getInetAddress().getHostAddress(),
                    bindRequest.getSystemId(),
                    bindRequest.getSystemType(),
                    bindRequest.getAddrNpi(),
                    bindRequest.getAddrTon(),
                    bindRequest.getInterfaceVersion()
            );

            switch (bindRequest.getBindType()) {
                case BIND_RX:
                case BIND_TX:
                    bindRequest.reject(SMPPConstant.STAT_ESME_RBINDFAIL);
                    break;
                case BIND_TRX:
//                    здесь должна была быть валидация бинд запроса

//                    пишем логику
                    try {
                        bindRequest.accept(bindRequest.getSystemId(), bindRequest.getInterfaceVersion());
                        activeSessions.put(smppServerSession.getSessionId(), bindRequest.getSystemId());

                        // Логируем информацию о привязке (bind) для данной сессии, включая идентификатор сессии и версию интерфейса
                        log.info("Accepting bind for session {}, interface version {}", smppServerSession.getSessionId(), bindRequest.getInterfaceVersion());

                    } catch (PDUStringException e) {
                        log.error("Failed to create session", e);
                    }
                    break;
            }

        } catch (TimeoutException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.warn(
                    "Failed accepting bind request for session: {} from ip - {}; IOException info: {}",
                    smppServerSession.getSessionId(),
                    smppServerSession.getInetAddress().getHostAddress(),
                    e
            );
        }
    }
}
