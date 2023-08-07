package com.task;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Builder
@Getter
@Setter
public class ServerSmppConfiguration {
    // Уникальный идентификатор сервера
    private int id;

    // Имя или описание сервера (максимальная длина 50 символов)
    private String serverName;

    // Порт, на котором сервер будет слушать входящие соединения от клиентов SMPP
    private int serverPort;

    // Время ожидания привязки (в миллисекундах)
    private int waitBind;

    // Размер пула потоков для обработки входящих сообщений
    private int poolSize;

    // Таймаут для различных операций (в миллисекундах)
    private int timeout;

    // Время ожидания следующей части сообщения (в миллисекундах)
    private int nextPartWaiting;

    // Количество потоков для обработки входящих сообщений
    private int processorDegree;

    // Версия SMPP, используемая сервером (максимальная длина 6 символов)
    private byte interfaceVersion;

    // Тип сервиса, предоставляемого сервером (максимальная длина 24 символа)
    private String serviceType;

    // Тип конкатенации сообщений (максимальная длина 2 символа)
    private String concatinateType;

    // Количество отправляемых отчетов о доставке в секунду
    private int sendDLRPerSec;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerSmppConfiguration)) return false;
        ServerSmppConfiguration that = (ServerSmppConfiguration) o;
        return id == that.id && serverPort == that.serverPort && waitBind == that.waitBind && poolSize == that.poolSize && timeout == that.timeout && nextPartWaiting == that.nextPartWaiting && processorDegree == that.processorDegree && interfaceVersion == that.interfaceVersion && sendDLRPerSec == that.sendDLRPerSec && serverName.equals(that.serverName) && serviceType.equals(that.serviceType) && concatinateType.equals(that.concatinateType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serverName, serverPort, waitBind, poolSize, timeout, nextPartWaiting, processorDegree, interfaceVersion, serviceType, concatinateType, sendDLRPerSec);
    }

    @Override
    public String toString() {
        return "ServerSmppConfiguration{" +
               "id=" + id +
               ", serverName='" + serverName + '\'' +
               ", serverPort=" + serverPort +
               ", waitBind=" + waitBind +
               ", poolSize=" + poolSize +
               ", timeout=" + timeout +
               ", nextPartWaiting=" + nextPartWaiting +
               ", processorDegree=" + processorDegree +
               ", interfaceVersion='" + interfaceVersion + '\'' +
               ", serviceType='" + serviceType + '\'' +
               ", concatinateType='" + concatinateType + '\'' +
               ", sendDLRPerSec=" + sendDLRPerSec +
               '}';
    }
}

