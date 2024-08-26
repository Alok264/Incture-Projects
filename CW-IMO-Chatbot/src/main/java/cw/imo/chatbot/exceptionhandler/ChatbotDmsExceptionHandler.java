package cw.imo.chatbot.exceptionhandler;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import cw.imo.chatbot.dto.ChatbotDmsResponseDto;

public class ChatbotDmsExceptionHandler {
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleException(Exception e) {
		ChatbotDmsResponseDto responseDto = new ChatbotDmsResponseDto();
		responseDto.setCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
		responseDto.setStatus("Failure");
		Map<String, Object> errorMap = new HashMap<>();
		errorMap.put("message", e.getMessage());
		responseDto.setData(errorMap);
		return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<?> handleIOException(IOException e) {
		ChatbotDmsResponseDto responseDto = new ChatbotDmsResponseDto();
		responseDto.setCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
		responseDto.setStatus("Failure");
		Map<String, Object> errorMap = new HashMap<>();
		errorMap.put("message", e.getMessage());
		responseDto.setData(errorMap);
		return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
		ChatbotDmsResponseDto responseDto = new ChatbotDmsResponseDto();
		responseDto.setCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
		responseDto.setStatus("Failure");
		Map<String, Object> errorMap = new HashMap<>();
		errorMap.put("message", e.getMessage());
		responseDto.setData(errorMap);
		return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
