package com.smpp.server;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.Session;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ForDestinationClientMessageReceiverListener implements MessageReceiverListener {
    private static final Logger log = LoggerFactory.getLogger(ForDestinationClientMessageReceiverListener.class);
    private SMPPServerSession smppServerSession;

    public ForDestinationClientMessageReceiverListener(SMPPServerSession smppServerSession) {
        this.smppServerSession = smppServerSession;
    }

    @Override
    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {

        DeliveryReceipt deliveryReceipt = null;

        try {
            deliveryReceipt = deliverSm.getShortMessageAsDeliveryReceipt();

            if (deliverSm.getShortMessageAsDeliveryReceipt().equals(DeliveryReceipt.DELREC_DLVRD)) {
                log.info("{}", DeliveryReceipt.DELREC_DLVRD);

                this.smppServerSession.deliverShortMessage(
                        deliverSm.getServiceType(),
                        TypeOfNumber.INTERNATIONAL,
                        NumberingPlanIndicator.ISDN,
                        deliverSm.getDestAddress(),
                        TypeOfNumber.INTERNATIONAL,
                        NumberingPlanIndicator.ISDN,
                        deliverSm.getSourceAddr(),
                        new ESMClass(
                                MessageMode.DEFAULT,
                                MessageType.SMSC_DEL_RECEIPT,
                                GSMSpecificFeature.DEFAULT
                        ),
                        (byte) 1,
                        (byte) 1,
                        new RegisteredDelivery().setSMSCDeliveryReceipt(SMSCDeliveryReceipt.SUCCESS_FAILURE),
                        DataCodings.ZERO,
                        deliverSm.getShortMessage()
                );

            }
        } catch (InvalidDeliveryReceiptException e) {
            log.info("{}", deliveryReceipt);
            throw new RuntimeException(e);
        } catch (ResponseTimeoutException e) {
            throw new RuntimeException(e);
        } catch (PDUException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidResponseException e) {
            throw new RuntimeException(e);
        } catch (NegativeResponseException e) {
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
