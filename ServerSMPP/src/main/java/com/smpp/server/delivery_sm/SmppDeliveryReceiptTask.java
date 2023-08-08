package com.smpp.server.delivery_sm;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.util.DeliveryReceiptState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;

public class SmppDeliveryReceiptTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SmppDeliveryReceiptTask.class);
    private final SmsData _SMSDATA;
    private final SMPPServerSession _SESSION;
    private final long _STARTED;

    public SmppDeliveryReceiptTask(SmsData _SMSDATA, SMPPServerSession _SESSION) {
        this._SMSDATA = _SMSDATA;
        this._SESSION = _SESSION;
        this._STARTED = System.currentTimeMillis();
    }

    @Override
    public void run() {

        String dlrResponseType = this._SMSDATA.getDlrResponseType();
        String smsState = this._SMSDATA.getState();

        // Проверка на отправку доставочного подтверждения
//        if (!"0".equals(dlrResponseType) && (!"1".equals(dlrResponseType) || ("2".equals(smsState) && "2".equals(dlrResponseType)) || (!"2".equals(smsState) && "3".equals(dlrResponseType)))) {
//            if (this._SESSION.getSessionState().isReceivable()) {
//                if ("9".equals(smsState)) {
//                    this._SMSDATA.setState("7");
//                }

        DeliveryReceipt deliveryReceipt = new DeliveryReceipt(
                String.valueOf(this._SMSDATA.getMessageId()),
                1,
                1,
                Date.from(this._SMSDATA.getInserted().atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(this._SMSDATA.getReceivedDLR().atZone(ZoneId.systemDefault()).toInstant()),
                DeliveryReceiptState.valueOf(Integer.valueOf(this._SMSDATA.getState())),
                null,
                this._SMSDATA.getMessage()
        );

        try {
            _SESSION.deliverShortMessage(
                    "",
                    TypeOfNumber.valueOf(Byte.parseByte(_SMSDATA.getFromTON())),
                    NumberingPlanIndicator.valueOf(Byte.parseByte(_SMSDATA.getFromNP())),
                    _SMSDATA.getFromAD(),
                    TypeOfNumber.valueOf(Byte.parseByte(_SMSDATA.getToAN())),
                    NumberingPlanIndicator.valueOf(Byte.parseByte(_SMSDATA.getToNP())),
                    String.valueOf(_SMSDATA.getToAD()),
                    new ESMClass(
                            MessageMode.STORE_AND_FORWARD,
                            MessageType.SMSC_DEL_RECEIPT,
                            GSMSpecificFeature.REPLYPATH
                    ),
                    (byte) 0,
                    (byte) 0,
                    new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE),
                    DataCodings.newInstance((byte) _SMSDATA.getDcs()),
                    deliveryReceipt.toString().getBytes()
            );

            if (System.currentTimeMillis() - _STARTED > 1000) {
                log.warn("Process SmppDeliveryReceiptTask to long!");
            }

            log.info("Delivery has been in session {}", _SESSION.getSessionId());

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
//            } else {
//                log.warn("Connection lost. SessionId - {}", _SESSION.getSessionId());
//            }
//        }
    }
}
