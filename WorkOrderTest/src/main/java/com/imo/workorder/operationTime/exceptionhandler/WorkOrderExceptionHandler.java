package com.imo.workorder.operationTime.exceptionhandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.imo.workorder.operationTime.dto.WorkOrderDmsResponseDto;

@RestControllerAdvice
public class WorkOrderExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleException(Exception e) {
		WorkOrderDmsResponseDto responseDto = new WorkOrderDmsResponseDto();
		responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
		responseDto.setStatus("Failure");
		Map<String, Object> errorMap = new HashMap<>();
		errorMap.put("message", e.getMessage());
		responseDto.setData(errorMap);
		return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<?> handleIOException(IOException e) {
		WorkOrderDmsResponseDto responseDto = new WorkOrderDmsResponseDto();
		responseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
		responseDto.setStatus("Failure");
		Map<String, Object> errorMap = new HashMap<>();
		errorMap.put("message", e.getMessage());
		responseDto.setData(errorMap);
		return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
