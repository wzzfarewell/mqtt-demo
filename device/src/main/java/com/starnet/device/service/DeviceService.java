package com.starnet.device.service;

import com.starnet.device.common.Const;
import com.starnet.device.common.MqttProperties;
import com.starnet.device.mqtt.MqttPushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * DeviceService
 *
 * @author wzzfarewell
 * @date 2019/12/6
 **/
@Slf4j
@Service
public class DeviceService {
    private final MqttProperties mqttProperties;

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public DeviceService(MqttProperties mqttProperties, RedisTemplate<String, String> redisTemplate) {
        this.mqttProperties = mqttProperties;
        this.redisTemplate = redisTemplate;
        deviceConnect();
    }

    public MqttProperties getMqttProperties() {
        return mqttProperties;
    }

    public RedisTemplate<String, String> getRedisTemplate() {
        return redisTemplate;
    }

    public void deviceConnect(){
        int totalClient = 20000;
        int threadNum = 200;
        final Semaphore semaphore = new Semaphore(threadNum);
        final CountDownLatch latch = new CountDownLatch(totalClient);
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 1; i <= totalClient; i++) {
            String deviceId = Const.DEVICE_PREFIX + i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        semaphore.acquire();
                        MqttPushClient client = new MqttPushClient(DeviceService.this, deviceId);
                        semaphore.release();
                    } catch (InterruptedException e) {
                        log.error("线程异常：{}", e.toString());
                    }
                    latch.countDown();
                }
            });
        }
//        executorService.shutdown();
    }

//    public void dumpFile(){
//        new MqttPushClient(this, "DumpClient")
//                .publish("DUMP", "dump");
//    }

}
