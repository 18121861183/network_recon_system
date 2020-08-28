package com.colasoft.tip.network.recon.service;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

public interface ReceiveTaskService {

    void insertScanTask(String scanType, String scanMethod, String ipList, String excludeList, String protocols, String ports, String pps, String username) throws IOException;

    JSONObject scanTaskSearch(Integer page, Integer size, Integer status);

    boolean checkTask(String id);

    JSONObject taskDetailQuery(String id, Integer page, Integer size);

    void taskOperation(String id, String operation);

    JSONObject taskSummary(String id, Integer page, Integer size);
}
