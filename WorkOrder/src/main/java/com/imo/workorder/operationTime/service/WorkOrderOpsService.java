package com.imo.workorder.operationTime.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.imo.workorder.operationTime.dto.Workorderopsdto;
import com.imo.workorder.operationTime.dto.workOrderOpsUpdate;

public interface WorkOrderOpsService {

	public ResponseEntity<?> SaveWorkOrderOps(Workorderopsdto wc);

	public ResponseEntity<?> UpdateWorkOrderOps(workOrderOpsUpdate wc);

//	public double getWorkOrderOpsTime(String plant,String workorder, String ops);

	public ResponseEntity<?> getWorkOrderOps(String plant, String workorder, String ops);
}
