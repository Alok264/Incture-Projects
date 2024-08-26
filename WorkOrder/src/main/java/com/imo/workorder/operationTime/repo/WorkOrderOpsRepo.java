package com.imo.workorder.operationTime.repo;

import java.sql.Timestamp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.imo.workorder.operationTime.model.WorkOrderOpsId;
import com.imo.workorder.operationTime.model.WorkOrderOpsModel;

@Repository
public interface WorkOrderOpsRepo extends JpaRepository<WorkOrderOpsModel, WorkOrderOpsId> {

	@Query(value = "SELECT COUNT(*) FROM WORKORDER_OERATION_TIME wot WHERE wot.PLANT=?1  AND wot.WORKORDER=?2 AND wot.OPERATION=?3", nativeQuery = true)
	int getopscount(String plant, String wo, String ops);

	@Modifying
	@Transactional
	@Query(value = "UPDATE WORKORDER_OERATION_TIME hd SET hd.END_TIME =?4, hd.STATUS =?5 WHERE hd.PLANT =?1 AND hd.WORKORDER =?2 AND hd.OPERATION =?3", nativeQuery = true)
	int updateopsOnhold(String plant, String wo, String ops, Timestamp enddate, String status);

	@Modifying
	@Transactional
	@Query(value = "UPDATE WORKORDER_OERATION_TIME hd SET hd.END_TIME =?4, hd.STATUS =?5, hd.PAUSE_DURATION=?6 WHERE hd.PLANT =?1 AND hd.WORKORDER =?2 AND hd.OPERATION =?3", nativeQuery = true)
	int updateopsOnrestart(String plant, String wo, String ops, Timestamp enddate, String status, double duration);

	@Modifying
	@Transactional
	@Query(value = "UPDATE WORKORDER_OERATION_TIME hd SET hd.END_TIME =?4, hd.STATUS =?5 WHERE hd.PLANT =?1 AND hd.WORKORDER =?2 AND hd.OPERATION =?3", nativeQuery = true)
	int updateopsOncomplete(String plant, String wo, String ops, Timestamp enddate, String status);

	@Query(value = "SELECT wot.PAUSE_DURATION FROM WORKORDER_OERATION_TIME wot WHERE wot.PLANT=?1 AND wot.WORKORDER=?2 AND wot.OPERATION=?3", nativeQuery = true)
	double getpreviouspauseduration(String plant, String wo, String ops);

	@Query(value = "SELECT wot.* FROM WORKORDER_OERATION_TIME wot WHERE wot.PLANT=:plant AND wot.WORKORDER=:wo AND (:ops is null or wot.OPERATION = :ops)", nativeQuery = true)
	WorkOrderOpsModel getopsData(@Param("plant") String plant, @Param("wo") String workorder,
			@Param("ops") String operation);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE WORKORDER_OERATION_TIME hd SET hd.END_TIME =?4, hd.START_TIME=?5, hd.PAUSE_DURATION=?6, hd.STATUS =?7 WHERE hd.PLANT =?1 AND hd.WORKORDER =?2 AND hd.OPERATION =?3", nativeQuery = true)
	int updateopsTime(String plant, String wo, String ops, Timestamp enddate, Timestamp startdate, double duration, String status);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE WORKORDER_OERATION_TIME hd SET hd.START_TIME=?4 WHERE hd.PLANT =?1 AND hd.WORKORDER =?2 AND hd.OPERATION =?3", nativeQuery = true)
	int updateStartopsTime(String plant, String wo, String ops, Timestamp startdate);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE WORKORDER_OERATION_TIME hd SET hd.END_TIME =?4 WHERE hd.PLANT =?1 AND hd.WORKORDER =?2 AND hd.OPERATION =?3", nativeQuery = true)
	int updateEndopsTime(String plant, String wo, String ops, Timestamp enddate);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE WORKORDER_OERATION_TIME hd SET hd.END_TIME =?4, hd.START_TIME=?5, hd.PAUSE_DURATION=?6 WHERE hd.PLANT =?1 AND hd.WORKORDER =?2 AND hd.OPERATION =?3", nativeQuery = true)
	int massUpdateopsTime(String plant, String wo, String ops, Timestamp enddate, Timestamp startdate, double duration);

}
