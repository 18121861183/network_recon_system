package com.colasoft.tip.network.recon.enums;

public enum ReconStatusEnums {

    WAITING(0, "等待扫描"),RUN(1, "已经提取到扫描队列"), DELETE(2, "等待删除");

    private Integer key;
    private String name;

    ReconStatusEnums(Integer key, String name) {
        this.key = key;
        this.name = name;
    }

//    public static String getKey(Integer key) {
//        for (ReconStatusEnums c : ReconStatusEnums.values()) {
//            if (c.getKey().equals(key)) {
//                return c.name;
//            }
//        }
//        return null;
//    }

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
