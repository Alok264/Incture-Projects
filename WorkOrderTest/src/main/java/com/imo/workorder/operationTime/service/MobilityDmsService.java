package com.imo.workorder.operationTime.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface MobilityDmsService {

	ResponseEntity<?> uploadMobilityDmsObj(MultipartFile file, String equipmentId) throws Exception;

	ResponseEntity<?> downloadMobilityDmsObj(String objectId) throws Exception;

	ResponseEntity<?> deleteMobilityDmsObj(String objectId, boolean flag) throws Exception;
}
