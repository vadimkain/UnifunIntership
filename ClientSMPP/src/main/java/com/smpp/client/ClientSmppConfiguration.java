package com.smpp.client;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ClientSmppConfiguration {
    // Уникальный идентификатор клиента
    private int id;

    // Идентификатор системы (System ID) для аутентификации на SMPP сервере
    private String systemId;

    // Пароль для аутентификации на SMPP сервере
    private String password;

    // Идентификатор группы клиента
    private int groupID;

    // Приоритет клиента
    private int clientPriority;

    // Тип конкатенации сообщений (например, для объединения длинных SMS)
    private String concatenateType;

    // Тип системы
    private String systemType;

    // Тип сервиса
    private String serviceType;

    // Тип номера отправителя
    private String ton;

    // План нумерации номера отправителя
    private String np;

    // Хост (адрес) SMPP сервера
    private String host;

    // Порт SMPP сервера
    private int port;

    // Время ожидания для различных операций (например, ожидание ответа от сервера)
    private int timeOut;

    // Количество потоков для обработки PDU (протокольных единиц данных)
    private int pduProcessorDegree;

    // Тип привязки (bind) клиента к серверу (например, RX, TX, TRX)
    private String bindType;

    // Количество попыток переподключения в случае разрыва соединения
    private int reconnectTries;

    // Время между попытками переподключения
    private int reconnectTriesTime;

    // Ограничение скорости отправки сообщений
    private int speedLimit;

    // Тип идентификатора удаленной сущности
    private String remoteIdType;

    // Тип идентификатора сообщения о доставке (DLR)
    private String dlrIdType;

    // Флаг, указывающий, включен ли клиент
    private Boolean enabled;
}
