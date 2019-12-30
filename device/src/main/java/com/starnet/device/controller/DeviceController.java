package com.starnet.device.controller;

import com.starnet.device.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * DeviceController
 *
 * @author wzzfarewell
 * @date 2019/12/13
 **/
@Slf4j
@RestController
public class DeviceController {
    @Autowired
    private DeviceService deviceService;

//    @GetMapping("/dump")
//    public String dumpOnlineClientsInfo(){
//        deviceService.dumpFile();
//        return "导出在线客户端信息成功！";
//    }
}
