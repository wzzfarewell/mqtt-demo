package com.starnet.server.mqtt;

import com.starnet.server.common.Const;
import com.starnet.server.common.MqttProperties;
import com.starnet.server.service.impl.MqttServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * MqttPushClient
 * 创建一个MQTT服务器客户端，因为服务器只有一个，单例模式创建实例
 * @author wzzfarewell
 * @date 2019/12/4
 **/
@Slf4j
public class MqttPushClient {
    private MqttClient client;
    private MqttServiceImpl mqttService;
    private MqttProperties mqttProperties;

    private static volatile MqttPushClient instance = null;

    /**
     * 创建一个双重锁的客户端单例
     * @param mqttService 业务处理对象
     * @return MQTT客户端实例
     */
    public static MqttPushClient getInstance(MqttServiceImpl mqttService){
        if(instance == null){
            synchronized (MqttPushClient.class){
                if(instance == null){
                    instance = new MqttPushClient(mqttService);
                }
            }
        }
        return instance;
    }

    private MqttPushClient(MqttServiceImpl mqttService) {
        log.info("MQTT客户端[{}]连接", mqttService.getMqttProperties().getClientId());
        this.mqttService = mqttService;
        this.mqttProperties = mqttService.getMqttProperties();
        connect();
    }

    /**
     * 客户端连接到MQTT服务器
     */
    private void connect(){
        try {
            client = new MqttClient(mqttProperties.getHost(), mqttProperties.getClientId(),
                    new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(mqttProperties.getCleanSession());
            options.setUserName(mqttProperties.getUsername());
            options.setPassword(mqttProperties.getPassword().toCharArray());
            options.setConnectionTimeout(mqttProperties.getConnectionTimeout());
            options.setKeepAliveInterval(mqttProperties.getKeepAliveInterval());
            options.setAutomaticReconnect(mqttProperties.getAutomaticReconnect());
            options.setWill(Const.DEVICE_WILL_TOPIC, "ServerOffline".getBytes(), 2, true);
            // 将业务处理对象赋给回调类
            client.setCallback(new PushCallback(mqttService));
            client.connect(options);
        } catch (MqttException e) {
            log.error("mqtt客户端[{}]连接异常：{}", mqttProperties.getClientId(), e.toString());
        }
    }

    public void publish(String topic, String data) {
        publish(topic, data, 1, false);
    }

    public void publish(String topic, String data, Integer qos, Boolean retained){
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(data.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        MqttTopic mqttTopic = client.getTopic(topic);
        try {
            MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
            token.waitForCompletion();
            log.info("[{}]发布了主题：[{}]，消息：[{}]", mqttProperties.getClientId(), topic, data);
        } catch (Exception e) {
            log.error("发布消息异常：{}", e.toString());
        }
    }

    public void subscribe(String topic){
        subscribe(topic, 1);
    }

    public void subscribe(String topic, int qos){
        try {
//            MqttTopic topic1 = client.getTopic(topic);
//            if(topic1 != null){
//                client.unsubscribe(topic);
//            }
            client.subscribe(topic, qos);
            log.info("{}订阅了主题：[{}], QOS[{}]", mqttProperties.getClientId(), topic, qos);
        } catch (MqttException e) {
            log.error("客户端订阅异常：{}", e.toString());
        }
    }

    public void disconnect(){
        try {
            client.disconnect();
            log.info("[{}]主动断开连接", mqttProperties.getClientId());
        } catch (MqttException e) {
            log.error("主动断开连接异常：{}", e.toString());
        }
    }
}
