package com.colasoft.tip.network.recon.controller;

import com.alibaba.fastjson.JSONObject;
import com.colasoft.tip.network.recon.constant.Common;
import com.colasoft.tip.network.recon.enums.ExecuteStatusEnums;
import com.colasoft.tip.network.recon.enums.ScanMethodEnums;
import com.colasoft.tip.network.recon.enums.ScanTypeEnums;
import com.colasoft.tip.network.recon.enums.TaskOperationEnums;
import com.colasoft.tip.network.recon.service.ReceiveTaskService;
import com.colasoft.tip.network.recon.service.impl.TaskRunServiceImpl;
import com.colasoft.tip.network.recon.utils.HashUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
public class ReceiveTaskController {

    @Resource
    private ReceiveTaskService receiveTaskService;

    @Resource
    private TaskRunServiceImpl taskRunService;

    @Value("${administrators-password}")
    private String rootPassword;

    /**
     * 获取支持的协议以及协议默认端口
     */
    @RequestMapping("/protocols_info.json")
    @ResponseBody
    public String getProtocolInfo() {
        JSONObject object = new JSONObject();
        object.put("port_protocol", Common.portProtocols);
        object.put("protocol_port", Common.protocolPortMap);
        return object.toJSONString();
    }

    /**
     * 扫描日志生成
     */
    @RequestMapping("/insert_task.do")
    @ResponseBody
    public String generateTask(HttpServletRequest request) throws IOException {
        String scanType = request.getParameter("scan_type");
        if (!ScanTypeEnums.contains(scanType)) {
            return "{\"msg\", \"非法请求！参数错误！\"}";
        }
        String ipList = request.getParameter("ip_list");
        if (StringUtils.isEmpty(ipList)) {
            return "{\"msg\", \"缺少必要的参数!\"}";
        }
        String excludeList = request.getParameter("exclude_list");
        String pps = request.getParameter("pps");
        if (StringUtils.isEmpty(pps)) {
            pps = "200";
        }
        String scanMethod = request.getParameter("scan_method");
        if (!ScanMethodEnums.contains(scanMethod)) {
            return "{\"msg\", \"非法请求！参数错误！\"}";
        }
        String protocols = request.getParameter("protocols");
        String ports = request.getParameter("ports");
        if (ScanTypeEnums.normal.name().equals(scanType)) {
            if (StringUtils.isEmpty(protocols) || StringUtils.isEmpty(ports)) {
                return "{\"msg\", \"缺少必要的参数!\"}";
            }
        }
        String username = request.getParameter("username");
        String id = HashUtils.sha1(username + scanType + scanMethod + pps + protocols + ipList + excludeList);
        if (receiveTaskService.checkTask(id)) {
            return "{\"msg\", \"任务已存在!\"}";
        } else {
            receiveTaskService.insertScanTask(scanType, scanMethod, ipList, excludeList, protocols, ports, pps, username);
            return "{\"msg\", \"success\"}";
        }
    }

    /**
     * 任务状态获取列表
     */
    @RequestMapping("/task_status.json")
    @ResponseBody
    public String scanTaskStatus() {
        JSONObject allStatus = ExecuteStatusEnums.getAllStatus();
        return allStatus.toJSONString();
    }

    /**
     * 查询扫描任务列表
     */
    @RequestMapping("/query_task.json")
    @ResponseBody
    public String scanTaskQuery(@RequestParam(name = "page", required = true, defaultValue = "1") Integer page,
                                @RequestParam(name = "size", required = true, defaultValue = "10") Integer size,
                                @RequestParam(name = "status", required = false) Integer status) {
        JSONObject object = receiveTaskService.scanTaskSearch(page, size, status);
        return object.toJSONString();
    }

    /**
     * 任务详情查询
     */
    @RequestMapping("/task_detail.json")
    @ResponseBody
    public String taskDetail(@RequestParam(name = "id", required = true) String id,
                             @RequestParam(name = "page", required = true, defaultValue = "1") Integer page,
                             @RequestParam(name = "size", required = true, defaultValue = "10") Integer size) {

        JSONObject object = receiveTaskService.taskDetailQuery(id, page, size);
        return object.toJSONString();
    }


    /**
     * 查询任务完成概要信息
     * @param id    任务ID
     * @param page  页数
     * @param size  大小
     * @return 详情
     */
    @PostMapping("/task_summary.json")
    @ResponseBody
    public String taskSummary(@RequestParam(name = "id", required = true) String id,
                              @RequestParam(name = "page", required = true, defaultValue = "1") Integer page,
                              @RequestParam(name = "size", required = true, defaultValue = "20") Integer size) {
        JSONObject object = receiveTaskService.taskSummary(id, page, size);
        return object.toJSONString();
    }


    @PostMapping("/task_operation.do")
    @ResponseBody
    public String taskOperation(@RequestParam(name = "id", required = true) String id,
                                @RequestParam(name = "operation", required = true) String operation) {
        if (!TaskOperationEnums.getAll().contains(operation)) {
            return "{\"msg\", \"缺少必要的参数或参数错误!\"}";
        }
        receiveTaskService.taskOperation(id, operation);
        return "{\"msg\": \"success\"}";
    }


    @RequestMapping("/init_system.do")
    @ResponseBody
    public String initSystem(@RequestParam(name = "password", required = true) String password) {
        if (!rootPassword.equals(password)) {
            return "{\"msg\": \"认证密码错误...\"}";
        }
        taskRunService.initSystemData();
        return "{\"msg\": \"success\"}";
    }

}
