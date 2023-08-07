package com.task;

import org.jsmpp.extra.SessionState;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.Session;
import org.jsmpp.session.SessionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class SessionStateListenerImpl implements SessionStateListener {
    private static final Logger log = LoggerFactory.getLogger(ServerSmpp.class);
    private final ConcurrentHashMap<String, SMPPServerSession> serverSessionConcurrentHashMap;

    public SessionStateListenerImpl(ConcurrentHashMap<String, SMPPServerSession> serverSessionConcurrentHashMap) {
        this.serverSessionConcurrentHashMap = serverSessionConcurrentHashMap;
    }

    @Override
    public void onStateChange(SessionState sessionState, SessionState sessionState1, Session session) {
        SMPPServerSession serverSession = (SMPPServerSession) session;
        log.info("Session state changed from {} to {} with id {}", sessionState1, sessionState, serverSession.getSessionId());

        switch (sessionState) {
            case CLOSED:
                serverSessionConcurrentHashMap.remove(serverSession.getSessionId());
                log.info("Clear connection info: {}", serverSessionConcurrentHashMap.containsKey(serverSession.getSessionId()) ? "failed" : "true");
//                String systemId = SmppWaitBindTask.activeSession.get(session.getSessionId());
//                logger.info("SMPP client with systemId " + systemId + " disconnected");
                break;
            case BOUND_RX:
            case BOUND_TRX:
            case BOUND_TX:
                serverSessionConcurrentHashMap.put(session.getSessionId(), serverSession);
                break;
            default:
                log.info("Modification of Session {}; State - {} (old state - {})", serverSession.getSessionId(), sessionState, sessionState1);
                break;

        }
    }
}
