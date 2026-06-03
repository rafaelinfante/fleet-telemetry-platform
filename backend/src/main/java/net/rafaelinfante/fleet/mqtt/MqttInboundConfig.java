package net.rafaelinfante.fleet.mqtt;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageHeaders;

import net.rafaelinfante.fleet.config.MqttProperties;

/**
 * Subscribes to the device telemetry and status topics over MQTT v5 and feeds each message to
 * {@link MqttIngestBridge}. Reconnection is delegated to the Paho client (automaticReconnect);
 * we never tear down and rebuild the adapter to recover a dropped connection.
 */
@Configuration
public class MqttInboundConfig {

    @Bean
    Mqttv5PahoMessageDrivenChannelAdapter mqttInbound(MqttProperties properties) {
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setServerURIs(new String[]{properties.url()});
        options.setAutomaticReconnect(true);
        options.setCleanStart(true);
        options.setKeepAliveInterval(30);

        Mqttv5PahoMessageDrivenChannelAdapter adapter = new Mqttv5PahoMessageDrivenChannelAdapter(
                options, properties.clientId() + "-in", properties.telemetryTopic(), properties.statusTopic());
        adapter.setQos(properties.qos());
        return adapter;
    }

    @Bean
    IntegrationFlow mqttInboundFlow(Mqttv5PahoMessageDrivenChannelAdapter mqttInbound, MqttIngestBridge bridge) {
        return IntegrationFlow.from(mqttInbound)
                .handle(byte[].class, (byte[] payload, MessageHeaders headers) -> {
                    bridge.handle(payload, headers);
                    return null;
                })
                .get();
    }
}
