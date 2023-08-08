package com.smpp.server.submit_sm;

import com.smpp.server.ServerSmpp;
import com.smpp.server.delivery_sm.ServerResponseDeliveryAdapterImpl;
import com.smpp.server.delivery_sm.SmsData;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.*;
import org.jsmpp.util.MessageId;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class ServerMessageReceiverListenerImpl implements ServerMessageReceiverListener {
    private static final Logger log = LoggerFactory.getLogger(ServerSmpp.class);

    @Override
    public SubmitSmResult onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession smppServerSession) throws ProcessRequestException {
        log.info("Received message from {} : {}", submitSm.getSourceAddr(), new String(submitSm.getShortMessage(), StandardCharsets.UTF_8));

        //        Обрабатываем логику тут

        MessageId messageId = new RandomMessageIDGenerator().newMessageId();

        SmsData smsData = new SmsData();

//        smsData.setMessageId(Long.parseLong(messageId.getValue()));
        smsData.setFromAD(submitSm.getSourceAddr());
        smsData.setFromTON(String.valueOf(submitSm.getSourceAddrTon()));
        smsData.setFromNP(String.valueOf(submitSm.getSourceAddrNpi()));
        smsData.setToAD(Long.parseLong(submitSm.getDestAddress()));
        smsData.setToAN(String.valueOf(submitSm.getDestAddrTon()));
        smsData.setToNP(String.valueOf(submitSm.getDestAddrNpi()));
        smsData.setMessage(new String(submitSm.getShortMessage(), StandardCharsets.UTF_8));
        smsData.setDcs(submitSm.getDataCoding());
        smsData.setInserted(LocalDateTime.now()); // Задаем текущее время вставки в базу данных
        smsData.setReceivedDLR(LocalDateTime.now()); // Пока не получили отчет о доставке, так что оставляем как null
        smsData.setState("1"); // Задаем состояние сообщения, например, "8"

        smppServerSession.setResponseDeliveryListener(new ServerResponseDeliveryAdapterImpl(smsData));

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
