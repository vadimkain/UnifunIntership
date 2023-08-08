package com.smpp.client.submit_sm;

import com.smpp.client.SmppClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class SmppClientSubmitSmWorker implements Runnable {

    private SmppClient client;
    private short speed = 10;
    private volatile long sleepTime;
    private static final Logger log = LoggerFactory.getLogger(SmppClientSubmitSmWorker.class);

    public SmppClientSubmitSmWorker(SmppClient client) {
        this.client = client;
        this.sleepTime =
                1000 / client.get_CONFIGURATION().getSpeedLimit() == 0
                        ? this.speed
                        : client.get_CONFIGURATION().getSpeedLimit();
    }

    @Override
    public void run() {
        log.info("Submit_SM Worker started for client - {}", client.get_CONFIGURATION().getSystemId());
        Thread.currentThread().setName("_THREAD-submit_" + client.get_CONFIGURATION().getSystemId());

        while (!Thread.currentThread().isInterrupted()) {
            if (!client.getSmsToSend().isEmpty() && client.getSmppSession() != null) {
                Thread submit = new Thread(new SubmitSm(
                        sleepTime,
                        client,
                        client.getSmsToSend().poll()
                ));

                submit.start();

                try {
                    submit.join();
                } catch (InterruptedException e) {
                    log.warn("Thread {} has been interrupted", Thread.currentThread().getName());
                }

                try {
                    TimeUnit.MICROSECONDS.sleep(sleepTime);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            } else {
//                client.stop();
            }
        }

        log.info("Submit_SM Worker stopped for client - {}", client.get_CONFIGURATION().getSystemId());
    }
}
