package com.imo.workorder.operationTime.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.imo.workorder.operationTime.model.WorkOrderDms;

public interface WorkOrderDmsRepo extends JpaRepository<WorkOrderDms, String> {

}
