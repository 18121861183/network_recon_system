package com.colasoft.tip.network.recon.dao;

import com.colasoft.tip.network.recon.bean.ScanTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ScanTaskRepository extends JpaRepository<ScanTask, String> {

    Page<ScanTask> findScanTaskByExecuteStatusNot(Integer status, Pageable page);

    long countScanTaskByExecuteStatusNot(Integer status);

    long countScanTaskByExecuteStatus(Integer status);

    Page<ScanTask> findScanTaskByExecuteStatus(Integer status, Pageable page);

    ScanTask getFirstById(String id);

//    List<ScanTask> findAllByExecuteStatusOrderByPriorityAscCreateTimeDesc(Integer status);

    @Modifying
    @Query("update ScanTask set executeStatus=?2 where id=?1")
    void updateStatusById(String id, Integer status);

    @Modifying
    @Query("update ScanTask set finishChildTaskCount=?2 where id=?1")
    void updateFinishCount(String scanTaskId, Integer current);

    @Modifying
    @Query("update ScanTask set finishChildTaskCount=?2,executeStatus=4 where id=?1")
    void updateFinishCountAndExecuteStatus(String scanTaskId, Integer finishCount);

}
