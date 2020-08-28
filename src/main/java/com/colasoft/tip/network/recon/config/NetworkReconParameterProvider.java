package com.colasoft.tip.network.recon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
@Configuration
@ConfigurationProperties(prefix = "network-recon")
public class NetworkReconParameterProvider {

    private List<String> forbiddenProtocols;

    private ScanProperties scan = new ScanProperties();

    private FtpProperties ftp = new FtpProperties();

    public List<String> getForbiddenProtocols() {
        return forbiddenProtocols;
    }

    public void setForbiddenProtocols(List<String> forbiddenProtocols) {
        this.forbiddenProtocols = forbiddenProtocols;
    }

    public ScanProperties getScan() {
        return scan;
    }

    public void setScan(ScanProperties scan) {
        this.scan = scan;
    }

    public FtpProperties getFtp() {
        return ftp;
    }

    public void setFtp(FtpProperties ftp) {
        this.ftp = ftp;
    }
}
