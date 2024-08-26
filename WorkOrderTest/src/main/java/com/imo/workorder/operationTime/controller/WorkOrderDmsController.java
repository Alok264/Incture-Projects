package com.imo.workorder.operationTime.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.imo.workorder.operationTime.service.WorkOrderDmsService;

@RestController
@RequestMapping("/imo/workorder/dms")
// Test
public class WorkOrderDmsController {

	@Autowired
	private WorkOrderDmsService woDmsService;

	@PostMapping("/WO_Operation_Confirmation/upload")
	public ResponseEntity<?> uploadWorkOrderDmsObj(@RequestParam(name = "file", required = true) MultipartFile file)
			throws Exception {
		return woDmsService.uploadWorkOrderDmsObj(file);
	}

	@GetMapping("/WO_Operation_Confirmation/download/{objectId}")
	public ResponseEntity<?> downloadWorkOrderDmsObj(@PathVariable String objectId) throws Exception {
		return woDmsService.downloadWorkOrderDmsObj(objectId);
	}

	@DeleteMapping("/WO_Operation_Confirmation/delete/{objectId}")
	public ResponseEntity<?> deleteWorkOrderDmsObj(@PathVariable String objectId) throws Exception {
		return woDmsService.deleteWorkOrderDmsObj(objectId, true);
	}

	@PostMapping("/WO_Operation_Confirmation/multi/upload")
	public ResponseEntity<?> uploadMultipleFiles(@RequestParam(name = "files", required = true) MultipartFile[] files)
			throws Exception {
		return woDmsService.uploadMultipleFiles(files);
	}

	@GetMapping("/WO_Operation_Confirmation/token")
	public String getAccessToken() throws Exception {
		return woDmsService.getAccessToken();
	}
}
