package com.colasoft.tip.network.recon.dao;

import com.colasoft.tip.network.recon.bean.BannerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BannerTaskRepository extends JpaRepository<BannerTask, String> {

    long countByExecuteStatus(Integer status);

    @Modifying
    @Query("update BannerTask b set b.executeStatus=?1 where b.zmapId=?2 and (b.executeStatus=2 or b.executeStatus=3)")
    void updateExecuteStatusByParentId(Integer status, String parentId);

    @Modifying
    @Query("update BannerTask b set b.executeStatus=?1 where b.zmapId=?2")
    void restartTaskByParentId(Integer status, String parentId);

    @Modifying
    @Query("update BannerTask b set b.executeStatus=?1 where b.zmapId=?2 and (b.executeStatus=0 or b.executeStatus=1)")
    void updateRunningTaskStop(Integer key, String taskId);

    BannerTask findTopByExecuteStatusOrderByPriorityAscCreateTimeAsc(Integer key);

    List<BannerTask> findAllByZmapId(String id);

}
