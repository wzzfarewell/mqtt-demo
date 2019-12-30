package com.starnet.server.controller;

import com.starnet.server.service.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MQTTServerController
 *
 * @author wzzfarewell
 * @date 2019/11/18
 **/
@Slf4j
@RestController
public class MqttServerController {

    @Autowired
    private MqttService mqttService;

    @GetMapping("/")
    public String sayHello(){
        return "Hello MQTT!";
    }

    @PostMapping("/pub")
    public String send(@RequestParam(value = "topic", defaultValue = "默认主题") String topic,
                       @RequestParam(value = "msg", defaultValue = "默认消息") String msg) {
        log.info("请求参数[主题：{}, 消息：{}]", topic, msg);
        mqttService.pub(topic, msg);
        return "Publish Message Success!";
    }

    @PostMapping("/sub")
    public String sub(@RequestParam(defaultValue = "默认主题") String topic) {
        log.info("请求参数[主题：{}]", topic);
        mqttService.sub(topic);
        return "Subscribe Message Success!";
    }

    @GetMapping("/disconnect")
    public String disconnect(){
        mqttService.disconnect();
        return "client disconnect success!";
    }

    @PostMapping("/pubAsyncCmd")
    public String pubAsyncCmd(Integer id){
        mqttService.pubAsyncCmd(id);
        return "发布异步自检命令给设备[" + id + "]成功";
    }

    @PostMapping("/pubOpenDoorCmd")
    public String pubOpenDoorCmd(Integer id){
        mqttService.pubOpenDoorCmd(id);
        return "发布开门命令给设备[" + id + "]成功";
    }

}
