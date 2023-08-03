package com.task;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SubmitSmResult;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);
    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();

    public static void main(String[] args) {
        String serverHost = "192.168.89.47"; // Адрес сервера SMPP
        int serverPort = 2775;               // Порт сервера SMPP
        String systemId = "username";        // Ид системы
        String password = "password";        // Пароль

        try {
            // Создаем новую сессию SMPP
            SMPPSession session = new SMPPSession();

            log.info("Object session has been created: " + session.toString());

            // Устанавливаем таймаут транзакции на 6000 миллисекунд (6 секунд)
            session.setTransactionTimer(3000); // Таймаут в 3 секунды

            // Создаем параметры привязки (bind) сессии
            BindParameter bindParam = new BindParameter(
                    BindType.BIND_TX,                // Тип привязки (в данном случае однонаправленная передача на сервер)
                    systemId,                        // Имя пользователя (system_id) для подключения к серверу
                    password,                        // Пароль для подключения к серверу
                    "cp",                            // Системный тип (system_type) для подключения к серверу (может быть пустым)
                    TypeOfNumber.UNKNOWN,            // Тип номера отправителя (в данном случае UNKNOWN)
                    NumberingPlanIndicator.INTERNET, // Индикатор плана нумерации (в данном случае INTERNET)
                    null                             // Список опциональных параметров (в данном случае не указываем)
            );

            log.info("BindParameter has been created: " + bindParam.toString());

//            session.setMessageReceiverListener(new MessageReceiverListener() {
//                /**
//                 * Метод onAcceptDeliverSm обрабатывает входящее SMS-сообщение DeliverSm.
//                 * Если сообщение содержит квитанцию о доставке (SMSC_DEL_RECEIPT), то парсит содержимое квитанции
//                 * и логирует информацию о доставке сообщения.
//                 * Если сообщение не содержит квитанцию о доставке, то выводит в лог содержимое сообщения.
//                 *
//                 * @param deliverSm Входящее SMS-сообщение типа DeliverSm
//                 * @throws ProcessRequestException Исключение, которое может быть выброшено при обработке запроса
//                 */
//                @Override
//                public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
//                    // Проверяем, содержит ли сообщение квитанцию о доставке (SMSC_DEL_RECEIPT)
//                    if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {
//                        try {
//                            // Получаем квитанцию о доставке из содержимого сообщения
//                            DeliveryReceipt deliveryReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
//                            // Преобразуем идентификатор квитанции из формата строки в шестнадцатеричное значение
//                            String messageId = Long.toString(Long.parseLong(deliveryReceipt.getId()) & 0xffffffff, 16).toUpperCase();
//                            // Логируем информацию о доставке сообщения
//                            log.info("Received '{}' : {}", messageId, deliveryReceipt);
//                        } catch (InvalidDeliveryReceiptException e) {
//                            // В случае ошибки парсинга квитанции логируем исключение
//                            log.error("Receive failed ", e);
//                        }
//                    } else {
//                        // Если сообщение не содержит квитанцию о доставке, то выводим содержимое сообщения в лог
//                        log.info("Receiving message: {}", new String(deliverSm.getShortMessage()));
//                    }
//                }
//
//
//                @Override
//                public void onAcceptAlertNotification(AlertNotification alertNotification) {
//                    log.info("Receiving alert for {} from {}", alertNotification.getSourceAddr(), alertNotification.getEsmeAddr());
//                }
//
//                @Override
//                public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
//                    return null;
//                }
//            });

            // Подключаемся к серверу и выполняем привязку (bind) с указанными параметрами

            session.connectAndBind(serverHost, serverPort, bindParam);

            log.info("SMPP client connected and bound");

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
                log.error("Response timeout " + e);
            } catch (InvalidResponseException e) {
                log.error("Receive invalid response " + e);
            } catch (NegativeResponseException e) {
                log.error("Receive negative response " + e);
            } catch (IOException e) {
                log.error("IO error occured " + e);
            }

            // Отключаемся от сервера и закрываем сессию
            session.unbindAndClose();
        } catch (IOException e) {
            log.error("Failed connect and bind to SMSC server" + e);
        }
    }
}

