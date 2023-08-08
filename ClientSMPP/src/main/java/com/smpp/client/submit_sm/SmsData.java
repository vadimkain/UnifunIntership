package com.smpp.client.submit_sm;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.AccessType;
import org.mobicents.protocols.ss7.map.api.service.sms.SMDeliveryOutcome;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class SmsData {
    private long messageId;                // Уникальный идентификатор сообщения
    private long[] messagePartsId;         // Идентификаторы частей сообщения (если сообщение разделено)
    private String fromAD;                 // Адрес отправителя
    private String fromTON;                // Тип номера отправителя (например, TON_ALPHANUMERIC)
    private String fromNP;                 // Индикатор плана нумерации отправителя (например, NP_UNKNOWN)
    private Long toAD;                     // Адрес получателя
    private String toAN;                   // Тип номера получателя (например, TON_UNKNOWN)
    private String toNP;                   // Индикатор плана нумерации получателя (например, UNKNOWN)
    private String message;                // Текст сообщения
    private AccessType channel;            // Тип канала доступа
    private short quantity;                // Количество частей сообщения (если сообщение разделено)
    private int dcs;                       // Кодировка данных сообщения (например, GSM_CHARSET)
    private int pid;                       // Код протокола (например, PID_DEFAULT)
    private LocalDateTime inserted;              // Время вставки в базу данных
    private LocalDateTime scheduledTime;         // Запланированное время отправки
    private LocalDateTime senduntil;             // Время до которого сообщение будет отправлено
    private int systemId;                  // Идентификатор системы
    private String dlrResponseType;        // Тип отчета о доставке
    private byte priority;               // Приоритет сообщения
    private String segmentLen;             // Длина сегмента (для конкатенированных сообщений)
    private Long dialogId;                 // Идентификатор диалога
    private LocalDateTime started;               // Время начала обработки сообщения
    private short messagePart = 1;         // Номер части сообщения (если сообщение разделено)
    //    private ISDNAddressString networkNodeNumber; // Номер сетевого узла
//    private IMSI imsi;                     // Международный идентификатор подвижной станции
    private SMDeliveryOutcome smDeliveryOutcome; // Результат доставки сообщения
    private int sendAttempts = 1;          // Количество попыток отправки
    private boolean expired = false;       // Признак истекшего сообщения
    private String state = "8";            // Состояние сообщения
    private int dlrSendAttempts = 0;       // Количество попыток отправки отчета о доставке
    private LocalDateTime sendDLRUntil;          // Время до которого отправить отчет о доставке
    private LocalDateTime nextDLRAttempt;        // Время следующей попытки отправки отчета о доставке
    private LocalDateTime receivedDLR;           // Время получения отчета о доставке
    private String globalKey;              // Глобальный ключ сообщения
    private Integer previousErrorCode = null; // Предыдущий код ошибки
    private int errorCounter = 0;          // Счетчик ошибок
    private LocalDateTime sentSriRequest;        // Время отправки запроса на отправку сообщения
    private LocalDateTime receivedSriResponse;   // Время получения ответа на запрос отправки сообщения
    private LocalDateTime sentMtfSmRequest;      // Время отправки запроса MTF SM
    private LocalDateTime receivedMtfSmResponse; // Время получения ответа MTF SM
    private LocalDateTime sentRdsSmRequest;      // Время отправки запроса RDS SM
    private LocalDateTime receivedRdsSmResponse; // Время получения ответа RDS SM
}
