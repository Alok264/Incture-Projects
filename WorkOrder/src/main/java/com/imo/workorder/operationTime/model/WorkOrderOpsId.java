package com.imo.workorder.operationTime.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class WorkOrderOpsId implements Serializable {

	private static final long serialVersionUID = 1L;

	private String Plant;
	private String Operation;
	private String Workorder;

}
