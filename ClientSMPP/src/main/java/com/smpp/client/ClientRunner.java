package com.smpp.client;

import com.smpp.client.submit_sm.SmsData;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.AccessType;
import org.mobicents.protocols.ss7.map.api.service.sms.SMDeliveryOutcome;

import java.time.LocalDateTime;

public class ClientRunner {

    public static void clientA() {
        ClientSmppConfiguration clientConfig = ClientSmppConfiguration.builder()
                .id(1)
                .systemId("mySystemId")
                .password("Password")
                .groupID(123)
                .clientPriority(1)
                .concatenateType("longMessage")
                .systemType("mySystem")
                .serviceType("DFLT")
                .ton("INTERNATIONAL")
                .np("INTERNET")
                .host("192.168.89.181")
                .port(2775)
                .timeOut(10000)
                .pduProcessorDegree(4)
                .bindType("BIND_TRX")
                .reconnectTries(5)
                .reconnectTriesTime(15000)
                .speedLimit(200)
                .remoteIdType("ip")
                .dlrIdType("messageId")
                .enabled(true)
                .build();

        ConnectionLimits connectionLimits = new ConnectionLimits(1, 500);

        SmppClient client = new SmppClient(clientConfig, connectionLimits);

        SmsData smsData = SmsData.builder()
                .messageId(12345)
                .messagePartsId(new long[]{1, 2, 3})
                .fromAD("sender")
                .fromTON("ALPHANUMERIC")
                .fromNP("UNKNOWN")
                .toAD(9876543210L)
                .toAN("INTERNATIONAL")
                .toNP("UNKNOWN")
                .message("HELLO MY FRIEND.")
                .channel(AccessType.shortMessage)
                .quantity((short) 3)
                .dcs(0)
                .pid(0)
                .inserted(LocalDateTime.now())
                .scheduledTime(LocalDateTime.now().plusSeconds(3600))
                .senduntil(LocalDateTime.now().plusSeconds(7200))
                .systemId(1)
                .dlrResponseType("SUCCESS_FAILURE")
                .priority((byte) 0)
                .segmentLen("160")
                .dialogId(67890L)
                .started(LocalDateTime.now())
                .messagePart((short) 1)
//                .networkNodeNumber(new ISDNAddressStringImpl("TON_INTERNATIONAL", "NP_E164", "1234567890"))
//                .imsi(new IMSIImpl("123456789012345"))
                .smDeliveryOutcome(SMDeliveryOutcome.successfulTransfer)
                .sendAttempts(1)
                .expired(false)
                .state("0")
                .dlrSendAttempts(0)
                .sendDLRUntil(LocalDateTime.now().plusSeconds(1800))
                .nextDLRAttempt(LocalDateTime.now().plusSeconds(300))
                .receivedDLR(LocalDateTime.now())
                .globalKey("xyz123")
                .previousErrorCode(null)
                .errorCounter(0)
                .sentSriRequest(LocalDateTime.now())
                .receivedSriResponse(LocalDateTime.now())
                .sentMtfSmRequest(LocalDateTime.now())
                .receivedMtfSmResponse(LocalDateTime.now())
                .sentRdsSmRequest(LocalDateTime.now())
                .receivedRdsSmResponse(LocalDateTime.now())
                .build();

        Thread smppClientThread = new Thread(client);

        smppClientThread.start();

        client.getSmsToSend().add(smsData);

        try {
            smppClientThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clientB() {
    }

    public static void main(String[] args) {
        clientA();
    }
}
