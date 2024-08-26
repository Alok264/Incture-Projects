package cw.imo.chatbot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cw.imo.chatbot.service.ChatbotDmsService;

@RestController
@RequestMapping("/imo/chatbot/pdf")
public class ChatbotDmsController {

	@Autowired
	private ChatbotDmsService chService;

	@PostMapping("/upload")
	public ResponseEntity<?> pdfUpload(@RequestParam(name = "file", required = true) MultipartFile file)
			throws Exception {
		System.out.println("file : " + file);
		return chService.uploadPdf(file);
	}

	@GetMapping("/download/{objectId}")
	public ResponseEntity<?> pdfDownload(@PathVariable(name = "objectId", required = true) String objectId)
			throws Exception {
		return chService.downloadObject(objectId);
	}

	@PostMapping("/image/upload/{pdfName}")
	public ResponseEntity<?> imageUpload(@RequestParam(name = "file", required = true) MultipartFile file,
			@PathVariable(name = "pdfName", required = true) String pdfName) throws Exception {
		return chService.uploadImage(file, pdfName);
	}

	@GetMapping("/image/download/{objectId}")
	public ResponseEntity<?> imageDownload(@PathVariable(name = "objectId", required = true) String objectId)
			throws Exception {
		return chService.downloadObject(objectId);
	}

	@PostMapping("/table/upload/{pdfName}")
	public ResponseEntity<?> tableUpload(@RequestParam(name = "file", required = true) MultipartFile file,
			@PathVariable(name = "pdfName", required = true) String pdfName) throws Exception {
		return chService.uploadTable(file, pdfName);
	}

	@GetMapping("/table/download/{objectId}")
	public ResponseEntity<?> tableDownload(@PathVariable(name = "objectId", required = true) String objectId)
			throws Exception {
		return chService.downloadObject(objectId);
	}
}
