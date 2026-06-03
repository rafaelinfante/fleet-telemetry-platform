package net.rafaelinfante.fleet.web;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import net.rafaelinfante.fleet.web.dto.AlertView;
import net.rafaelinfante.fleet.web.dto.DeviceView;

/**
 * Pushes live updates to subscribed dashboards over STOMP. When called inside a transaction the
 * send is deferred until after commit, so a rolled-back or retried unit of work never broadcasts
 * an alert or device state that was not actually persisted.
 */
@Component
public class TelemetryBroadcaster {

    public static final String TOPIC_DEVICES = "/topic/devices";
    public static final String TOPIC_ALERTS = "/topic/alerts";

    private final SimpMessagingTemplate messagingTemplate;

    public TelemetryBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastDevice(DeviceView device) {
        send(TOPIC_DEVICES, device);
    }

    public void broadcastAlert(AlertView alert) {
        send(TOPIC_ALERTS, alert);
    }

    private void send(String destination, Object payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend(destination, payload);
                }
            });
        } else {
            messagingTemplate.convertAndSend(destination, payload);
        }
    }
}
