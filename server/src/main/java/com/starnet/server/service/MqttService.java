package com.starnet.server.service;

/**
 * MqttService
 *
 * @author wzzfarewell
 * @date 2019/12/4
 **/
public interface MqttService {

    void sub(String topic);

    void pub(String topic, String msg);

    void disconnect();

    void pubAsyncCmd(Integer id);

    void pubOpenDoorCmd(Integer id);

}
