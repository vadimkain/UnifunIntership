package com.task;

import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.SMPPServerSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSmpp implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ServerSmpp.class);

    /**
     * `messageParts` - это экземпляр класса `ConcurrentHashMap`, который служит для временного хранения частей SMS-сообщений до тех пор, пока они не будут полностью собраны в одно целое сообщение. Для лучшего понимания давайте разберемся, почему это может быть полезным и как это работает:
     * <br><br>
     * 1. <b>Проблема</b>: SMS-сообщения могут быть достаточно большими, и в определенных случаях они могут быть разбиты на части, так как операторы связи могут иметь ограничения на размер SMS. Например, если текст SMS превышает определенное количество символов, он может быть разбит на несколько частей.
     * <br><br>
     * 2. <b>Решение</b>: Для того чтобы передать такие большие SMS, SMPP-протокол предоставляет механизм разбиения SMS на части, а также механизм сборки этих частей на стороне получателя. Для этой цели `messageParts` используется как временное хранилище для этих частей.
     * <br><br>
     * 3. <b>Работа с `messageParts`</b>:
     * - Когда SMS-сообщение разбивается на части на стороне отправителя, каждая часть получает уникальный идентификатор, как правило, номер в последовательности.
     * - Эти части отправляются на сервер, и каждая из них помещается в `messageParts` с использованием уникального идентификатора в качестве ключа.
     * - Как только все части SMS приходят на сервер, они могут быть извлечены из `messageParts`, объединены в одно целое сообщение и обработаны как обычное SMS-сообщение.
     * <br><br>
     * 4. <b>Потокобезопасность</b>: Использование `ConcurrentHashMap` гарантирует потокобезопасность при доступе к `messageParts`, что особенно важно в многопоточной среде, как сервер SMPP.
     * <br><br>
     * 5. <b>Очистка</b>: После того как все части SMS были успешно собраны и обработаны, соответствующие записи из `messageParts` могут быть удалены для освобождения ресурсов.
     * <br><br>
     * Таким образом, использование `messageParts` позволяет серверу правильно собирать и обрабатывать большие SMS-сообщения, разделенные на части, и обеспечивает надежную обработку и доставку полных SMS-сообщений получателю.
     */
    private static ConcurrentHashMap<String, MessageContainer> messageParts;

    private final ServerSmppConfiguration configuration;

    //    Храним сессии
    private final ConcurrentHashMap<String, SMPPServerSession> serverSessionsConcurrentHashMap = new ConcurrentHashMap();
    private static boolean serverIsRunning;
    SMPPServerSessionListener sessionListener;


    public ServerSmpp(ServerSmppConfiguration configuration, ConcurrentHashMap<String, MessageContainer> messageParts) {
        this.configuration = configuration;
        ServerSmpp.messageParts = messageParts;
    }


    @Override
    public void run() {
        ServerSmpp.serverIsRunning = true;

        Thread.currentThread().setName("THREAD-SmppServer-" + configuration.getId() + "-" + configuration.getServerName());

        ExecutorService executorService = Executors.newFixedThreadPool(
                configuration.getWaitBind(),
                new CustomThreadFactoryBuilder()
                        .setPriority(Thread.MAX_PRIORITY)
                        .setNamePrefix("_waitBind_" + configuration.getServerName())
                        .setDaemon(false)
                        .build()
        );

        try {
            sessionListener = new SMPPServerSessionListener(configuration.getServerPort());
            sessionListener.setSessionStateListener(new SessionStateListenerImpl(serverSessionsConcurrentHashMap));
            sessionListener.setPduProcessorDegree(configuration.getProcessorDegree());
            sessionListener.setTimeout(configuration.getTimeout());

            log.info("Server started:\n" +
                     "id = {}\n" +
                     "serverName = {}\n" +
                     "serverPort = {}\n" +
                     "waitBind = {}\n" +
                     "poolSize = {}\n" +
                     "timeout = {}\n" +
                     "nextPartWaiting = {}\n" +
                     "processorDegree = {}\n" +
                     "interfaceVersion = {}\n" +
                     "serviceType = {}\n" +
                     "concatinateType = {}\n" +
                     "sendDLRPerSec = {}",
                    configuration.getId(),
                    configuration.getServerName(),
                    configuration.getServerPort(),
                    configuration.getWaitBind(),
                    configuration.getPoolSize(),
                    configuration.getTimeout(),
                    configuration.getNextPartWaiting(),
                    configuration.getProcessorDegree(),
                    configuration.getInterfaceVersion(),
                    configuration.getServiceType(),
                    configuration.getConcatinateType(),
                    configuration.getSendDLRPerSec()
            );

            while (ServerSmpp.serverIsRunning) {
                SMPPServerSession smppServerSession = sessionListener.accept();

                smppServerSession.setMessageReceiverListener(new ServerMessageReceiverListenerImpl());
//                smppServerSession.setResponseDeliveryListener();

                try {
                    executorService.execute(new SmppWaitBindTask(configuration, smppServerSession));
                } catch (Exception e) {
                    log.error(e.toString() + Arrays.toString(e.getStackTrace()));
                }
            }

            ServerSmpp.serverIsRunning = false;
            executorService.shutdown();

        } catch (IOException e) {
            log.error(e + Arrays.toString(e.getStackTrace()));
        }
    }
}
