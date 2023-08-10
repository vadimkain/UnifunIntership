package com.smpp.client;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SubmitSmResult;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

public class SendSubmitSm {
    private static final Logger log = LoggerFactory.getLogger(SendSubmitSm.class);

    public static void sendSubmitSm(SMPPSession smppSession, String shortMessage) {
        try {
            log.info("in sendSubmitSm");

            SubmitSmResult submitSmResult = smppSession.submitShortMessage(
                    "CMT",
                    TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.UNKNOWN,
                    "1616",
                    TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.UNKNOWN,
                    "380665431860",
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    new AbsoluteTimeFormatter().format(new Date()),
                    null,
                    new RegisteredDelivery().setSMSCDeliveryReceipt(SMSCDeliveryReceipt.SUCCESS_FAILURE),
                    (byte) 0,
                    new GeneralDataCoding(
                            Alphabet.ALPHA_DEFAULT,
                            MessageClass.CLASS1,
                            false
                    ),
                    (byte) 0,
                    shortMessage.getBytes()
            );


            log.info("Submit SM has been sent");

        } catch (PDUException e) {
            log.error(e.getMessage());
        } catch (ResponseTimeoutException e) {
            log.error(e.getMessage());
        } catch (InvalidResponseException e) {
            log.error(e.getMessage());
        } catch (NegativeResponseException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
