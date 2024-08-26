package cw.imo.chatbot.dto;

import java.util.Map;

import lombok.Data;

@Data
public class ChatbotDmsResponseDto {

	private String status;
	private String code;
	private Map<String, Object> data;

}
