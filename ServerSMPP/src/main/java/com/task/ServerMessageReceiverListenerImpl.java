package com.task;

import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.*;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class ServerMessageReceiverListenerImpl implements ServerMessageReceiverListener {
    private static final Logger log = LoggerFactory.getLogger(ServerSmpp.class);

    @Override
    public SubmitSmResult onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession smppServerSession) throws ProcessRequestException {
        log.info("Received message from {} : {}", submitSm.getSourceAddr(), new String(submitSm.getShortMessage(), StandardCharsets.UTF_8));

        //        Обрабатываем логику тут

        // В данном примере просто отправляем успешный ответ на клиентскую сессию
        return new SubmitSmResult(
                new RandomMessageIDGenerator().newMessageId(),
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
