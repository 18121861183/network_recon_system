package com.colasoft.tip.network.recon.dao;

import com.colasoft.tip.network.recon.bean.ZmapTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ZmapTaskRepository extends JpaRepository<ZmapTask, String> {

    Page<ZmapTask> findByScanTaskId(String scanTaskId, Pageable page);

    long countByScanTaskId(String scanTaskId);

    long countByScanTaskIdAndExecuteStatusAndUploadStatus(String id, Integer execute, Integer upload);

    List<ZmapTask> findByScanTaskId(String parentId);

    ZmapTask findTopByExecuteStatusOrderByPriorityAscCreateTimeAsc(Integer status);

    @Modifying
    @Query(value = "update ZmapTask set executeStatus=?2 where scanTaskId=?1")
    void updateStatusById(String taskId, Integer status);

    @Modifying
    @Query("update ZmapTask set executeStatus=4,uploadStatus=1 where id=?1")
    void finishAllChild(String taskId);


    List<ZmapTask> findByExecuteStatusAndUnfinishedBannerTaskAndUploadStatusOrderByPriorityAscCreateTimeAsc(Integer status, Integer count, Integer upload);

    List<ZmapTask> findByUploadStatus(Integer key);
}
