package com.colasoft.tip.network.recon.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScanProperties {

    private String zmapResultPath;

    private String bannerSavePath;

    private String reportSavePath;

    private String zmapTaskPath;

    private String tempFilePath;

    private String nmapFilePath;

    private Integer recordMaxIps;

    private Integer nmapScanRate;

    private Integer normalScanRate;

    private String scanTaskSummary;

    private String centerUrl;

}
