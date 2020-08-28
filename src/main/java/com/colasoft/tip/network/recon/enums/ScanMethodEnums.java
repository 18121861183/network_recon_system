package com.colasoft.tip.network.recon.enums;

public enum ScanMethodEnums {
    telnet, ping, socket;

    public static boolean contains(String type){
        for(ScanMethodEnums typeEnum : ScanMethodEnums.values()){
            if(typeEnum.name().equals(type)){
                return true;
            }
        }
        return false;
    }
}
