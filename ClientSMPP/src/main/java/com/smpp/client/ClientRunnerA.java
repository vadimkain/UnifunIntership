package com.smpp.client;

public class ClientRunnerA {
    public static void main(String[] args) {
        ClientSmpp clientSmpp = new ClientSmpp();

        clientSmpp.run();

        SendSubmitSm.sendSubmitSm(clientSmpp.getSmppSession(), "HELLO");
    }
}