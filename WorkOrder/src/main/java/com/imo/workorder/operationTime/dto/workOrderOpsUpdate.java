package com.imo.workorder.operationTime.dto;

import java.util.List;

import lombok.Data;

@Data
public class workOrderOpsUpdate {	
	
	private String Workorder;
	private String Plant;
	private List<WorkOpsTimeDetails> TimeData;
}
