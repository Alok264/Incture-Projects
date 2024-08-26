package cw.imo.chatbot.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ChatbotDmsService {

	public ResponseEntity<?> downloadObject(String objectId) throws Exception;

	public ResponseEntity<?> uploadPdf(MultipartFile file) throws Exception;

	public ResponseEntity<?> uploadImage(MultipartFile file, String pdfName) throws Exception;

	public ResponseEntity<?> uploadTable(MultipartFile file, String pdfName) throws Exception;
}
