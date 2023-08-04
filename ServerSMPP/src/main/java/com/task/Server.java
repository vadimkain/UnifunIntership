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
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: РЕАЛИЗОВАТЬ КЛАСС WaitBindTask implements Runnable ОБЯЗАТЕЛЬНО ДЛЯ БИЗНЕС-ЛОГИКИ И УБРАТЬ ЦИКЛ
// ПОТОМУ ЧТО ТУТ КОСТЫЛИ УЖАСНЫЕ
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

        public SmppServerSessionListener(int port) {
            this.port = port;
            this.sessionCounter = new AtomicInteger();
        }

        private void forwardShortMessage(SubmitSm submitSm) {
            try {
                // Получаем данные из запроса от первого клиента
                String sourceAddress = submitSm.getSourceAddr();
                String messageText = new String(submitSm.getShortMessage(), StandardCharsets.UTF_8);

                // Создаем новую сессию для второго клиента
                SMPPSession secondClientSession = new SMPPSession();
                BindParameter bindParam = new BindParameter(
                        BindType.BIND_TRX,   // Тип привязки (в данном случае двунаправленная передача на сервер)
                        "username2",         // Имя пользователя второго клиента
                        "pwrd",              // Пароль второго клиента
                        "cp",                // Системный тип (system_type) для подключения к серверу (может быть пустым)
                        TypeOfNumber.UNKNOWN,// Тип номера отправителя (в данном случае UNKNOWN)
                        NumberingPlanIndicator.INTERNET, // Индикатор плана нумерации отправителя (в данном случае INTERNET)
                        null                 // Список опциональных параметров (в данном случае не указываем)
                );

                // Подключаемся к серверу второго клиента и выполняем привязку (bind) с указанными параметрами
                secondClientSession.connectAndBind("192.168.89.47", 2776, bindParam);

                try {
                    // Отправляем сообщение второму клиенту
                    String destinationNumber = "79234567890"; // Номер второго клиента
                    SubmitSmResult messageId = secondClientSession.submitShortMessage(
                            "CMT",                            // Сервисный тип (service_type) сообщения
                            TypeOfNumber.ALPHANUMERIC,         // Тип номера отправителя (ALPHANUMERIC - алфавитно-цифровой)
                            NumberingPlanIndicator.UNKNOWN,    // Индикатор плана нумерации отправителя (в данном случае UNKNOWN)
                            "SENDER_SERVER",                  // Номер отправителя
                            TypeOfNumber.UNKNOWN,              // Тип номера получателя (в данном случае UNKNOWN)
                            NumberingPlanIndicator.UNKNOWN,    // Индикатор плана нумерации получателя (в данном случае UNKNOWN)
                            destinationNumber,                 // Номер получателя (адрес назначения сообщения)
                            new ESMClass(),                    // Класс ESM (Extended Short Message)
                            (byte) 0,                          // Время жизни сообщения
                            (byte) 1,                          // Код качества обслуживания
                            null,                              // Идентификатор сообщения (если null, будет сгенерирован автоматически)
                            null,                              // Код приложения
                            new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE), // Опции доставки сообщений
                            (byte) 0,                          // Язык данных сообщения
                            new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false), // Кодировка данных сообщения
                            (byte) 0,                          // Отложенная доставка
                            messageText.getBytes(StandardCharsets.UTF_8) // Байтовое представление текста сообщения
                    );

                    log.info("Message forwarded from " + sourceAddress + " to " + destinationNumber + " with Message ID: " + messageId);

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

                // Закрываем сессию второго клиента
                secondClientSession.unbindAndClose();

            } catch (IOException e) {
                log.error("Failed connect and bind to receiver " + e);
            }
        }

        private void messageReceiving(SMPPServerSessionListener serverSessionListener) {
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

                        forwardShortMessage(submitSm);

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
        }

        private void sessionStateListening(SMPPServerSessionListener serverSessionListener) {
            // Устанавливаем слушатель изменения состояния сессии
            serverSessionListener.setSessionStateListener(new SessionStateListener() {
                // Метод onStateChange вызывается при изменении состояния сессии
                @Override
                public void onStateChange(SessionState newState, SessionState oldState, Session source) {
                    log.info("Session state changed from " + oldState + " to " + newState);
                }
            });
        }

        private void waitingForConnections(SMPPServerSessionListener serverSessionListener) throws IOException, TimeoutException {
            // Бесконечный цикл для прослушивания новых клиентских подключений
            while (true) {
                // Принимаем новую сессию от клиента
                SMPPServerSession serverSession = serverSessionListener.accept();

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
        }

        public void start() {
            try {
                // Создаем объект SMPPServerSessionListener для прослушивания указанного порта
                SMPPServerSessionListener serverSessionListener = new SMPPServerSessionListener(port);

                messageReceiving(serverSessionListener);

                sessionStateListening(serverSessionListener);

                waitingForConnections(serverSessionListener);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
