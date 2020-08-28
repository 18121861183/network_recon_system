package com.colasoft.tip.network.recon.enums;

public enum ReconFlagEnums {

    BATCH(0, "批量探测"), NOW(1, "立即探测");

    private Integer key;
    private String name;

    ReconFlagEnums(Integer key, String name) {
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
