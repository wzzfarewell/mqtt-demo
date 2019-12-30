package com.starnet.server.common;

/**
 * Const
 * 常量类
 * @author wzzfarewell
 * @date 2019/12/6
 **/
public class Const {
    /**
     * 客户端设备id前缀
     */
    public static final String DEVICE_PREFIX = "Device_";
    /**
     * 设备上下线消息主题，订阅之后每当有客户端上下线都会发送通知给这个主题
     */
    public static final String ON_OFF_LINE_TOPIC = "client/notice/status/onoffline";
    /**
     * 设备遗嘱主题
     */
    public static final String DEVICE_WILL_TOPIC = "iot/device/will-topic";
    /**
     * 服务器指令主题
     */
    public static final String SERVER_CMD_TOPIC = "iot/server/cmd";
    /**
     * 设备响应命令主题
     */
    public static final String DEVICE_RESP_TOPIC = "iot/device/resp";

    public enum CMD{
        /**
         * 开门指令
         */
        OPEN_DOOR_CMD(1001, "openDoor"),
        /**
         * 自检指令
         */
        CHECK_STATUS_CMD(1002, "checkStatus");
        private int code;
        private String value;

        CMD(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }
    }

    public enum CmdType{
        /**
         * 同步命令状态码
         */
        SYNC(1, "sync"),
        /**
         * 异步命令状态码
         */
        ASYNC(2, "async");
        private int code;
        private String value;

        CmdType(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }
    }

    public interface CmdResult{
        /**
         * 表示这是服务器发送的命令
         */
        int NO_RESP_CMD = 0;
        /**
         * 命令执行成功
         */
        int SUCCESS = 1;
        /**
         * 命令执行失败
         */
        int FAIL = 2;
    }

}
