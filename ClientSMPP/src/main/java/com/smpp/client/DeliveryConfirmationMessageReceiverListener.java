package com.smpp.client;

import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.DeliveryReceipt;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.Session;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryConfirmationMessageReceiverListener implements MessageReceiverListener {
    private static final Logger log = LoggerFactory.getLogger(DeliveryConfirmationMessageReceiverListener.class);

    @Override
    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
        try {
            DeliveryReceipt deliveryReceipt = deliverSm.getShortMessageAsDeliveryReceipt();

            log.info("Delivery confirmation received: {}", deliveryReceipt);

        } catch (InvalidDeliveryReceiptException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAcceptAlertNotification(AlertNotification alertNotification) {

    }

    @Override
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
        return null;
    }
}
