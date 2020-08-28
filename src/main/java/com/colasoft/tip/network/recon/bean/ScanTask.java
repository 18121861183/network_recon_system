package com.colasoft.tip.network.recon.bean;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * 从TDS接收的扫描任务
 */
@Getter
@Setter
@Entity
@Table(name = "scan_task")
public class ScanTask {

    @Id
    @Column(length = 40)
    private String id;

    // 扫描类型
    @Column(name = "scan_type", length = 50)
    private String scanType;

    // 扫描方法
    @Column(name = "scan_method", length = 50)
    private String scanMethod;

    // 扫描的IP
    @Column(name = "ip_list", columnDefinition = "text")
    private String ipList;

    // 扫描的端口
    @Column(name = "port", columnDefinition = "text")
    private String port;

    // 扫描协议
    @Column(name = "protocol", columnDefinition = "text")
    private String protocol;

    // 扫描速度
    @Column(name = "scan_speed", columnDefinition = "int default 0")
    private Integer scanSpeed;

    // 排除扫描的IP
    @Column(name = "exclude_list", columnDefinition = "text")
    private String excludeList;

    // 需要扫描的IP数量
    @Column(name = "ip_count", columnDefinition = "int default 0")
    private Integer ipCount;

    // 上传任务的用户
    @Column(length = 100)
    private String username;

    // 任务执行状态
    @Column(name = "execute_status", columnDefinition = "int default 0")
    private Integer executeStatus;

    // 子任务数量
    @Column(name = "child_task_count", columnDefinition = "int default 0")
    private Integer childTaskCount;

    // 已完成子任务数量
    @Column(name = "finish_child_task_count", columnDefinition = "int default 0")
    private Integer finishChildTaskCount;

    // 报告存储路径
    @Column(name = "report_path", length = 300)
    private String reportPath;

    // 探测优先级
    @Column(columnDefinition = "int default 5")
    private Integer priority;

    // 创建时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    private Date createTime;

    // 完成时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finish_time")
    private Date finishTime;


}
