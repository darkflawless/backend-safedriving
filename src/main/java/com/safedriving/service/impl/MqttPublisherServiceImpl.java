package com.safedriving.service.impl;

import com.safedriving.service.MqttPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MqttPublisherServiceImpl implements MqttPublisherService {

    private final MessageChannel mqttOutboundChannel;

    @Autowired
    public MqttPublisherServiceImpl(@Autowired(required = false) @Qualifier("mqttOutboundChannel") MessageChannel mqttOutboundChannel) {
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    @Override
    public void publish(String topic, String payload) {
        if (mqttOutboundChannel == null) {
            log.warn("⚠️ MQTT Outbound Channel chưa sẵn sàng hoặc MQTT bị tắt. Bỏ qua gửi tới: {}", topic);
            return;
        }

        try {
            Message<String> message = MessageBuilder.withPayload(payload)
                    .setHeader(MqttHeaders.TOPIC, topic)
                    .build();
            mqttOutboundChannel.send(message);
            log.info("📤 [MQTT Published] Topic: {}, Payload: {}", topic, payload);
        } catch (Exception e) {
            log.error("❌ Lỗi xuất bản MQTT [topic={}]: {}", topic, e.getMessage(), e);
        }
    }
}
