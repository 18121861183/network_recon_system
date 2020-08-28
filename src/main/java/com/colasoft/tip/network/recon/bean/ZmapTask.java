package com.colasoft.tip.network.recon.bean;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * zmap获取开放端口
 */
@Setter
@Getter
@Entity
@Table(name = "zmap_task")
public class ZmapTask {

    @Id
    @Column(length = 40)
    private String id;

    @Column(name = "scan_task_id", length = 40)
    private String scanTaskId;

    // 扫描指令
    @Column(columnDefinition = "text", nullable = false)
    private String command;

    // 端口
    @Column(columnDefinition = "text", nullable = false)
    private String port;

    // 探测协议，多个
    @Column(columnDefinition = "text", nullable = false)
    private String protocol;

    // 扫描的IP, 非网段时使用×代替
    @Column(name = "ip_range", columnDefinition = "text")
    private String ipRange;

    // 扫描IP的数量
    @Column(name = "ip_count", columnDefinition = "int default 0")
    private Integer ipCount;

    // 扫描成功的数量
    @Column(name = "open_port_count", columnDefinition = "int default 0")
    private Integer openPortCount;

    // 扫描结果存储路径
    @Column(name = "zmap_result_path")
    private String zmapResultPath;

    // 执行状态
    @Column(name = "execute_status", columnDefinition = "int default 0")
    private Integer executeStatus;

    // banner任务下发状态
    @Column(name = "send_banner_status", columnDefinition = "int default 0")
    private Integer sendBannerStatus;

    // 未完成子任务数量
    @Column(name = "unfinished_banner_task", columnDefinition = "int default 0")
    private Integer unfinishedBannerTask;

    // 报告存储路径
    @Column(name = "report_path", length = 300)
    private String reportPath;

    // 报告文件MD5
    @Column(name = "report_md5", length = 40)
    private String reportMd5;

    // 报告文件大小
    @Column(name = "report_size", columnDefinition = "int default 0")
    private Long reportSize;

    // 上报到数据中心状态
    @Column(name = "upload_status", columnDefinition = "int default -1")
    private Integer uploadStatus;

    // 探测优先级
    @Column(columnDefinition = "int default 5")
    private Integer priority;

    // 创建时间
    @Column(name = "create_time", columnDefinition = "datetime")
    private Date createTime;

    // 完成时间
    @Column(name = "finish_time", columnDefinition = "datetime")
    private Date finishTime;


    @Override
    public String toString() {
        return "ZmapTask{" +
                "id='" + id + '\'' +
                ", scanTaskId='" + scanTaskId + '\'' +
                ", command='" + command + '\'' +
                ", port='" + port + '\'' +
                ", protocol='" + protocol + '\'' +
                ", ipRange='" + ipRange + '\'' +
                ", ipCount=" + ipCount +
                ", openPortCount=" + openPortCount +
                ", zmapResultPath='" + zmapResultPath + '\'' +
                ", executeStatus=" + executeStatus +
                ", sendBannerStatus=" + sendBannerStatus +
                ", unfinishedBannerTask=" + unfinishedBannerTask +
                ", reportPath='" + reportPath + '\'' +
                ", reportMd5='" + reportMd5 + '\'' +
                ", reportSize=" + reportSize +
                ", uploadStatus=" + uploadStatus +
                ", priority=" + priority +
                ", createTime=" + createTime +
                ", finishTime=" + finishTime +
                '}';
    }
}
