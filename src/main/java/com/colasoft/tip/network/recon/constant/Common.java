package com.colasoft.tip.network.recon.constant;

import com.alibaba.fastjson.JSONObject;
import com.colasoft.tip.network.recon.utils.FileUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

public class Common {

    // 默认协议默认端口
    public static JSONObject protocolPortMap = new JSONObject();

    // 默认端口对应协议
    public static JSONObject portProtocols = new JSONObject();

    // 默认国外扫描端口协议
    public static JSONObject foreignProtocols = new JSONObject();

    static {
        try {
            // 协议默认端口
            File file = ResourceUtils.getFile("classpath:json/protocol_port.json");
            String line = FileUtils.readFileLineByLine(file);
            protocolPortMap = JSONObject.parseObject(line);

            // 端口对应协议
            File file1 = ResourceUtils.getFile("classpath:json/foreign_protocols.json");
            String line1 = FileUtils.readFileLineByLine(file1);
            foreignProtocols = JSONObject.parseObject(line1);

            // 端口对应协议
            File file2 = ResourceUtils.getFile("classpath:json/port_protocols.json");
            String line2 = FileUtils.readFileLineByLine(file2);
            portProtocols = JSONObject.parseObject(line2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
