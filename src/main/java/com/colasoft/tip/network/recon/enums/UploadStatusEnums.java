package com.colasoft.tip.network.recon.enums;

public enum  UploadStatusEnums {
    WAITING(0, "未上报"), UPLOAD(1, "已上报"), CANNOT(-1, "不可上报");

    private Integer key;
    private String name;

    UploadStatusEnums(Integer key, String name) {
        this.key = key;
        this.name = name;
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
