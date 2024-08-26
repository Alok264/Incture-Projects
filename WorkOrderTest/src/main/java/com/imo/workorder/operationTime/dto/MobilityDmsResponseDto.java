package com.imo.workorder.operationTime.dto;

import java.util.Map;

import lombok.Data;

@Data
public class MobilityDmsResponseDto {
	private String status;
	private String statusCode;
	private Map<String, Object> data;
}
