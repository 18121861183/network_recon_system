package com.colasoft.tip.network.recon.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.colasoft.tip.network.recon.bean.BannerTask;
import com.colasoft.tip.network.recon.bean.ScanTask;
import com.colasoft.tip.network.recon.bean.ZmapTask;
import com.colasoft.tip.network.recon.config.NetworkReconParameterProvider;
import com.colasoft.tip.network.recon.dao.BannerTaskRepository;
import com.colasoft.tip.network.recon.dao.ScanTaskRepository;
import com.colasoft.tip.network.recon.dao.ZmapTaskRepository;
import com.colasoft.tip.network.recon.enums.*;
import com.colasoft.tip.network.recon.service.ReceiveTaskService;
import com.colasoft.tip.network.recon.utils.FileUtils;
import com.colasoft.tip.network.recon.utils.HashUtils;
import com.colasoft.tip.network.recon.utils.Ipv4Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ReceiveTaskServiceImpl implements ReceiveTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveTaskServiceImpl.class);

    @Resource
    private ScanTaskRepository scanTaskRepository;

    @Resource
    private ZmapTaskRepository zmapTaskRepository;

    @Resource
    private BannerTaskRepository bannerTaskRepository;

    @Resource
    private NetworkReconParameterProvider networkReconParameterProvider;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss_");

    @Override
    public void insertScanTask(String scanType, String scanMethod, String ipList, String excludeList, String protocols, String ports, String pps, String username) throws IOException {
        String id = HashUtils.sha1(username + scanType + scanMethod + pps + protocols + ipList + excludeList);

        Map<String, Object> generalFileInfo = getFileList(ipList, excludeList);
        Integer total = (Integer) generalFileInfo.get("total");
        if (total > 0) {
            throw new RuntimeException("未解析出有效的IP，请检查IP是否正确或者被白名单过滤...");
        }
        if (ScanTypeEnums.deep.name().equals(scanType)) {
            ports = "1-65535";
        }
        List<Object> portList = new ArrayList<>();
        if (ports.indexOf("-") > 0) {
            portList = new ArrayList<>();
            String[] split = ports.split("-");
            int start = Integer.parseInt(split[0]);
            int end = Integer.parseInt(split[1]);
            for (int i=start;i<=end;i++) {
                portList.add(i);
            }
        } else if (!StringUtils.isEmpty(ports)) {
            String[] split = ports.split(",");
            portList = Arrays.asList(split);
        }

        List<String> protocolList = Arrays.asList(protocols.split(","));
        int taskCount = portList.size() * protocolList.size();
        if (ScanMethodEnums.telnet.name().equals(scanMethod)) {
            taskCount += portList.size();
        } else if (ScanMethodEnums.ping.name().equals(scanMethod)) {
            taskCount++;
        }

        ScanTask scanTask = new ScanTask();
        scanTask.setId(id);
        scanTask.setScanMethod(scanMethod);
        scanTask.setScanSpeed(Integer.parseInt(pps));
        scanTask.setScanType(scanType);
        scanTask.setIpList(ipList);
        scanTask.setPort(ports);
        scanTask.setProtocol(protocols);
        scanTask.setExcludeList(excludeList);
        scanTask.setIpCount(total);
        scanTask.setUsername(username);
        scanTask.setExecuteStatus(ExecuteStatusEnums.WAITING.getKey());
        scanTask.setChildTaskCount(taskCount);
        scanTask.setFinishChildTaskCount(0);
        scanTask.setPriority(5);
        scanTask.setCreateTime(new Date());
        scanTaskRepository.save(scanTask);

        String zmapResultPath = networkReconParameterProvider.getScan().getZmapResultPath();
        zmapResultPath = fileMkdirs(zmapResultPath);
        String bannerSavePath = networkReconParameterProvider.getScan().getBannerSavePath();
        bannerSavePath = fileMkdirs(bannerSavePath);

        Integer executeStatus = ExecuteStatusEnums.WAITING.getKey();
        if (ScanMethodEnums.socket.name().equals(scanMethod)) {
            executeStatus = ExecuteStatusEnums.FINISH.getKey();
        }
        List<String> allFiles = (List<String>) generalFileInfo.get("files");
        for (String path: allFiles) {
            int number = (int) generalFileInfo.get(path);
            if (ScanMethodEnums.ping.name().equals(scanMethod)) {
                String resultPath = zmapResultPath + getFilename(path);
                String[] command = new String[]{"zmap", "-w", path, "-r", pps, " | ztee", resultPath};
                String zmapId = HashUtils.sha1(String.join(" ", command));
                ZmapTask task = new ZmapTask();
                task.setId(zmapId);
                task.setCommand(String.join(" ", command));
                task.setScanTaskId(id);
                task.setPort(com.colasoft.tip.network.recon.utils.StringUtils.join(",", portList));
                task.setProtocol(protocols);
                task.setPriority(5);
                task.setIpRange("*");
                task.setIpCount(number);
                task.setOpenPortCount(0);
                task.setZmapResultPath(resultPath);
                task.setExecuteStatus(executeStatus);
                task.setCreateTime(new Date());
                zmapTaskRepository.save(task);
            } else {
                for (Object port: portList) {
                    String resultPath = zmapResultPath + getFilename(path) + "_" + port.toString();
                    String[] command = new String[]{"zmap", "-w", path, "-r", pps, "-p", port.toString(), " | ztee", resultPath};
                    String zmapId = HashUtils.sha1(String.join(" ", command));

                    ZmapTask task = new ZmapTask();
                    task.setId(zmapId);
                    task.setCommand(String.join(" ", command));
                    task.setScanTaskId(id);
                    task.setPort(port.toString());
                    task.setProtocol(protocols);
                    task.setPriority(5);
                    task.setIpRange("*");
                    task.setIpCount(number);
                    task.setOpenPortCount(0);
                    task.setZmapResultPath(resultPath);
                    task.setUnfinishedBannerTask(protocolList.size());
                    task.setExecuteStatus(executeStatus);
                    task.setCreateTime(new Date());
                    zmapTaskRepository.save(task);

                    if (ScanMethodEnums.socket.name().equals(scanMethod)) {

                        for (String proto: protocolList) {
                            if (networkReconParameterProvider.getForbiddenProtocols().contains(proto)) {
                                continue;
                            }
                            String bannerFile = bannerSavePath+proto+"_"+port.toString()+"_"+zmapId+"_"+".json";
                            String[] bannerCommand = new String[] {"zgrab2", "-f", path, proto, "-p", port.toString(),
                                    "-t 5s", "--output-file="+bannerFile};

                            String bannerId = HashUtils.sha1(String.join(" ", bannerCommand));
                            BannerTask bannerTask = new BannerTask();
                            bannerTask.setId(bannerId);
                            bannerTask.setCommand(String.join(" ", bannerCommand));
                            bannerTask.setPort(Integer.parseInt(port.toString()));
                            bannerTask.setProtocol(proto);
                            bannerTask.setIpCount(number);
                            bannerTask.setZmapId(zmapId);
                            bannerTask.setExecuteStatus(ExecuteStatusEnums.WAITING.getKey());
                            bannerTask.setBannerSavePath(bannerFile);
                            bannerTask.setPriority(5);
                            bannerTask.setCreateTime(new Date());
                            bannerTaskRepository.save(bannerTask);

                        }

                    }
                }

            }
        }

    }

    private String fileMkdirs(String zmapResultPath) {
        if (!zmapResultPath.endsWith("/")) {
            zmapResultPath += "/";
        }
        if (!new File(zmapResultPath).exists()) {
            boolean mkdirs = new File(zmapResultPath).mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("无法创建目录：" + zmapResultPath + "，请确认权限或者磁盘等...");
            }
        }
        return zmapResultPath;
    }

    private String getFilename(String path) {
        if (!StringUtils.isEmpty(path)) {
            String[] split = path.split("/");
            return split[split.length - 1];
        }
        return null;
    }

    private Map<String, Object> getFileList(String ipList, String excludeList) throws IOException {
        List<String> inList = new ArrayList<>();
        List<String> excList = new ArrayList<>();
        if (!StringUtils.isEmpty(ipList)) {
            inList.addAll(Arrays.asList(ipList.split(",")));
        }
        if (!StringUtils.isEmpty(excludeList)) {
            excList.addAll(Arrays.asList(excludeList.split(",")));
        }
        // 解析所有符合条件IP
        List<String> allIp = getSingerIpList(inList, excList);
        if (allIp.size() == 0) {
            return null;
        }
        // 单个任务最大IP数量
        Integer recordMaxIps = networkReconParameterProvider.getScan().getRecordMaxIps();
        // 扫描IP存储路径
        String scanFilePath = networkReconParameterProvider.getScan().getZmapTaskPath();
        if (!scanFilePath.endsWith("/")) {
            scanFilePath += "/";
        }
        String filenameStart = simpleDateFormat.format(new Date());
        scanFilePath += filenameStart.substring(0,6)+"/";
        if (!new File(scanFilePath).exists()) {
            boolean mkdirs = new File(scanFilePath).mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("无法创建目录："+ scanFilePath + "，请确认权限或者磁盘等...");
            }
        }

        List<String> fileList = new ArrayList<>();

        Map<String, Object> result = new HashMap<>();

        int totalCount = 0;
        int count = 0;
        int fileCount = 1;
        String fileFullPath = scanFilePath + filenameStart + fileCount;
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileFullPath));
        fileList.add(fileFullPath);
        for (String ip: allIp) {
            if (count >= recordMaxIps) {
                writer.flush();
                writer.close();
                result.put(fileFullPath, count);
                totalCount += count;
                fileCount++;
                count = 0;
                fileFullPath = scanFilePath + filenameStart + fileCount;
                writer = new BufferedWriter(new FileWriter(fileFullPath));
                fileList.add(fileFullPath);
            }
            writer.write(ip+"\n");
            count++;
        }
        writer.flush();
        writer.close();

        if (count > 0) {
            totalCount += count;
            result.put(fileFullPath, count);
        }
        result.put("total", totalCount);
        result.put("files", fileList);
        return result;
    }

    private List<String> getSingerIpList(List<String> inList, List<String> excList) {
        List<String> result = new ArrayList<>();
        for (String mask: inList) {
            if (mask.indexOf("/") > 0) {
                String startIp = Ipv4Utils.getStartIp(mask);
                String endIp = Ipv4Utils.getEndIp(mask);
                if (startIp != null && endIp != null) {
                    long start = Ipv4Utils.ipToLong(startIp);
                    long end = Ipv4Utils.ipToLong(endIp);
                    for (long i=start;i<=end;i++) {
                        result.add(Ipv4Utils.long2Ip(i));
                    }
                }
                
            } else if (mask.indexOf("-") > 0) {
                String[] strings = mask.split("-");
                long start = Ipv4Utils.ipToLong(strings[0]);
                long end = Ipv4Utils.ipToLong(strings[1]);
                if (start < end) {
                    for (long i=start;i<=end;i++) {
                        result.add(Ipv4Utils.long2Ip(i));
                    }
                }
            } else {
                if (Ipv4Utils.isIPv4Address(mask)) {
                    result.add(mask);
                }
            }

        }

        for (String ext: excList) {
            if (ext.indexOf("/") > 0) {
                String startIp = Ipv4Utils.getStartIp(ext);
                String endIp = Ipv4Utils.getEndIp(ext);
                if (startIp != null && endIp != null) {
                    long start = Ipv4Utils.ipToLong(startIp);
                    long end = Ipv4Utils.ipToLong(endIp);
                    for (long i=start;i<=end;i++) {
                        result.remove(Ipv4Utils.long2Ip(i));
                    }
                }

            } else if (ext.indexOf("-") > 0) {
                String[] strings = ext.split("-");
                long start = Ipv4Utils.ipToLong(strings[0]);
                long end = Ipv4Utils.ipToLong(strings[1]);
                if (start < end) {
                    for (long i=start;i<=end;i++) {
                        result.remove(Ipv4Utils.long2Ip(i));
                    }
                }
            } else {
                if (Ipv4Utils.isIPv4Address(ext)) {
                    result.remove(ext);
                }
            }
        }

        return result;
    }

    @Override
    public JSONObject scanTaskSearch(Integer page, Integer size, Integer status) {
        JSONObject result = new JSONObject();
        if (StringUtils.isEmpty(status)) {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "create_time");
            Page<ScanTask> task = scanTaskRepository.findScanTaskByExecuteStatusNot(ExecuteStatusEnums.DELETE.getKey(), pageRequest);
            long count = scanTaskRepository.countScanTaskByExecuteStatusNot(ExecuteStatusEnums.DELETE.getKey());
            result.put("record_size", count);
            result.put("record_list", task);
            result.put("page", page);
        } else {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "create_time");
            Page<ScanTask> task = scanTaskRepository.findScanTaskByExecuteStatus(status, pageRequest);
            long count = scanTaskRepository.countScanTaskByExecuteStatus(status);
            result.put("record_size", count);
            result.put("record_list", task);
            result.put("page", page);
        }
        return result;
    }

    @Override
    public boolean checkTask(String id) {
        return scanTaskRepository.findById(id).isPresent();
    }

    @Override
    public JSONObject taskDetailQuery(String id, Integer page, Integer size) {
        ScanTask scanTask = scanTaskRepository.getFirstById(id);
        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.ASC, "create_time");
        Page<ZmapTask> task = zmapTaskRepository.findByScanTaskId(id, pageable);
        long count = zmapTaskRepository.countByScanTaskId(id);
        long finish = zmapTaskRepository.countByScanTaskIdAndExecuteStatusAndUploadStatus(id, ExecuteStatusEnums.FINISH.getKey(), UploadStatusEnums.UPLOAD.getKey());
        JSONObject result = new JSONObject();
        result.put("main", scanTask);
        result.put("finished_sub_task_count", finish);
        result.put("all_sub_task_count", count);
        result.put("sub_task_list", task);
        return result;
    }

    @Override
    public void taskOperation(String id, String operation) {
        ScanTask scanTask = scanTaskRepository.getFirstById(id);
        Integer executeStatus = scanTask.getExecuteStatus();
        if ("start".equals(operation)) {
            if (ExecuteStatusEnums.SUSPEND.getKey().equals(executeStatus)) {
                // 暂停状态恢复执行
                List<ZmapTask> zmapTasks = zmapTaskRepository.findByScanTaskId(id);
                for (ZmapTask task: zmapTasks) {
                    String taskId = task.getId();
                    bannerTaskRepository.updateExecuteStatusByParentId(ExecuteStatusEnums.WAITING.getKey(), taskId);
                    if (task.getExecuteStatus().equals(ExecuteStatusEnums.CANCEL.getKey()) ||
                            task.getExecuteStatus().equals(ExecuteStatusEnums.SUSPEND.getKey())) {
                        zmapTaskRepository.updateStatusById(taskId, ExecuteStatusEnums.WAITING.getKey());
                    }
                }
                scanTaskRepository.updateStatusById(id, ExecuteStatusEnums.RUNNING.getKey());
            } else if (ExecuteStatusEnums.CANCEL.getKey().equals(executeStatus)) {
                // 取消任务恢复执行
                List<ZmapTask> zmapTasks = zmapTaskRepository.findByScanTaskId(id);
                for (ZmapTask task: zmapTasks) {
                    String taskId = task.getId();
                    bannerTaskRepository.restartTaskByParentId(ExecuteStatusEnums.WAITING.getKey(), taskId);
                    if (task.getExecuteStatus().equals(ExecuteStatusEnums.CANCEL.getKey()) ||
                            task.getExecuteStatus().equals(ExecuteStatusEnums.SUSPEND.getKey())) {
                        zmapTaskRepository.updateStatusById(taskId, ExecuteStatusEnums.WAITING.getKey());
                    }
                }
                scanTaskRepository.updateStatusById(id, ExecuteStatusEnums.RUNNING.getKey());
            }

        } else if ("stop".equals(operation)) {
            scanTaskRepository.updateStatusById(id, ExecuteStatusEnums.SUSPEND.getKey());
            List<ZmapTask> byScanTaskId = zmapTaskRepository.findByScanTaskId(id);
            for (ZmapTask task: byScanTaskId) {
                String taskId = task.getId();
                bannerTaskRepository.updateRunningTaskStop(ExecuteStatusEnums.SUSPEND.getKey(), taskId);
                if (task.getExecuteStatus().equals(ExecuteStatusEnums.WAITING.getKey())) {
                    zmapTaskRepository.updateStatusById(taskId, ExecuteStatusEnums.SUSPEND.getKey());
                }
            }
            if (executeStatus.equals(ExecuteStatusEnums.RUNNING.getKey())) {
                scanTaskRepository.updateStatusById(id, ExecuteStatusEnums.SUSPEND.getKey());
            }
        } else if ("delete".equals(operation)) {
            scanTaskRepository.updateStatusById(id, ExecuteStatusEnums.DELETE.getKey());
            List<ZmapTask> byScanTaskId = zmapTaskRepository.findByScanTaskId(id);
            for (ZmapTask task: byScanTaskId) {
                String taskId = task.getId();
                bannerTaskRepository.updateRunningTaskStop(ExecuteStatusEnums.DELETE.getKey(), taskId);
                zmapTaskRepository.updateStatusById(taskId, ExecuteStatusEnums.DELETE.getKey());
            }
        } else if ("cancel".equals(operation)) {
            if (executeStatus.equals(ExecuteStatusEnums.RUNNING.getKey())) {
                scanTaskRepository.updateStatusById(id, ExecuteStatusEnums.CANCEL.getKey());
            }
            List<ZmapTask> byScanTaskId = zmapTaskRepository.findByScanTaskId(id);
            for (ZmapTask task: byScanTaskId) {
                String taskId = task.getId();
                bannerTaskRepository.updateRunningTaskStop(ExecuteStatusEnums.CANCEL.getKey(), taskId);
                if (task.getExecuteStatus().equals(ExecuteStatusEnums.WAITING.getKey())) {
                    zmapTaskRepository.updateStatusById(taskId, ExecuteStatusEnums.CANCEL.getKey());
                }
            }
        }
    }

    @Override
    public JSONObject taskSummary(String id, Integer page, Integer size) {
        JSONObject result = new JSONObject();
        ScanTask scanTask = scanTaskRepository.getFirstById(id);
        if (scanTask==null) {
            result.put("msg", "错误的任务号..."+id);
            return result;
        }
        if (StringUtils.isEmpty(scanTask.getReportPath())) {
            result.put("msg", "暂未生成概要报告，请稍后再查看");
            return result;
        }
        try {
            String line = FileUtils.readFileLineByLine(new File(scanTask.getReportPath()));
            JSONObject object = JSONObject.parseObject(line);
            JSONArray detail = object.getJSONArray("detail");
            int from = (page-1) * size;
            int to = from + size;
            object.put("detail", detail.subList(from, to));
            return object;
        } catch (Exception e) {
            result.put("msg", "概要报告读取失败："+e.getMessage());
            return result;
        }
    }

}
