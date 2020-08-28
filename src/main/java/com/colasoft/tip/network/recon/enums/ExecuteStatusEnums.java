package com.colasoft.tip.network.recon.enums;

import com.alibaba.fastjson.JSONObject;

public enum ExecuteStatusEnums {
    WAITING(0, "未执行"), RUNNING(1, "正在执行"), SUSPEND(2, "暂停执行"),
    CANCEL(3, "任务取消"), FINISH(4, "执行完成"), DELETE(5, "标记删除"), FAILED(-1, "执行失败");

    private Integer key;
    private String name;

    ExecuteStatusEnums(Integer key, String name) {
        this.key = key;
        this.name = name;
    }

    public static JSONObject getAllStatus() {
        JSONObject object = new JSONObject();
        object.put(String.valueOf(WAITING.key), WAITING.getName());
        object.put(String.valueOf(RUNNING.key), RUNNING.getName());
        object.put(String.valueOf(SUSPEND.key), SUSPEND.getName());
        object.put(String.valueOf(CANCEL.key), CANCEL.getName());
        object.put(String.valueOf(FINISH.key), FINISH.getName());
        object.put(String.valueOf(FAILED.key), FAILED.getName());
        return object;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
