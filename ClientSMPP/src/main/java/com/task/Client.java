package com.task;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.*;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);
    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();

    private static void messageReceiverListening(SMPPSession session) {
        session.setMessageReceiverListener(new MessageReceiverListener() {
            @Override
            public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {

            }

            @Override
            public void onAcceptAlertNotification(AlertNotification alertNotification) {

            }

            @Override
            public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
                return null;
            }
        });
    }

    private static void sendShortMessage(SMPPSession session) {
        // В этом мы отправляем SMS-сообщения с помощью метода submitShortMessage

        try {
            String destinationNumber = "79123456789"; // Номер получателя
            String message = "Test message"; // Текст сообщения
            SubmitSmResult submitSmResult = session.submitShortMessage(
                    "CMT",              // Сервисный тип (service_type) сообщения (в данном случае "CMT")
                    TypeOfNumber.ALPHANUMERIC,     // Тип номера отправителя (ALPHANUMERIC - алфавитно-цифровой)
                    NumberingPlanIndicator.UNKNOWN,// Индикатор плана нумерации отправителя (в данном случае UNKNOWN)
                    "SENDER",                      // Номер отправителя (может быть алфавитно-цифровым или числовым)
                    TypeOfNumber.UNKNOWN,          // Тип номера получателя (в данном случае UNKNOWN)
                    NumberingPlanIndicator.UNKNOWN,// Индикатор плана нумерации получателя (в данном случае UNKNOWN)
                    destinationNumber,             // Номер получателя (адрес назначения сообщения)
                    new ESMClass(),                // Класс ESM (Extended Short Message)
                    (byte) 0,                      // Время жизни сообщения (в данном случае 0 - без ограничения)
                    (byte) 1,                      // Код качества обслуживания (в данном случае 1 - не проверять)
                    null,                          // Идентификатор сообщения (если null, будет сгенерирован автоматически)
                    null,                          // Код приложения
                    // RegisteredDelivery используется для определения опций доставки сообщений.
                    // Опция SMSCDeliveryReceipt определяет, должен ли SMSC (Short Message Service Center)
                    // отправлять квитанции о доставке сообщений на отправителя. В данном случае,
                    // опция установлена на SUCCESS_FAILURE, что означает, что SMSC будет отправлять квитанции
                    // о доставке только в случае успешной или неуспешной доставки сообщения.
                    new RegisteredDelivery()
                            .setSMSCDeliveryReceipt(
                                    SMSCDeliveryReceipt.SUCCESS_FAILURE
                            ),
                    (byte) 0,                      // Язык данных сообщения (в данном случае 0 - язык по умолчанию)
                    new GeneralDataCoding(         // Кодировка данных сообщения
                            Alphabet.ALPHA_DEFAULT,
                            MessageClass.CLASS1,
                            false
                    ),
                    (byte) 0,                      // Отложенная доставка (в данном случае 0 - не откладывать)
                    message.getBytes()             // Байтовое представление текста сообщения
            );
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
    }

    private static void connectingAndBinding(SMPPSession session, String systemId, String password, String serverHost, int serverPort) throws IOException {
        // Создаем параметры привязки (bind) сессии
        BindParameter bindParam = new BindParameter(
                BindType.BIND_TRX,               // Тип привязки (в данном случае двунаправленная передача на сервер)
                systemId,                        // Имя пользователя (system_id) для подключения к серверу
                password,                        // Пароль для подключения к серверу
                "cp",                            // Системный тип (system_type) для подключения к серверу (может быть пустым)
                TypeOfNumber.UNKNOWN,            // Тип номера отправителя (в данном случае UNKNOWN)
                NumberingPlanIndicator.INTERNET, // Индикатор плана нумерации (в данном случае INTERNET)
                null                             // Список опциональных параметров (в данном случае не указываем)
        );

        log.info("BindParameter has been created: " + bindParam.toString());

        // Подключаемся к серверу и выполняем привязку (bind) с указанными параметрами

        session.connectAndBind(serverHost, serverPort, bindParam);
    }

    public static void main(String[] args) {
        String serverHost = "192.168.89.47"; // Адрес сервера SMPP
        int serverPort = 2775;               // Порт сервера SMPP
        String systemId = "username";        // Ид системы
        String password = "password";        // Пароль

        try {
            // Создаем новую сессию SMPP
            SMPPSession session = new SMPPSession();

            log.info("Object session has been created: " + session.toString());

            // Устанавливаем таймаут транзакции на 3000 миллисекунд (6 секунд)
            session.setTransactionTimer(3000);

            messageReceiverListening(session);

            connectingAndBinding(session, systemId, password, serverHost, serverPort);

            log.info("SMPP client connected and bound");

            sendShortMessage(session);

            // Отключаемся от сервера и закрываем сессию
            session.unbindAndClose();
        } catch (IOException e) {
            log.error("Failed connect and bind to SMSC server" + e);
        }
    }
}

