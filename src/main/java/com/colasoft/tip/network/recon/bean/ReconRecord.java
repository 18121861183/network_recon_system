package com.colasoft.tip.network.recon.bean;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "recon_record")
public class ReconRecord {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    // IP数量
    @Column(name = "ip_count", columnDefinition = "int default 0")
    private Integer ipCount;

    // 执行指令
    @Column(name = "command", columnDefinition = "text")
    private String command;

    // 成功数量
    @Column(name = "success_count", columnDefinition = "int default 0")
    private Integer successCount;

    // 任务类型
    @Column(name = "task_type", length = 20)
    private String taskType;

    // 时间
    @Column(name = "create_time", columnDefinition = "datetime")
    private Date createTime;

}
