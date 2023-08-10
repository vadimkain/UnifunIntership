package com.smpp.server;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.*;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

public class ForServerMessageReceiverListener implements ServerMessageReceiverListener {
    private static final Logger log = LoggerFactory.getLogger(ForServerMessageReceiverListener.class);

    private SMPPSession clientSession;

    public ForServerMessageReceiverListener(SMPPSession clientSession) {
        this.clientSession = clientSession;
    }

    @Override
    public SubmitSmResult onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession smppServerSession) throws ProcessRequestException {
        log.info("Received message: {}", submitSm.getShortMessage());

        try {
            this.clientSession.submitShortMessage(
                    submitSm.getServiceType(),
                    TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.UNKNOWN,
                    submitSm.getSourceAddr(),
                    TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.UNKNOWN,
                    submitSm.getDestAddress(),
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    new AbsoluteTimeFormatter().format(new Date()),
                    null,
                    new RegisteredDelivery().setSMSCDeliveryReceipt(SMSCDeliveryReceipt.SUCCESS_FAILURE),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false),
                    (byte) 0,
                    submitSm.getShortMessage()
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

        return new SubmitSmResult(new RandomMessageIDGenerator().newMessageId(), new OptionalParameter[]{});
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
