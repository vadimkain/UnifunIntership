package com.smpp.client;

import com.smpp.client.delivery_sm.MessageReceiverListenerImpl;
import com.smpp.client.submit_sm.SmppClientSubmitSmWorker;
import com.smpp.client.submit_sm.SmsData;
import lombok.Getter;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SmppClient implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SmppClient.class);
    @Getter
    private final ClientSmppConfiguration _CONFIGURATION;
    private ConnectionLimits connectionLimits;
    @Getter
    private volatile SMPPSession smppSession;
    private AtomicInteger reconnectTears = new AtomicInteger();
    private boolean reconnect;
    private boolean clientIsRunning;
    private AtomicBoolean clientAvailable = new AtomicBoolean();
    private SmppClientSubmitSmWorker submitSmWorker;
    @Getter
    private final ConcurrentLinkedQueue<SmsData> smsToSend = new ConcurrentLinkedQueue<>();
    private Thread submitSmWorkerThread;

    public SmppClient(ClientSmppConfiguration _CONFIGURATION, ConnectionLimits connectionLimits) {
        this._CONFIGURATION = _CONFIGURATION;
        this.connectionLimits = connectionLimits;
        this.clientIsRunning = true;
        this.submitSmWorker = new SmppClientSubmitSmWorker(this);
        this.submitSmWorkerThread = new Thread(this.submitSmWorker);
    }

    public void stop() {
        clientIsRunning = false;

        if (smppSession != null) {
            smppSession.unbindAndClose();
        }

        submitSmWorkerThread.interrupt();

        if (!submitSmWorkerThread.isInterrupted()) {
            log.warn("Force stop submitSmWorkerThread");
            submitSmWorkerThread.interrupt();
        }

        smsToSend.clear();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("_SMPPClient_" + _CONFIGURATION.getSystemId());
        log.info("Client started for - {}", _CONFIGURATION.getSystemId());
        reconnectTears = new AtomicInteger(_CONFIGURATION.getReconnectTries());

        connectAndBind();

        submitSmWorkerThread.start();
        this.reconnect = true;
        try {
            submitSmWorkerThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while (clientIsRunning) {
            if (smppSession == null || smppSession.getSessionState().isBound()) connectAndBind();
            try {
                Thread.sleep(_CONFIGURATION.getReconnectTriesTime());
            } catch (InterruptedException e) {
                log.error(
                        "SMPP CLIENT [Host=" + _CONFIGURATION.getHost() +
                        ";Port=" + _CONFIGURATION.getPort() +
                        ";SystemId=" + _CONFIGURATION.getSystemId() +
                        "] - Thread InterruptedException in method connectAndBind().",
                        e
                );
            }
        }

        log.info("Client finished for - {}", _CONFIGURATION.getSystemId());
    }

    private void connectAndBind() {
        if (this.reconnect && (reconnectTears.get() == 0)) {
            clientAvailable.set(false);
            smppSession = null; // TODO: ???
            return;
        }

        if (smppSession != null) {
            // TODO: Разобраться с контроллером который тут должен был что-то делать
            clientAvailable.set(false);
            smppSession.unbindAndClose(); // TODO: ???
        }

        smppSession = new SMPPSession();
        smppSession.setPduProcessorDegree(_CONFIGURATION.getPduProcessorDegree());

        int timeOut = 2; // TODO: ???

        smppSession.setTransactionTimer(_CONFIGURATION.getTimeOut() == 0 ? timeOut : _CONFIGURATION.getTimeOut());

        String ton = _CONFIGURATION.getTon();
        String np = _CONFIGURATION.getNp();

        BindParameter bindParam = new BindParameter(
                BindType.valueOf(_CONFIGURATION.getBindType()),
                _CONFIGURATION.getSystemId(),
                _CONFIGURATION.getPassword(),
                _CONFIGURATION.getSystemType(),
                TypeOfNumber.valueOf(ton),
                NumberingPlanIndicator.valueOf(np),
                ""
        );

        try {
            smppSession.connectAndBind(
                    _CONFIGURATION.getHost(),
                    _CONFIGURATION.getPort(),
                    bindParam
            );

            smppSession.setMessageReceiverListener(new MessageReceiverListenerImpl(_CONFIGURATION));

            log.info(
                    "SMPP CLIENT [Host=" + _CONFIGURATION.getHost() +
                    ";Port=" + _CONFIGURATION.getPort() +
                    ";SystemId=" + _CONFIGURATION.getSystemId() +
                    "] IS BOUNDED TO SERVER"
            );

            reconnectTears = new AtomicInteger(_CONFIGURATION.getReconnectTries());
            clientAvailable.set(true);

        } catch (IOException e) {
            smppSession = null;
            log.warn(
                    "SMPP CLIENT [Host=" + _CONFIGURATION.getHost() +
                    ";Port=" + _CONFIGURATION.getPort() +
                    ";SystemId=" + _CONFIGURATION.getSystemId() +
                    "] - Failed initialize connection or bind. Reconnection after " + (_CONFIGURATION.getReconnectTriesTime()) +
                    " seconds. Remained [" + reconnectTears.decrementAndGet() + "] tries."
            );
            // try to reconnect after N seconds
            log.error(e.getMessage(), e);
        }
    }
}
