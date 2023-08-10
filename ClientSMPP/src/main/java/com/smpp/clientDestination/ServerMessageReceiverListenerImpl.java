package com.smpp.clientDestination;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.*;
import org.jsmpp.util.DeliveryReceiptState;
import org.jsmpp.util.MessageId;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class ServerMessageReceiverListenerImpl implements ServerMessageReceiverListener {
    private static final Logger log = LoggerFactory.getLogger(ClientDestinationSmpp.class);

    @Override
    public SubmitSmResult onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession smppServerSession) throws ProcessRequestException {
        log.info("Received message: {}", submitSm.getShortMessage());

        MessageId messageId = new RandomMessageIDGenerator().newMessageId();

        try {
            smppServerSession.deliverShortMessage(
                    submitSm.getServiceType(),
                    TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.ISDN,
                    submitSm.getDestAddress(),
                    TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.ISDN,
                    submitSm.getSourceAddr(),
                    new ESMClass(
                            MessageMode.DEFAULT,
                            MessageType.SMSC_DEL_RECEIPT,
                            GSMSpecificFeature.DEFAULT
                    ),
                    (byte) 1,
                    (byte) 1,
                    new RegisteredDelivery().setSMSCDeliveryReceipt(SMSCDeliveryReceipt.SUCCESS_FAILURE),
                    DataCodings.ZERO,
//                    ВОТ РЕШЕНИЕ ПРОБЛЕМЫ
                    new DeliveryReceipt(
                            messageId.getValue(),
                            1,
                            1,
                            new Date(),
                            new Date(),
                            DeliveryReceiptState.DELIVRD,
                            null,
                            new String(submitSm.getShortMessage(), StandardCharsets.UTF_8)
                    ).toString().getBytes()
            );
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

        return new SubmitSmResult(
                messageId,
                new OptionalParameter[]{}
        );
    }

    @Override
    public SubmitMultiResult onAcceptSubmitMulti(SubmitMulti submitMulti, SMPPServerSession smppServerSession) throws ProcessRequestException {
        return null;
    }

    @Override
    public QuerySmResult onAcceptQuerySm(QuerySm querySm, SMPPServerSession smppServerSession) throws ProcessRequestException {
        return null;
    }

    @Override
    public void onAcceptReplaceSm(ReplaceSm replaceSm, SMPPServerSession smppServerSession) throws ProcessRequestException {

    }

    @Override
    public void onAcceptCancelSm(CancelSm cancelSm, SMPPServerSession smppServerSession) throws ProcessRequestException {

    }

    @Override
    public BroadcastSmResult onAcceptBroadcastSm(BroadcastSm broadcastSm, SMPPServerSession smppServerSession) throws ProcessRequestException {
        return null;
    }

    @Override
    public void onAcceptCancelBroadcastSm(CancelBroadcastSm cancelBroadcastSm, SMPPServerSession smppServerSession) throws ProcessRequestException {

    }

    @Override
    public QueryBroadcastSmResult onAcceptQueryBroadcastSm(QueryBroadcastSm queryBroadcastSm, SMPPServerSession smppServerSession) throws ProcessRequestException {
        return null;
    }

    @Override
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
        return null;
    }
}
