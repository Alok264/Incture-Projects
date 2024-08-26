package com.imo.workorder.operationTime.service.Impl;

import java.util.concurrent.CompletableFuture;
import org.springframework.web.multipart.MultipartFile;

public class UploadFileProcessing implements Runnable {

	private MultipartFile file1;
	private MultipartFile file2;
	private String url;
	private FileUploadService fileUploadService;

	public UploadFileProcessing(MultipartFile file1, MultipartFile file2, String url,
			FileUploadService fileUploadService) {
		this.file1 = file1;
		this.file2 = file2;
		this.url = url;
		this.fileUploadService = fileUploadService;
	}

	@Override
	public void run() {
		try {
			CompletableFuture<String> future1 = fileUploadService.uploadFile(file1, url);
			CompletableFuture<String> future2 = fileUploadService.uploadFile(file2, url);

			String message1 = future1.get();
			String message2 = future2.get();

			System.out.println("File 1 Upload Result: " + message1);
			System.out.println("File 2 Upload Result: " + message2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
