package com.colasoft.tip.network.recon.bean;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "banner_task")
public class BannerTask {

    @Id
    @Column(length = 40)
    private String id;

    // 扫描指令
    @Column(columnDefinition = "text")
    private String command;

    // 扫描端口
    @Column(columnDefinition = "int default 0")
    private Integer port;

    // 扫描协议
    @Column(length = 100)
    private String protocol;

    // IP数量
    @Column(columnDefinition = "int default 0")
    private Integer ipCount;

    // 上级任务ID
    @Column(name = "zmap_id", length = 40)
    private String zmapId;

    // 执行状态
    @Column(name = "execute_status", columnDefinition = "int default 0")
    private Integer executeStatus;

    // 执行结果存储位置
    @Column(name = "banner_save_path", length = 300)
    private String bannerSavePath;

    // 执行结果文件大小
    @Column(name = "banner_file_size", columnDefinition = "int default 0")
    private long bannerFileSize;

    // 成功获取banner数量
    @Column(name = "success_count", columnDefinition = "int default 0")
    private Integer successCount;

    // 执行优先级，数字越小优先级越高
    @Column(name = "priority", columnDefinition = "int default 5")
    private Integer priority;

    // 任务创建时间
    @Column(name = "create_time", columnDefinition = "datetime")
    private Date createTime;

    // 扫描完成时间
    @Column(name = "finish_time", columnDefinition = "datetime")
    private Date finishTime;

}
