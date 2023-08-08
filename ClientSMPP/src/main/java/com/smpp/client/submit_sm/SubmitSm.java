package com.smpp.client.submit_sm;

import com.smpp.client.SmppClient;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.SubmitSmResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// TODO: так как разбор клиентского кода займет намного больше времени - я сделал облегченную версию
public class SubmitSm implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SubmitSm.class);

    private final long _SLEEPTIME;
    private final SmppClient _CLIENT;
    private final SmsData _SMSDATA;

    public SubmitSm(long sleeptime, SmppClient client, SmsData smsdata) {
        this._SLEEPTIME = sleeptime;
        this._CLIENT = client;
        this._SMSDATA = smsdata;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(_CLIENT.get_CONFIGURATION().getSystemId() + "_submitSM_MessageId-" + _SMSDATA.getMessageId());

        switch (_CLIENT.get_CONFIGURATION().getServiceType()) {
            case "UDH":
                break;
            case "SAR":
                break;
            case "Payload":
                break;
            case "DFLT":
                try {
                    SubmitSmResult submitSmResult = _CLIENT.getSmppSession().submitShortMessage(
                            _CLIENT.get_CONFIGURATION().getServiceType(),
                            TypeOfNumber.valueOf(_SMSDATA.getFromTON()),
                            NumberingPlanIndicator.valueOf(_SMSDATA.getFromNP()),
                            _SMSDATA.getFromAD(),
                            TypeOfNumber.valueOf(_SMSDATA.getToAN()),
                            NumberingPlanIndicator.valueOf(_SMSDATA.getToNP()),
                            String.valueOf(_SMSDATA.getToAD()),
                            new ESMClass(
                                    MessageMode.STORE_AND_FORWARD,
                                    MessageType.SMSC_DEL_RECEIPT,
                                    GSMSpecificFeature.REPLYPATH
                            ),
                            Integer.valueOf(_SMSDATA.getPid()).byteValue(),
                            Integer.valueOf(_SMSDATA.getPriority()).byteValue(),
                            null,
                            null,
                            new RegisteredDelivery().setSMSCDeliveryReceipt(SMSCDeliveryReceipt.valueOf(_SMSDATA.getDlrResponseType())),
                            (byte) 0,
                            new GeneralDataCoding(
                                    Alphabet.ALPHA_DEFAULT,
                                    MessageClass.CLASS1,
                                    false
                            ),
                            (byte) 0,
                            _SMSDATA.getMessage().getBytes()
                    );

                } catch (PDUException e) {
                    log.error("Invalid PRU parameter " + e);
                } catch (ResponseTimeoutException e) {
                    log.error("Response timeout " + e);
                } catch (InvalidResponseException e) {
                    log.error("Receive invalid response " + e);
                } catch (NegativeResponseException e) {
                    log.error("Receive negative response " + e);
                } catch (IOException e) {
                    log.error("IO error occured " + e);
                }
                break;
            case "CMT":
                break;
        }
    }
}
