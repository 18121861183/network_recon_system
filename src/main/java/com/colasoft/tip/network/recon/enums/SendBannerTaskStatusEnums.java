package com.colasoft.tip.network.recon.enums;

public enum SendBannerTaskStatusEnums {

    WAITING(0, "未下发"), FINISH(1, "已下发"), FAILED(-1, "下发失败");

    private Integer key;
    private String name;

    SendBannerTaskStatusEnums(Integer key, String name) {
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
