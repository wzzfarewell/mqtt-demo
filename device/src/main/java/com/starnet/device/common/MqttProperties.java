package com.starnet.device.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MqttProperties
 * MQTT客户端连接服务器所需设置的一些属性
 * @author wzzfarewell
 * @date 2019/12/4
 **/
@Component
@ConfigurationProperties(prefix = "mqtt-properties")
@Data
public class MqttProperties {
    private String host;
    private String clientId;
    private String username;
    private String password;
    private Boolean cleanSession;
    private Integer connectionTimeout;
    private Integer keepAliveInterval;
    private Boolean automaticReconnect;

}
