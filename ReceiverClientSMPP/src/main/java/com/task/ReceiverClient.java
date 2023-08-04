package com.task;

import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.*;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

;

public class ReceiverClient {

    private static final Logger log = LoggerFactory.getLogger(ReceiverClient.class);
    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();

    public static void main(String[] args) {
        int serverPort = 2776;

        try {
            SmppClientSessionListener clientSessionListener = new SmppClientSessionListener(serverPort);
            log.info("SMPP server is running on port " + serverPort);
            clientSessionListener.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class SmppClientSessionListener {
        private final int port;
        private final AtomicInteger sessionCounter;

        SmppClientSessionListener(int port) {
            this.port = port;
            this.sessionCounter = new AtomicInteger();
        }

        public void start() {
            try {
                SMPPServerSessionListener clientSessionListener = new SMPPServerSessionListener(port);

                clientSessionListener.setMessageReceiverListener(new ServerMessageReceiverListener() {
                    public SubmitSmResult onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession smppServerSession) throws ProcessRequestException {

                        try {
                            log.info("Received message from " + submitSm.getSourceAddr() + ": " + new String(submitSm.getShortMessage(), "UTF-8"));

                            return new SubmitSmResult(new RandomMessageIDGenerator().newMessageId(), new OptionalParameter[]{});
                        } catch (Exception e) {
                            throw new ProcessRequestException("Failed to process request: " + e.getMessage(), SMPPConstant.STAT_ESME_RSYSERR);
                        }
                    }

                    public SubmitMultiResult onAcceptSubmitMulti(SubmitMulti submitMulti, SMPPServerSession smppServerSession) throws ProcessRequestException {
                        return null;
                    }

                    public QuerySmResult onAcceptQuerySm(QuerySm querySm, SMPPServerSession smppServerSession) throws ProcessRequestException {
                        return null;
                    }

                    public void onAcceptReplaceSm(ReplaceSm replaceSm, SMPPServerSession smppServerSession) throws ProcessRequestException {

                    }

                    public void onAcceptCancelSm(CancelSm cancelSm, SMPPServerSession smppServerSession) throws ProcessRequestException {

                    }

                    public BroadcastSmResult onAcceptBroadcastSm(BroadcastSm broadcastSm, SMPPServerSession smppServerSession) throws ProcessRequestException {
                        return null;
                    }

                    public void onAcceptCancelBroadcastSm(CancelBroadcastSm cancelBroadcastSm, SMPPServerSession smppServerSession) throws ProcessRequestException {

                    }

                    public QueryBroadcastSmResult onAcceptQueryBroadcastSm(QueryBroadcastSm queryBroadcastSm, SMPPServerSession smppServerSession) throws ProcessRequestException {
                        return null;
                    }

                    public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
                        return null;
                    }
                });

                clientSessionListener.setSessionStateListener(new SessionStateListener() {
                    public void onStateChange(SessionState sessionState, SessionState sessionState1, Session session) {
                        log.info("Session state changed from " + sessionState1 + " to " + sessionState);
                    }
                });

                while (true) {
                    SMPPServerSession clientSession = clientSessionListener.accept();
                    BindRequest bindRequest = clientSession.waitForBind(10000L);
                    log.info("Accepting bind for session {}, interface version {}", clientSession.getSessionId(), bindRequest.getInterfaceVersion());

                    try {
                        bindRequest.accept("username2", InterfaceVersion.IF_34);
                    } catch (PDUStringException e) {
                        log.error("PDU string exception", e);
                        bindRequest.reject(SMPPConstant.STAT_ESME_RSYSERR);
                    }

                    clientSession.setEnquireLinkTimer(10000);

                    log.info("Accepted new SMPP session #" + sessionCounter.incrementAndGet());

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
