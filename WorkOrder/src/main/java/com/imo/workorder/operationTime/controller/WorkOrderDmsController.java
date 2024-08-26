package com.imo.workorder.operationTime.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.imo.workorder.operationTime.service.WorkOrderDmsService;

@RestController
@RequestMapping("imo/workorder/dms")
public class WorkOrderDmsController {

	@Autowired
	private WorkOrderDmsService woDmsService;

	@PostMapping("/WO_Operation_Confirmation/upload")
	public ResponseEntity<?> uploadWorkOrderDmsObj(@RequestParam(name = "file", required = true) MultipartFile file)
			throws Exception {
		return woDmsService.uploadWorkOrderDmsObj(file);
	}

	@GetMapping("/WO_Operation_Confirmation/download")
	public ResponseEntity<?> downloadWorkOrderDmsObj(@RequestParam(name = "objectId", required = true) String objectId)
			throws Exception {
		return woDmsService.downloadWorkOrderDmsObj(objectId);
	}

	@DeleteMapping("/WO_Operation_Confirmation/delete")
	public ResponseEntity<?> deleteWorkOrderDmsObj(@RequestParam(name = "objectId", required = true) String objectId)
			throws Exception {
		return woDmsService.deleteWorkOrderDmsObj(objectId, true);
	}
}
