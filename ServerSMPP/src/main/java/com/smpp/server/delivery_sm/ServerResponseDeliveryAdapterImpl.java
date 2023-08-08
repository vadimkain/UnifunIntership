package com.smpp.server.delivery_sm;

import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.ServerResponseDeliveryAdapter;
import org.jsmpp.session.SubmitMultiResult;
import org.jsmpp.session.SubmitSmResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerResponseDeliveryAdapterImpl extends ServerResponseDeliveryAdapter {

    private static final Logger log = LoggerFactory.getLogger(ServerResponseDeliveryAdapterImpl.class);
    private final SmsData _SMSDATA;

    public ServerResponseDeliveryAdapterImpl(SmsData smsdata) {
        _SMSDATA = smsdata;
    }

    @Override
    public void onSubmitSmRespSent(SubmitSmResult submitSmResult, SMPPServerSession source) {
        String messageId = submitSmResult.getMessageId();
        String sessionId = source.getSessionId();

        Thread deliveryReceiptTaskThread = new Thread(new SmppDeliveryReceiptTask(
                _SMSDATA,
                source
        ));

        deliveryReceiptTaskThread.start();

        try {
            deliveryReceiptTaskThread.join();
        } catch (InterruptedException e) {
            log.warn("deliveryReceiptTaskThread.join();");
        }

        log.info("Submit_sm has been sent successfully: Message id {}, session id {}", messageId, sessionId);
        log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }

    @Override
    public void onSubmitSmRespError(SubmitSmResult submitSmResult, Exception e, SMPPServerSession source) {
        String messageId = submitSmResult.getMessageId();
        String sessionId = source.getSessionId();

        log.warn("Submit_sm has been sent failed: Message id {}, session id {}", messageId, sessionId);
    }

    @Override
    public void onSubmitMultiRespSent(SubmitMultiResult submitMultiResult, SMPPServerSession source) {
    }

    @Override
    public void onSubmitMultiRespError(SubmitMultiResult submitMultiResult, Exception e, SMPPServerSession source) {
    }
}