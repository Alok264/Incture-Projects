package com.imo.workorder.operationTime.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class Workorderopsdto {
	private Timestamp EndTime;
	private double Pause_duration;
	private String Status;
	private String Operation;
	private String Workorder;
	private String Plant;
	private Timestamp StartTime;

}
