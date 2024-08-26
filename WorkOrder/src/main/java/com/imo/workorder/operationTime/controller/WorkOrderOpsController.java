package com.imo.workorder.operationTime.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.imo.workorder.operationTime.dto.Workorderopsdto;
import com.imo.workorder.operationTime.dto.workOrderOpsUpdate;
import com.imo.workorder.operationTime.service.WorkOrderOpsService;

@RestController
@RequestMapping("/imo/Workorder")
public class WorkOrderOpsController {

	@Autowired
	private WorkOrderOpsService woService;

	// api to get operation total time based on ordernum and ops
//	@GetMapping("/get/ActualTime/{plant}/{ordernum}/{operation}")
//	public ResponseEntity<?> getoperationTime(@PathVariable String plant, @PathVariable String ordernum,
//			@PathVariable String operation) {
//		return woService.getWorkOrderOpsTime(plant, ordernum, operation);
//	}

	// api to get ops data based on plant, workorder, ops and also actual
	// runtime(hrs)
	@GetMapping("/get/data/{plant}/{ordernum}/{operation}")
	public ResponseEntity<?> getoperationdetails(@PathVariable String plant, @PathVariable String ordernum,
			@PathVariable String operation) {
		return woService.getWorkOrderOps(plant, ordernum, operation);
	}

	// insert workorder ops status along with timestamp
	@PostMapping("/post/WorkorderOperation")
	public ResponseEntity<?> insertRunDetails(@RequestBody Workorderopsdto details) {
		return woService.SaveWorkOrderOps(details);
	}

	// update the timestamp for particular WO operation
	@PostMapping("/update/WorkorderOperationTime")
	public ResponseEntity<?> updateRunDetails(@RequestBody workOrderOpsUpdate details) {
		return woService.UpdateWorkOrderOps(details);
	}

}
