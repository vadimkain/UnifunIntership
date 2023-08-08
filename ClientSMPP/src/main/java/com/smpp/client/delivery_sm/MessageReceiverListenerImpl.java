package com.smpp.client.delivery_sm;

import com.smpp.client.ClientSmppConfiguration;
import com.smpp.client.SmppClient;
import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageReceiverListenerImpl implements MessageReceiverListener {
    private static final Logger log = LoggerFactory.getLogger(MessageReceiverListenerImpl.class);
    private final ClientSmppConfiguration _CONFIGURATION;

    public MessageReceiverListenerImpl(ClientSmppConfiguration _CONFIGURATION) {
        this._CONFIGURATION = _CONFIGURATION;
    }

    @Override
    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
        String message = new String(deliverSm.getShortMessage());
        String sourceAddress = deliverSm.getSourceAddr();
        String destinationAddress = deliverSm.getDestAddress();

        log.info("Received a deliver_sm: \n message: {}, \n source address: {}, \n destination address: {}", message, sourceAddress, destinationAddress);
    }

    @Override
    public void onAcceptAlertNotification(AlertNotification alertNotification) {

    }

    @Override
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
        return null;
    }
}
