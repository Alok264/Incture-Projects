package com.imo.workorder.operationTime.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class WorkOpsTimeDetails {
	private Timestamp EndTime;
	private double Pause_duration;
	private String Status;
	private Timestamp StartTime;
	private String Operation;

}
