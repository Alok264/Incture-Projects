package cw.imo.chatbot.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public class ChatbotDmsServiceUtil {

	public static File multipartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile();
		FileOutputStream fileOutputStream = new FileOutputStream(convFile);
		fileOutputStream.write(file.getBytes());
		fileOutputStream.close();
		return convFile;
	}

}
