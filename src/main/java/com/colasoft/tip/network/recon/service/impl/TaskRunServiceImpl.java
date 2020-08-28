package com.colasoft.tip.network.recon.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.colasoft.tip.network.recon.bean.BannerTask;
import com.colasoft.tip.network.recon.bean.ReconRecord;
import com.colasoft.tip.network.recon.bean.ScanTask;
import com.colasoft.tip.network.recon.bean.ZmapTask;
import com.colasoft.tip.network.recon.config.NetworkReconParameterProvider;
import com.colasoft.tip.network.recon.config.ScanProperties;
import com.colasoft.tip.network.recon.dao.*;
import com.colasoft.tip.network.recon.enums.ExecuteStatusEnums;
import com.colasoft.tip.network.recon.enums.TaskTypeEnums;
import com.colasoft.tip.network.recon.enums.UploadStatusEnums;
import com.colasoft.tip.network.recon.utils.CommandUtils;
import com.colasoft.tip.network.recon.utils.FileUtils;
import com.colasoft.tip.network.recon.utils.HashUtils;
import com.colasoft.tip.network.recon.utils.HttpPostUtil;
import com.google.common.collect.HashMultimap;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPOutputStream;

@Component("taskRunService")
public class TaskRunServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunServiceImpl.class);

    @Resource
    private NetworkReconParameterProvider networkReconParameterProvider;

    @Resource
    private BannerTaskRepository bannerTaskRepository;

    @Resource
    private ReceiveScansRepository receiveScansRepository;

    @Resource
    private ReconRecordRepository reconRecordRepository;

    @Resource
    private ScanTaskRepository scanTaskRepository;

    @Resource
    private ZmapTaskRepository zmapTaskRepository;

    @PostConstruct
    private void initPaths() {
        ScanProperties scan = networkReconParameterProvider.getScan();

        String zmapTaskPath = scan.getZmapTaskPath();
        if (!zmapTaskPath.endsWith("/")) {
            zmapTaskPath += "/";
            networkReconParameterProvider.getScan().setZmapTaskPath(zmapTaskPath);
        }
        if (!new File(zmapTaskPath).exists()) {
            new File(zmapTaskPath).mkdirs();
        }

        String zmapResultPath = scan.getZmapResultPath();
        if (!zmapResultPath.endsWith("/")) {
            zmapResultPath += "/";
            networkReconParameterProvider.getScan().setZmapResultPath(zmapResultPath);
        }
        if (!new File(zmapResultPath).exists()) {
            new File(zmapResultPath).mkdirs();
        }

        String bannerSavePath = scan.getBannerSavePath();
        if (!bannerSavePath.endsWith("/")) {
            bannerSavePath += "/";
            networkReconParameterProvider.getScan().setBannerSavePath(bannerSavePath);
        }
        if (!new File(bannerSavePath).exists()) {
            new File(bannerSavePath).mkdirs();
        }

        String nmapFilePath = scan.getNmapFilePath();
        if (!nmapFilePath.endsWith("/")) {
            nmapFilePath += "/";
            networkReconParameterProvider.getScan().setNmapFilePath(nmapFilePath);
        }
        if (!new File(nmapFilePath).exists()) {
            new File(nmapFilePath).mkdirs();
        }

        String reportSavePath = scan.getReportSavePath();
        if (!reportSavePath.endsWith("/")) {
            reportSavePath += "/";
            networkReconParameterProvider.getScan().setReportSavePath(reportSavePath);
        }
        if (!new File(reportSavePath).exists()) {
            new File(reportSavePath).mkdirs();
        }

        String tempFilePath = scan.getTempFilePath();
        if (!tempFilePath.endsWith("/")) {
            tempFilePath += "/";
            networkReconParameterProvider.getScan().setTempFilePath(tempFilePath);
        }
        if (!new File(tempFilePath).exists()) {
            new File(tempFilePath).mkdirs();
        }
    }

    public void initSystemData() {
        ScanProperties scan = networkReconParameterProvider.getScan();
        String zmapTaskPath = scan.getZmapTaskPath();
        String zmapResultPath = scan.getZmapResultPath();
        String bannerSavePath = scan.getBannerSavePath();
        String nmapFilePath = scan.getNmapFilePath();
        String reportSavePath = scan.getReportSavePath();
        String tempFilePath = scan.getTempFilePath();

        // 执行指令集合
        List<String> commandList = new ArrayList<>();
        commandList.add("rm -rf " + zmapTaskPath + "*");
        commandList.add("rm -rf " + zmapResultPath + "*");
        commandList.add("rm -rf " + bannerSavePath + "*");
        commandList.add("rm -rf " + nmapFilePath + "*");
        commandList.add("rm -rf " + reportSavePath + "*");
        commandList.add("rm -rf " + tempFilePath + "*");

        for (String command: commandList) {
            try {
                CommandUtils.justRunCommand(command);
            } catch (InterruptedException | IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }

        bannerTaskRepository.deleteAll();
        receiveScansRepository.deleteAll();
        reconRecordRepository.deleteAll();
        scanTaskRepository.deleteAll();
        zmapTaskRepository.deleteAll();

    }


    /**
     * 探测任务第一步，zmap任务开启
     */
    @Scheduled(fixedDelay = 5*1000)
    @Async
    public void startZmap() {
        long count = bannerTaskRepository.countByExecuteStatus(ExecuteStatusEnums.WAITING.getKey());
        if (count > 0) {
            return;
        }

        ZmapTask task = zmapTaskRepository.findTopByExecuteStatusOrderByPriorityAscCreateTimeAsc(ExecuteStatusEnums.WAITING.getKey());
        if (task != null) {
            logger.info("开始执行zmap探测任务，执行指令："+task.getCommand());
            try {
                CommandUtils.runCommandOutputLogger(logger, task.getCommand());
                Integer numbers = FileUtils.getFileLineNumbers(task.getZmapResultPath());

                if (numbers ==null || numbers == 0) {
                    Optional<ScanTask> byId = scanTaskRepository.findById(task.getScanTaskId());
                    if (!byId.isPresent()) {
                        logger.error("错误的探测记录，请确认是否存在垃圾数据...");
                        logger.error("错误记录："+task.toString());
                        task.setExecuteStatus(ExecuteStatusEnums.FAILED.getKey());
                        zmapTaskRepository.saveAndFlush(task);
                        return;
                    }
                    ScanTask scanTask = byId.get();
                    Integer finishCount = scanTask.getFinishChildTaskCount();
                    Integer allCount = scanTask.getChildTaskCount();
                    finishCount++;
                    if (finishCount.equals(allCount)) {
                        scanTask.setExecuteStatus(ExecuteStatusEnums.FINISH.getKey());
                    }
                    scanTask.setFinishChildTaskCount(finishCount);
                    scanTaskRepository.saveAndFlush(scanTask);
                    task.setExecuteStatus(ExecuteStatusEnums.FINISH.getKey());
                    zmapTaskRepository.saveAndFlush(task);
                    return;
                }
                List<String> ports;
                String port = task.getPort();
                if (port.indexOf(",") > 0) {
                    ports = Arrays.asList(port.split(","));
                } else {
                    ports = Collections.singletonList(port);
                }

                String protocol = task.getProtocol();
                String[] protocols = protocol.split(",");

                int taskTotal = 0;
                for (String pt: ports) {
                    for (String proto: protocols) {
                        if (networkReconParameterProvider.getForbiddenProtocols().contains(proto)) {
                            continue;
                        }
                        String bannerSavePath = networkReconParameterProvider.getScan().getBannerSavePath();
                        String bannerFile = bannerSavePath+proto+"_"+pt+"_"+task.getId()+"_"+".json";
                        String[] bannerCommand = new String[] {"zgrab2", "-f", task.getZmapResultPath(), proto, "-p", pt,
                                "-t 5s", "--output-file="+bannerFile};

                        String bannerId = HashUtils.sha1(String.join(" ", bannerCommand));
                        BannerTask bannerTask = new BannerTask();
                        bannerTask.setId(bannerId);
                        bannerTask.setCommand(String.join(" ", bannerCommand));
                        bannerTask.setPort(Integer.parseInt(pt));
                        bannerTask.setProtocol(proto);
                        bannerTask.setIpCount(numbers);
                        bannerTask.setZmapId(task.getId());
                        bannerTask.setExecuteStatus(ExecuteStatusEnums.WAITING.getKey());
                        bannerTask.setBannerSavePath(bannerFile);
                        bannerTask.setPriority(5);
                        bannerTask.setCreateTime(new Date());
                        bannerTaskRepository.save(bannerTask);
                        taskTotal++;
                    }
                }

                task.setExecuteStatus(ExecuteStatusEnums.FINISH.getKey());
                task.setOpenPortCount(numbers);
                task.setUnfinishedBannerTask(taskTotal);
                zmapTaskRepository.saveAndFlush(task);

                ScanTask scanTask = scanTaskRepository.getFirstById(task.getScanTaskId());
                Integer finishChildTaskCount = scanTask.getFinishChildTaskCount();
                finishChildTaskCount++;
                scanTask.setFinishChildTaskCount(finishChildTaskCount);
                scanTaskRepository.saveAndFlush(scanTask);

                // 插入到探测日志
                insertLog(task.getCommand(), task.getIpCount(), task.getOpenPortCount(), TaskTypeEnums.open_port.name());

            } catch (InterruptedException | TimeoutException | IOException e) {
                e.printStackTrace();
                zmapTaskRepository.updateStatusById(task.getId(), ExecuteStatusEnums.FAILED.getKey());
            }
        }
    }


    /**
     * 探测第二步，获取banner
     */
    @Scheduled(fixedDelay = 5*1000)
    @Async
    public void startBanner() {
        BannerTask task = bannerTaskRepository.findTopByExecuteStatusOrderByPriorityAscCreateTimeAsc(ExecuteStatusEnums.WAITING.getKey());
        if (task != null) {
            logger.info("开始执行banner获取，执行指令："+task.getCommand());
            try {
                CommandUtils.runCommandOutputLogger(logger, task.getCommand());
                String bannerSavePath = task.getBannerSavePath();

                int successCount = 0;
                BufferedReader reader = new BufferedReader(new FileReader(bannerSavePath));
                String temp = null;
                while ((temp = reader.readLine()) != null) {
                    if (!temp.contains("\"error\":\"")) {
                        successCount++;
                    }
                }
                reader.close();

                long bannerSize = new File(bannerSavePath).length();
                task.setExecuteStatus(ExecuteStatusEnums.FINISH.getKey());
                task.setSuccessCount(successCount);
                task.setBannerFileSize(bannerSize);
                task.setFinishTime(new Date());
                bannerTaskRepository.saveAndFlush(task);

                // 更新上级任务完成状态和数量
                ZmapTask zmapTask = zmapTaskRepository.getOne(task.getZmapId());
                Integer bannerCount = zmapTask.getUnfinishedBannerTask();
                bannerCount--;
                if (bannerCount==0) {
                    zmapTask.setUnfinishedBannerTask(bannerCount);
                    zmapTask.setExecuteStatus(ExecuteStatusEnums.FINISH.getKey());
                    zmapTask.setFinishTime(new Date());
                    zmapTaskRepository.saveAndFlush(zmapTask);
                } else {
                    zmapTask.setUnfinishedBannerTask(bannerCount);
                    zmapTaskRepository.saveAndFlush(zmapTask);
                }

                ScanTask scanTask = scanTaskRepository.getOne(zmapTask.getScanTaskId());
                Integer count = scanTask.getFinishChildTaskCount();
                count++;
                if (count.equals(scanTask.getChildTaskCount())) {
                    scanTask.setFinishChildTaskCount(count);
                    scanTask.setExecuteStatus(ExecuteStatusEnums.FINISH.getKey());
                    scanTaskRepository.saveAndFlush(scanTask);
                } else {
                    scanTask.setFinishChildTaskCount(count);
                    scanTaskRepository.saveAndFlush(scanTask);
                }

                insertLog(task.getCommand(), task.getIpCount(), successCount, TaskTypeEnums.banner.name());

            } catch (InterruptedException | TimeoutException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    /**
     * 探测第三步，探测结果打包存储
     */
    @Scheduled(fixedDelay = 5*1000)
    @Async
    public void taskReportFinish() {
        List<ZmapTask> tasks = zmapTaskRepository.findByExecuteStatusAndUnfinishedBannerTaskAndUploadStatusOrderByPriorityAscCreateTimeAsc(ExecuteStatusEnums.FINISH.getKey(),
                0, UploadStatusEnums.CANNOT.getKey());
        if (tasks.size()>0) {
            String reportSavePath = networkReconParameterProvider.getScan().getReportSavePath() + simpleDateFormat.format(new Date()) + "/";
            for (ZmapTask task: tasks) {
                try {
                    String summaryPath = networkReconParameterProvider.getScan().getScanTaskSummary() + task.getScanTaskId() + "/";
                    if (!new File(summaryPath).exists()) {
                        new File(summaryPath).mkdirs();
                    }

                    Map<String, String> bannerFileMap = new HashMap<>();

                    List<BannerTask> allBanners = bannerTaskRepository.findAllByZmapId(task.getId());
                    CSVPrinter printer = CSVFormat.EXCEL.withDelimiter(',').withQuote('\"').print(new File(summaryPath+task.getId()+".json"), StandardCharsets.UTF_8);
                    for (BannerTask bannerTask: allBanners) {
                        String bannerSavePath = bannerTask.getBannerSavePath();
                        bannerFileMap.put(bannerSavePath, "banner_"+bannerTask.getProtocol()+"_"+bannerTask.getPort()+".csv");

                        String temp;
                        BufferedReader reader = new BufferedReader(new FileReader(bannerSavePath));
                        while ((temp = reader.readLine()) != null) {
                            if (temp.contains("\"status\":\"success\"")) {
                                try {
                                    JSONObject object = JSONObject.parseObject(temp);
                                    String ip = object.getString("ip");
                                    printer.printRecord(ip, bannerTask.getProtocol(), bannerTask.getPort());
                                }catch (Exception ignored){}
                            }
                        }
                    }
                    printer.flush();
                    printer.close();

                    JSONObject params = new JSONObject();
                    params.put("port", task.getPort());
                    params.put("count", task.getIpCount());
                    params.put("ip_range", task.getIpRange());
                    params.put("protocols", task.getProtocol());
                    params.put("time", task.getFinishTime().getTime());

                    String paramPath = networkReconParameterProvider.getScan().getTempFilePath()+"/param.json";
                    BufferedWriter writer = new BufferedWriter(new FileWriter(paramPath));
                    writer.write(params.toJSONString());
                    writer.flush();
                    writer.close();

                    String reportFilePath = reportSavePath + task.getId() + '_' + task.getPort() + ".tar.gz";

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    TarArchiveOutputStream tarArchiveOutputStream = null;
                    GZIPOutputStream gzipOutputStream = null;
                    try {
                        tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream);
                        // 将所有文件打包成 tar文件
                        try {
                            tarArchiveOutputStream.putArchiveEntry(new TarArchiveEntry(new File(task.getZmapResultPath()), "zmap_"+task.getPort()+".csv"));
                            tarArchiveOutputStream.putArchiveEntry(new TarArchiveEntry(new File(paramPath), "param.json"));
                            for (String source : bannerFileMap.keySet()) {
                                File file = new File(source);
                                tarArchiveOutputStream.putArchiveEntry(new TarArchiveEntry(file, bannerFileMap.get(source)));
                                IOUtils.copy(new FileInputStream(file), tarArchiveOutputStream);
                                tarArchiveOutputStream.closeArchiveEntry();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            tarArchiveOutputStream.flush();
                            tarArchiveOutputStream.close();
                        }
                        gzipOutputStream = new GZIPOutputStream(new FileOutputStream(reportFilePath));
                        gzipOutputStream.write(byteArrayOutputStream.toByteArray());
                    } finally {
                        byteArrayOutputStream.close();
                        if(gzipOutputStream != null) {
                            gzipOutputStream.flush();
                            gzipOutputStream.close();
                        }
                    }
                    File file = new File(reportFilePath);
                    String md5 = FileUtils.getMD5(file);
                    long length = file.length();
                    task.setReportPath(reportFilePath);
                    task.setReportMd5(md5);
                    task.setReportSize(length);
                    task.setUploadStatus(UploadStatusEnums.WAITING.getKey());
                    zmapTaskRepository.saveAndFlush(task);

                    if (bannerFileMap.size()>0) {
                        Files.delete(Paths.get(task.getZmapResultPath()));
                        for (String path: bannerFileMap.keySet()) {
                            Files.delete(Paths.get(path));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("生成探测报告失败: "+task.getCommand());
                }

            }

        }
    }


    /**
     * 探测第四步，统计概要结果
     */
    @Scheduled(fixedDelay = 5*1000)
    @Async
    public void startTaskSummary() {
        HashMultimap<String, String> multimap = HashMultimap.create();


    }


    /**
     * 探测第五步，探测结果回传到中心
     */
    @Scheduled(fixedDelay = 5*1000)
    @Async
    public void sendReportToCenter() {
        List<ZmapTask> uploadStatus = zmapTaskRepository.findByUploadStatus(UploadStatusEnums.WAITING.getKey());
        if (uploadStatus!=null && uploadStatus.size()>0) {
            for (ZmapTask task: uploadStatus) {
                try {
                    HttpPostUtil postUtil = new HttpPostUtil(networkReconParameterProvider.getScan().getCenterUrl());
                    postUtil.addParameter("file", FileUtils.getFileBytes(task.getReportPath()), task.getId()+"tar.gz");
                    postUtil.send();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }


    private void insertLog(String command, Integer totalCount, Integer successCount, String taskType) {

        ReconRecord record = new ReconRecord();
        record.setId(HashUtils.getUuid32());
        record.setCreateTime(new Date());
        record.setCommand(command);
        record.setIpCount(totalCount);
        record.setSuccessCount(successCount);
        record.setTaskType(taskType);

        reconRecordRepository.save(record);

    }

}
