package com.smpp.client;

import lombok.Getter;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ClientSmpp {

    private static final Logger log = LoggerFactory.getLogger(ClientSmpp.class);

    @Getter
    private SMPPSession smppSession = new SMPPSession();

    public void run() {
        try {
            this.smppSession.connectAndBind(
                    "192.168.89.181",
                    2775,
                    new BindParameter(
                            BindType.BIND_TRX,
                            "user",
                            "pwrd",
                            "cp",
                            TypeOfNumber.INTERNATIONAL,
                            NumberingPlanIndicator.UNKNOWN,
                            null
                    )
            );

            this.smppSession.setMessageReceiverListener(new DeliveryConfirmationMessageReceiverListener());

//            SendSubmitSm.sendSubmitSm(smppSession, "HELLO MY FRIEND!");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
