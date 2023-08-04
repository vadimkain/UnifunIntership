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
import org.jsmpp.util.RandomMessageIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        int serverPort = 2775;

        try {
            SmppServerSessionListener sessionListener = new SmppServerSessionListener(serverPort);
            System.out.println("SMPP server is running on port " + serverPort);
            sessionListener.start();
        } catch (Exception e) {
            System.out.println("Failed the start");
            e.printStackTrace();
        }
    }

    static class SmppServerSessionListener {
        private final int port;
        private final AtomicInteger sessionCounter;

//        private Map<String, SMPPSession> clientSessions = new ConcurrentHashMap<>();
//
//        // Метод для добавления клиентской сессии в реестр
//        public void addClientSession(String phoneNumber, SMPPSession session) {
//            clientSessions.put(phoneNumber, session);
//        }
//
//        // Метод для получения клиентской сессии из реестра
//        public SMPPSession getClientSession(String phoneNumber) {
//            return clientSessions.get(phoneNumber);
//        }


        public SmppServerSessionListener(int port) {
            this.port = port;
            this.sessionCounter = new AtomicInteger();
        }

        public void start() {
            try {
                // Создаем объект SMPPServerSessionListener для прослушивания указанного порта
                SMPPServerSessionListener serverSessionListener = new SMPPServerSessionListener(port);

                // Устанавливаем слушателя сообщений для серверной сессии
                serverSessionListener.setMessageReceiverListener(new ServerMessageReceiverListener() {

                    public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
                        return null;
                    }

                    // Метод onAcceptSubmitSm обрабатывает входящее SMS-сообщение
                    public SubmitSmResult onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession smppServerSession) throws ProcessRequestException {
                        // Здесь можно добавить обработку полученных сообщений
                        // Например, сохранение сообщения в базе данных или пересылка на другой номер

                        try {

                            log.info("Received message from " + submitSm.getSourceAddr() + ": " + new String(submitSm.getShortMessage(), StandardCharsets.UTF_8));

                            try {

                                // Получаем данные из запроса от первого клиента
                                String sourceAddress = submitSm.getSourceAddr();
                                String messageText = new String(submitSm.getShortMessage(), StandardCharsets.UTF_8);

                                // Создаем новую сессию для второго клиента
                                SMPPSession secondClientSession = new SMPPSession();
                                BindParameter bindParam = new BindParameter(
                                        BindType.BIND_RX, // или другой тип привязки
                                        "username2",       // Имя пользователя второго клиента
                                        "pwrd",            // Пароль второго клиента
                                        "cp",
                                        TypeOfNumber.UNKNOWN,
                                        NumberingPlanIndicator.INTERNET,
                                        null
                                );

                                secondClientSession.connectAndBind("192.168.89.47", 2776, bindParam);

                                try {

                                    // Отправляем сообщение второму клиенту
                                    String destinationNumber = "79234567890"; // Номер второго клиента
                                    SubmitSmResult messageId = secondClientSession.submitShortMessage(
                                            "CMT",
                                            TypeOfNumber.ALPHANUMERIC,
                                            NumberingPlanIndicator.UNKNOWN,
                                            "SENDER_SERVER",
                                            TypeOfNumber.UNKNOWN,
                                            NumberingPlanIndicator.UNKNOWN,
                                            destinationNumber,
                                            new ESMClass(),
                                            (byte) 0,
                                            (byte) 1,
                                            null,
                                            null,
                                            new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE),
                                            (byte) 0,
                                            new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false),
                                            (byte) 0,
                                            messageText.getBytes(StandardCharsets.UTF_8)
                                    );

                                    log.info("Message forwarded from " + sourceAddress + " to " + destinationNumber + " with Message ID: " + messageId);

                                } catch (PDUException e) {
                                    log.error("Invalid PRU parameter " + e);
                                } catch (ResponseTimeoutException e) {
                                    log.error("Response tiдmeout " + e);
                                } catch (InvalidResponseException e) {
                                    log.error("Receive invalid response " + e);
                                } catch (NegativeResponseException e) {
                                    log.error("Receive negative response " + e);
                                } catch (IOException e) {
                                    log.error("IO error occured " + e);
                                }

                                // Закрываем сессию второго клиента
                                secondClientSession.unbindAndClose();

                            } catch (IOException e) {
                                log.error("Failed connect and bind to receiver " + e);
                            }

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

//                TODO: РЕАЛИЗОВАТЬ КЛАСС WaitBindTask implements Runnable ОБЯЗАТЕЛЬНО ДЛЯ БИЗНЕС-ЛОГИКИ И УБРАТЬ ЦИКЛ
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
