package com.starnet.server.mqtt;

import com.alibaba.fastjson.JSONObject;
import com.starnet.server.service.impl.MqttServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * <h3>PushCallback
 * 消息的回调类</h3>
 * <p>
 * 必须实现MqttCallback的接口并实现对应的相关接口方法。<br/>
 * MqttCallbackExtended接口继承了MqttCallBack接口，增加了一个方法。<br/>
 * 每个客户机标识都需要一个回调实例。
 *
 * @author wzzfarewell
 * @date 2019/11/18
 **/
@Slf4j
public class PushCallback implements MqttCallbackExtended {
    private MqttServiceImpl mqttService;

    public PushCallback(MqttServiceImpl mqttService) {
        this.mqttService = mqttService;
    }

    /**
     * 在这里处理接收到的消息。
     *
     * @param s           消息主题
     * @param mqttMessage 消息对象
     * @throws Exception
     */
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        // 订阅之后的消息执行到这里
        String payload = new String(mqttMessage.getPayload());
        log.info("主题：[{}]，内容: [{}]", s, payload);
    }

    /**
     * 接收到已经发布的 QoS 1 或 QoS 2 消息的传递令牌时调用。
     *
     * @param iMqttDeliveryToken 消息的传递令牌
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.info("发送完成?--->[{}]", iMqttDeliveryToken.isComplete());
    }

    /**
     * 此方法在客户端连接断开之后调用
     *
     * @param throwable
     */
    @Override
    public void connectionLost(Throwable throwable) {
        log.error("连接断开，可以重连...");
    }

    /**
     * 此方法在客户端连接成功之后调用
     *
     * @param b
     * @param s
     */
    @Override
    public void connectComplete(boolean b, String s) {
        // 重连后重新订阅之前订阅的topic
        String key = mqttService.getMqttProperties().getClientId() + "_Topics";
        RedisTemplate<String, String> redisTemplate = mqttService.getRedisTemplate();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        String lstJson = redisTemplate.opsForValue().get(key);
        List<String> topics = new ArrayList<>();
        if (!StringUtils.isEmpty(lstJson)) {
            topics = JSONObject.parseArray(lstJson, String.class);
        }
        for (String topic : topics) {
            mqttService.sub(topic);
        }
    }
}
