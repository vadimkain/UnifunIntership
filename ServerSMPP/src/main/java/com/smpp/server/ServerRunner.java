package com.smpp.server;

import org.jsmpp.bean.InterfaceVersion;

public class ServerRunner {
    public static void main(String[] args) {
        Thread server = new Thread(new ServerSmpp(
                ServerSmppConfiguration.builder()
                        .id(1)
                        .serverName("SMPP Server")
                        .serverPort(2775)
                        .waitBind(10)
                        .poolSize(10)
                        .timeout(10000000)
                        .nextPartWaiting(1000)
                        .processorDegree(4)
                        .interfaceVersion(InterfaceVersion.IF_34.value())
                        .serviceType("DEFAULT")
                        .concatinateType("1")
                        .sendDLRPerSec(100)
                        .build()
        ));

        server.start();

        try {
            server.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
