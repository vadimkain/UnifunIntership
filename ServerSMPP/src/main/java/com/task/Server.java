package com.task;

import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.*;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.MessageIDGenerator;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();

    public static void main(String[] args) {
        int serverPort = 2775; // Замените на желаемый порт для сервера SMPP

        try {
            SMPPServerSessionListener sessionListener = new SMPPServerSessionListener(serverPort);
            System.out.println("SMPP server is running on port " + serverPort);
            sessionListener.start();
        } catch (Exception e) {
            System.out.println("Failed the start");
            e.printStackTrace();
        }
    }

    static class SMPPServerSessionListener {
        private final int port;
        private final MessageIDGenerator messageIDGenerator;
        private final AtomicInteger sessionCounter;

        public SMPPServerSessionListener(int port) {
            this.port = port;
            this.messageIDGenerator = new RandomMessageIDGenerator();
            this.sessionCounter = new AtomicInteger();
        }

        public void start() {
            try {
                // Создаем объект SMPPServerSessionListener для прослушивания указанного порта
                org.jsmpp.session.SMPPServerSessionListener serverSessionListener = new org.jsmpp.session.SMPPServerSessionListener(port);

                // Устанавливаем слушателя сообщений для серверной сессии
                serverSessionListener.setMessageReceiverListener(new ServerMessageReceiverListener() {

                    public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
                        return null;
                    }

                    // Метод onAcceptSubmitSm обрабатывает входящее SMS-сообщение
                    public SubmitSmResult onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession smppServerSession) throws ProcessRequestException {

                        try {

                            log.info("Received message from " + submitSm.getSourceAddr() + ": " + new String(submitSm.getShortMessage(), StandardCharsets.UTF_8));

                            // Здесь можно добавить обработку полученных сообщений
                            // Например, сохранение сообщения в базе данных или пересылка на другой номер

                            // В данном примере просто отправляем успешный ответ на клиентскую сессию
                            return new SubmitSmResult(
                                    new RandomMessageIDGenerator().newMessageId(),
                                    new OptionalParameter[]{}
                            );
                        } catch (Exception e) {
                            // Если возникли проблемы с обработкой сообщения, выбрасываем ProcessRequestException
                            throw new ProcessRequestException("Failed to process request: " + e.getMessage(), SMPPConstant.STAT_ESME_RSYSERR);
                        }
                    }

                    // Другие методы не используются в данной реализации, возвращаем null или ничего не делаем
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
                });

                // Устанавливаем слушатель изменения состояния сессии
                serverSessionListener.setSessionStateListener(new SessionStateListener() {
                    // Метод onStateChange вызывается при изменении состояния сессии
                    @Override
                    public void onStateChange(SessionState newState, SessionState oldState, Session source) {
                        log.info("Session state changed from " + oldState + " to " + newState);
                    }
                });

                // Бесконечный цикл для прослушивания новых клиентских подключений
                while (true) {
                    // Принимаем новую сессию от клиента
                    org.jsmpp.session.SMPPServerSession serverSession = serverSessionListener.accept();

                    // Ожидаем привязку (bind) от клиента в течение 10 секунд
                    BindRequest bindRequest = serverSession.waitForBind(10000L);

                    // Логируем информацию о привязке (bind) для данной сессии, включая идентификатор сессии и версию интерфейса
                    log.info("Accepting bind for session {}, interface version {}", serverSession.getSessionId(), bindRequest.getInterfaceVersion());

                    try {
                        // Принимаем привязку от клиента с указанием имени пользователя ("username") и версии интерфейса (IF_34)
                        bindRequest.accept("username", InterfaceVersion.IF_34);
                    } catch (PDUStringException e) {
                        // Если возникла ошибка при обработке привязки, логируем и отправляем отказ с кодом ошибки STAT_ESME_RSYSERR
                        log.error("PDU string exception", e);
                        bindRequest.reject(SMPPConstant.STAT_ESME_RSYSERR);
                    }

                    // Устанавливаем таймер для отправки запросов enquire_link (проверка активности соединения) каждые 10 секунд
                    serverSession.setEnquireLinkTimer(10000);

                    // Логируем информацию о новой принятой сессии и увеличиваем счетчик сессий
                    log.info("Accepted new SMPP session #" + sessionCounter.incrementAndGet());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
