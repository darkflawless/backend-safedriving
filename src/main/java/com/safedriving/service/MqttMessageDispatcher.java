package com.safedriving.service;

public interface MqttMessageDispatcher {

    void dispatch(String topic, String payload);
}
