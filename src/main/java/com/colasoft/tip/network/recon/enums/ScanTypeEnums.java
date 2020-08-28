package com.colasoft.tip.network.recon.enums;

public enum ScanTypeEnums {
    normal, deep;

    public static boolean contains(String type){
        for(ScanTypeEnums typeEnum : ScanTypeEnums.values()){
            if(typeEnum.name().equals(type)){
                return true;
            }
        }
        return false;
    }

}
