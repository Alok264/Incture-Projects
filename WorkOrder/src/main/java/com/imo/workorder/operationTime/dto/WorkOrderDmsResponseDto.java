package com.imo.workorder.operationTime.dto;

import java.util.Map;

import lombok.Data;

@Data
public class WorkOrderDmsResponseDto {
	private String status;
	private String statusCode;
	private Map<String, Object> data;
}
