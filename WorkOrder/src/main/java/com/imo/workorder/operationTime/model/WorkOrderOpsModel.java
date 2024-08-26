package com.imo.workorder.operationTime.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

//entity to store hierarchy data of assets
@Entity
@Table(name = "WorkorderOerationTime")
@IdClass(WorkOrderOpsId.class)
@Data
public class WorkOrderOpsModel {

	@Id
	@Column(name = "Plant", length = 25)
	private String Plant;

	@Id
	@Column(name = "Workorder", length = 25)
	private String Workorder;

	@Id
	@Column(name = "Operation")
	private String Operation;

	@Column(name = "Status")
	private String Status;

	@Column(name = "Pause_duration")
	private double Pause_duration;

	@Column(name = "EndTime")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private Timestamp EndTime;

	@Column(name = "StartTime")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	private Timestamp StartTime;

}
