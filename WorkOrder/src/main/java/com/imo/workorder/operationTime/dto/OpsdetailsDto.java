package com.imo.workorder.operationTime.dto;

import com.imo.workorder.operationTime.model.WorkOrderOpsModel;

import lombok.Data;

@Data
public class OpsdetailsDto {

	private double Actualtime;
	private WorkOrderOpsModel opsModel;
}
