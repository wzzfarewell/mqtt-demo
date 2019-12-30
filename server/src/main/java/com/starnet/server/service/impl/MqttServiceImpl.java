package com.starnet.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.starnet.server.common.CmdPayload;
import com.starnet.server.common.Const;
import com.starnet.server.common.MqttProperties;
import com.starnet.server.mqtt.MqttPushClient;
import com.starnet.server.service.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * MqttServiceImpl
 *
 * @author wzzfarewell
 * @date 2019/12/4
 **/
@Slf4j
@Service
public class MqttServiceImpl implements MqttService {
    private final MqttProperties mqttProperties;

    private final RedisTemplate<String, String> redisTemplate;

    private MqttPushClient client;

    public MqttProperties getMqttProperties() {
        return mqttProperties;
    }

    public RedisTemplate<String, String> getRedisTemplate() {
        return redisTemplate;
    }

    @Autowired
    public MqttServiceImpl(MqttProperties mqttProperties, RedisTemplate<String, String> redisTemplate) {
        this.mqttProperties = mqttProperties;
        this.redisTemplate = redisTemplate;
        init();
    }

    private void init(){
        // 服务器客户端初始化，连接MQTT代理以及订阅一些主题
        client = MqttPushClient.getInstance(this);
        // 订阅设备遗嘱
        sub(Const.DEVICE_WILL_TOPIC);
        // 订阅设备上下线主题
        sub(Const.ON_OFF_LINE_TOPIC);
        // 订阅设备回复命令主题
        sub(Const.DEVICE_RESP_TOPIC);
    }

    @Override
    public void sub(String topic) {
        // 将客户端订阅过的主题存储在redis中
        String key = mqttProperties.getClientId() + "_Topics";
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
        client.subscribe(topic);
    }

    @Override
    public void pub(String topic, String msg) {
        client.publish(topic, msg);
    }

    @Override
    public void disconnect() {
        client.disconnect();
    }


    @Override
    public void pubAsyncCmd(Integer id) {
        String deviceId = Const.DEVICE_PREFIX + id;
        CmdPayload cmd = new CmdPayload();
        cmd.setDeviceId(deviceId);
        cmd.setCmdId(Const.CMD.CHECK_STATUS_CMD.getCode());
        cmd.setCmd(Const.CMD.CHECK_STATUS_CMD.getValue());
        cmd.setTypeCode(Const.CmdType.ASYNC.getCode());
        cmd.setType(Const.CmdType.ASYNC.getValue());
        cmd.setMsg("设备需要自检");
        cmd.setRespCode(Const.CmdResult.NO_RESP_CMD);
        pub(Const.SERVER_CMD_TOPIC + "/" + id, JSON.toJSONString(cmd));
    }

    @Override
    public void pubOpenDoorCmd(Integer id) {
        String deviceId = Const.DEVICE_PREFIX + id;
        CmdPayload cmd = new CmdPayload();
        cmd.setDeviceId(deviceId);
        cmd.setCmdId(Const.CMD.OPEN_DOOR_CMD.getCode());
        cmd.setCmd(Const.CMD.OPEN_DOOR_CMD.getValue());
        cmd.setTypeCode(Const.CmdType.SYNC.getCode());
        cmd.setType(Const.CmdType.SYNC.getValue());
        cmd.setMsg("设备[" + deviceId + "]，开门");
        cmd.setRespCode(Const.CmdResult.NO_RESP_CMD);
        pub(Const.SERVER_CMD_TOPIC + "/" + id, JSON.toJSONString(cmd));
    }
}
