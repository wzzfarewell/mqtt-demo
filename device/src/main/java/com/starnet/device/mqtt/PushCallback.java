package com.starnet.device.mqtt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.starnet.device.common.CmdPayload;
import com.starnet.device.common.Const;
import com.starnet.device.service.DeviceService;
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
    private DeviceService mqttService;
    private MqttPushClient client;

    public PushCallback(DeviceService mqttService, MqttPushClient client) {
        this.mqttService = mqttService;
        this.client = client;
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
        if (!StringUtils.isEmpty(client.getDeviceId())) {
            String deviceCmd = Const.SERVER_CMD_TOPIC + "/" +
                    client.getDeviceId().substring(Const.DEVICE_PREFIX.length());
            // 服务器发给当前设备的命令
            if (s.equals(deviceCmd)) {
                CmdPayload cmd = JSON.parseObject(payload, CmdPayload.class);
                if (cmd.getTypeCode().equals(Const.CmdType.SYNC.getCode())) {
                    // 服务器发送过来的是同步命令，需要回复
                    doSync(cmd);
                } else {
                    // 服务器发送过来的是异步命令，只执行，不回复
                    doAsync(cmd);
                }
            }
        }

    }

    private void doSync(CmdPayload cmd) {
        log.warn("接收到服务器命令：[{}], 命令详情：[{}]", cmd.getCmd(), cmd.getMsg());
        log.warn("执行命令：[{}]", cmd.getCmd());
        CmdPayload payload = new CmdPayload();
        payload.setDeviceId(client.getDeviceId());
        payload.setCmdId(cmd.getCmdId());
        payload.setCmd(cmd.getCmd());
        payload.setTypeCode(cmd.getTypeCode());
        payload.setType(cmd.getType());
        if (cmd.getCmdId().equals(Const.CMD.OPEN_DOOR_CMD.getCode())) {
            payload.setMsg("设备[" + client.getDeviceId() + "]开门成功。");
        }
        payload.setRespCode(Const.CmdResult.SUCCESS);
        client.publish(Const.DEVICE_RESP_TOPIC, JSON.toJSONString(payload));
    }

    private void doAsync(CmdPayload cmd) {
        log.info("接收到服务器命令：[{}], 命令详情：[{}]", cmd.getCmd(), cmd.getMsg());
        log.info("执行命令：[{}]", cmd.getCmd());
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
        String key = client.getDeviceId() + "_Topics";
        RedisTemplate<String, String> redisTemplate = mqttService.getRedisTemplate();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        String lstJson = redisTemplate.opsForValue().get(key);
        List<String> topics = new ArrayList<>();
        if (!StringUtils.isEmpty(lstJson)) {
            topics = JSONObject.parseArray(lstJson, String.class);
            for (String topic : topics) {
                client.subscribe(topic);
            }
        }
    }
}
