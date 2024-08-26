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

import com.imo.workorder.operationTime.service.MobilityDmsService;

@RestController
@RequestMapping("/imo/mobility/dms")
public class MobilityDmsController {

	@Autowired
	private MobilityDmsService moDmsService;

	@PostMapping("/upload")
	public ResponseEntity<?> uploadMobilityDmsObj(@RequestParam(name = "file", required = true) MultipartFile file,
			@RequestParam(name = "equipmentId", required = true) String equipmentId) throws Exception {
		return moDmsService.uploadMobilityDmsObj(file, equipmentId);
	}

	@GetMapping("/download/{objectId}")
	public ResponseEntity<?> downloadMobilityDmsObj(@PathVariable String objectId) throws Exception {
		return moDmsService.downloadMobilityDmsObj(objectId);
	}

	@DeleteMapping("/delete/{objectId}")
	public ResponseEntity<?> deleteMobilityDmsObj(@PathVariable String objectId) throws Exception {
		return moDmsService.deleteMobilityDmsObj(objectId, true);
	}
}
