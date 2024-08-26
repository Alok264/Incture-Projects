package com.imo.workorder.operationTime.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface WorkOrderDmsService {
	public ResponseEntity<?> uploadWorkOrderDmsObj(MultipartFile file) throws Exception;

	public ResponseEntity<?> downloadWorkOrderDmsObj(String objectId) throws Exception;

	public ResponseEntity<?> deleteWorkOrderDmsObj(String objectId, boolean flag) throws Exception;
}
