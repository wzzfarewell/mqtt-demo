package com.starnet.device.mqtt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.starnet.device.common.Const;
import com.starnet.device.common.MqttProperties;
import com.starnet.device.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * MqttPushClient
 * 创建一个MQTT设备客户端
 * @author wzzfarewell
 * @date 2019/12/4
 **/
@Slf4j
public class MqttPushClient {
    private MqttClient client;
    private DeviceService mqttService;
    private MqttProperties mqttProperties;

    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public MqttPushClient(DeviceService mqttService, String deviceId) {
        log.info("MQTT客户端[{}]连接", deviceId);
        this.mqttService = mqttService;
        this.mqttProperties = mqttService.getMqttProperties();
        this.deviceId = deviceId;
        init();
    }

    private void init(){
        connect();
        // 订阅服务器命令主题，例如：设备Device_12的命令主题：iot/server/cmd/12
        String deviceCmd = Const.SERVER_CMD_TOPIC + "/" +
                deviceId.substring(Const.DEVICE_PREFIX.length());
        subscribe(deviceCmd);
    }

    /**
     * 客户端连接到MQTT服务器
     */
    private void connect(){
        try {
            client = new MqttClient(mqttProperties.getHost(), deviceId,
                    new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(mqttProperties.getCleanSession());
            options.setUserName(mqttProperties.getUsername());
            options.setPassword(mqttProperties.getPassword().toCharArray());
            options.setConnectionTimeout(mqttProperties.getConnectionTimeout());
            options.setKeepAliveInterval(mqttProperties.getKeepAliveInterval());
            options.setAutomaticReconnect(mqttProperties.getAutomaticReconnect());
            // 设置设备遗嘱
            String willMsg = "设备[" + deviceId + "]的遗嘱";
            options.setWill(Const.DEVICE_WILL_TOPIC, willMsg.getBytes(), 2, true);
            // 将设备客户端赋给回调类
            client.setCallback(new PushCallback(mqttService, this));
            client.connect(options);
        } catch (MqttException e) {
            log.error("mqtt客户端[{}]连接异常：{}", deviceId, e.toString());
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
            log.info("[{}]发布了主题：[{}]，消息：[{}]", deviceId, topic, data);
        } catch (MqttException e) {
            log.error("发布消息异常：{}", e.toString());
        }
    }

    public void subscribe(String topic){
        subscribe(topic, 1);
    }

    public void subscribe(String topic, int qos){
        try {
            RedisTemplate<String, String> redisTemplate = mqttService.getRedisTemplate();
            // 将客户端订阅过的主题存储在redis中
            String key = deviceId + "_Topics";
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            String lstJson = redisTemplate.opsForValue().get(key);
            List<String> topics = new ArrayList<>();
            if(!StringUtils.isEmpty(lstJson)){
                topics = JSONObject.parseArray(lstJson, String.class);
            }
            if(!topics.contains(topic)){
                topics.add(topic);
            }
            redisTemplate.opsForValue().set(key, JSON.toJSON(topics).toString());
            client.subscribe(topic, qos);
            log.info("{}订阅了主题：[{}], QOS[{}]", deviceId, topic, qos);
        } catch (MqttException e) {
            log.error("客户端订阅异常：{}", e.toString());
        }
    }

    public void disconnect(){
        try {
            client.disconnect();
            log.info("[{}]主动断开连接", deviceId);
        } catch (MqttException e) {
            log.error("主动断开连接异常：{}", e.toString());
        }
    }
}
