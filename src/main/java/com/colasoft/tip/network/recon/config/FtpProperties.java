package com.colasoft.tip.network.recon.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FtpProperties {

    private boolean enable;

    private String sftpHost;

    private Integer sftpPort;

    private String sftpUsername;

    private String sftpPassword;

    private String sftpRemote;


}
