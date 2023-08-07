package com.task;

import org.jsmpp.bean.InterfaceVersion;

import java.util.concurrent.ConcurrentHashMap;

public class ServerRunner {
    public static void main(String[] args) {
        // Создаем конфигурацию для сервера
        ServerSmppConfiguration configuration = ServerSmppConfiguration.builder()
                .id(1)
                .serverName("SMPP Server")
                .serverPort(2775)
                .waitBind(10)
                .poolSize(10)
                .timeout(10000)
                .nextPartWaiting(1000)
                .processorDegree(4)
                .interfaceVersion(InterfaceVersion.IF_34.value())
                .serviceType("DEFAULT")
                .concatinateType("1")
                .sendDLRPerSec(100)
                .build();

        // Создаем ConcurrentHashMap для хранения частей сообщений
        ConcurrentHashMap<String, MessageContainer> messageParts = new ConcurrentHashMap<>();

        // Создаем экземпляр сервера
        ServerSmpp serverSmpp = new ServerSmpp(configuration, messageParts);

        // Запускаем сервер в отдельном потоке
        Thread serverThread = new Thread(serverSmpp);
        serverThread.start();

        // Ожидаем завершения работы сервера (можно добавить обработку сигналов для остановки)
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
