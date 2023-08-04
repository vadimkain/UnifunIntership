package com.task;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.*;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
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

        private void sendDeliver(SubmitSm submitSm, SMPPServerSession smppServerSession) {
            try {
                log.info("DELIVER has been successfully sent from {} to {}", submitSm.getSourceAddr(), "79123456789");
                smppServerSession.deliverShortMessage(
                        "CMT",              // Сервисный тип (service_type) сообщения (в данном случае "CMT")
                        TypeOfNumber.ALPHANUMERIC,     // Тип номера отправителя (ALPHANUMERIC - алфавитно-цифровой)
                        NumberingPlanIndicator.UNKNOWN,// Индикатор плана нумерации отправителя (в данном случае UNKNOWN)
                        "SENDER_DELIVER_RC",           // Номер отправителя (может быть алфавитно-цифровым или числовым)
                        TypeOfNumber.UNKNOWN,          // Тип номера получателя (в данном случае UNKNOWN)
                        NumberingPlanIndicator.UNKNOWN,// Индикатор плана нумерации получателя (в данном случае UNKNOWN)
                        "79123456789",                 // Номер получателя (адрес назначения сообщения)
                        new ESMClass(),                // Класс ESM (Extended Short Message)
                        (byte) 0,                      // Время жизни сообщения (в данном случае 0 - без ограничения)
                        (byte) 1,                      // Код качества обслуживания (в данном случае 1 - не проверять)
                        null,                          // Идентификатор сообщения (если null, будет сгенерирован автоматически)
                        null,                          // Код приложения
                        "DELIVER FROM RECEIVER_CLIENT".getBytes()
                );


            } catch (PDUException e) {
                log.error("Invalid PDU parameter " + e);
            } catch (ResponseTimeoutException e) {
                log.error("Response timeout " + e);
            } catch (InvalidResponseException e) {
                log.error("Receive invalid response " + e);
            } catch (NegativeResponseException e) {
                log.error("Receive negative response " + e);
            } catch (IOException e) {
                log.error("IO error occurred " + e);
            }

        }

        private void messageReceiving(SMPPServerSessionListener clientSessionListener) {
            clientSessionListener.setMessageReceiverListener(new ServerMessageReceiverListener() {
                public SubmitSmResult onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession smppServerSession) throws ProcessRequestException {

                    try {
                        log.info("Received message from " + submitSm.getSourceAddr() + ": " + new String(submitSm.getShortMessage(), "UTF-8"));

                        sendDeliver(submitSm, smppServerSession);

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

        }

        private void sessionStateListening(SMPPServerSessionListener clientSessionListener) {
            clientSessionListener.setSessionStateListener(new SessionStateListener() {
                public void onStateChange(SessionState sessionState, SessionState sessionState1, Session session) {
                    log.info("Session state changed from " + sessionState1 + " to " + sessionState);
                }
            });
        }

        private void waitingForConnections(SMPPServerSessionListener clientSessionListener) throws IOException, TimeoutException {
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
        }

        public void start() {
            try {
                SMPPServerSessionListener clientSessionListener = new SMPPServerSessionListener(port);

                messageReceiving(clientSessionListener);

                sessionStateListening(clientSessionListener);

                waitingForConnections(clientSessionListener);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
