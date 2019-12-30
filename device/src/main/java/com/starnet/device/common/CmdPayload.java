package com.starnet.device.common;

import lombok.Data;

import java.io.Serializable;

/**
 * CmdPayload
 * 服务器发给设备的指令消息
 * @author wzzfarewell
 * @date 2019/12/6
 **/
@Data
public class CmdPayload implements Serializable {
    private static final long serialVersionUID = 6925996708105153435L;

    private String deviceId;
    /**
     * 指令id
     */
    private Integer cmdId;
    private String cmd;
    /**
     * 指令类型码，1表示同步指令（需要立即回复），2表示异步指令（可不回复），0表示这是服务器发送的命令
     */
    private Integer typeCode;
    /**
     * 指令类型，sync 同步，async 异步
     */
    private String type;
    private String msg;
    /**
     * 指令响应码，1表示指令执行成功，2表示执行失败
     */
    private Integer respCode;
}
