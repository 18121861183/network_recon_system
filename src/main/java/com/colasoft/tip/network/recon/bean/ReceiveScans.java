package com.colasoft.tip.network.recon.bean;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "receive_scans")
public class ReceiveScans {

    @Id
    @Column(name = "ip", length = 40)
    private String ip;

    // 探测状态
    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer status;

    // 探测标记
    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer flag;


}
