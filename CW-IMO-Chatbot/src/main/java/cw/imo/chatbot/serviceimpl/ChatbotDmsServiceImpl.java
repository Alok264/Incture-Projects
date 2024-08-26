package cw.imo.chatbot.serviceimpl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import cw.imo.chatbot.constants.ChatbotDmsConstants;
import cw.imo.chatbot.dto.ChatbotDmsResponseDto;
import cw.imo.chatbot.model.ChatbotDms;
import cw.imo.chatbot.repository.ChatbotDmsRepository;
import cw.imo.chatbot.service.ChatbotDmsService;
import cw.imo.chatbot.utils.DestinationReaderUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatbotDmsServiceImpl implements ChatbotDmsService {

	@Autowired
	private ChatbotDmsRepository chRepo;

	@Autowired
	private DestinationReaderUtil destRepo;

	@Override
	public ResponseEntity<?> uploadPdf(MultipartFile file) throws Exception {
		String URL = returnURL();
		return uploadDocument(file, URL, true);
	}

	@Override
	public ResponseEntity<?> uploadImage(MultipartFile file, String pdfName) throws Exception {
		if (chRepo.existsByDocumentName(pdfName + ChatbotDmsConstants.DMS_FOLDER_SUFFIX)) {
			String URL = returnURL();
			return uploadDocument(file, URL + "/" + pdfName + ChatbotDmsConstants.DMS_FOLDER_SUFFIX + "/"
					+ ChatbotDmsConstants.DMS_IMAGE_FOLDER, false);
		} else {
			Map<String, Object> data = new HashMap<>();
			data.put("message", "PDF not found in database");
			return ResponseEntity.badRequest()
					.body(returnResponseDto("Failure", HttpStatus.NOT_FOUND.toString(), data));
		}
	}

	@Override
	public ResponseEntity<?> uploadTable(MultipartFile file, String pdfName) throws Exception {
		if (chRepo.existsByDocumentName(pdfName + ChatbotDmsConstants.DMS_FOLDER_SUFFIX)) {
			String URL = returnURL();
			return uploadDocument(file, URL + "/" + pdfName + ChatbotDmsConstants.DMS_FOLDER_SUFFIX + "/"
					+ ChatbotDmsConstants.DMS_TABLE_FOLDER, false);
		} else {
			Map<String, Object> data = new HashMap<>();
			data.put("message", "PDF not found in database");
			return ResponseEntity.badRequest()
					.body(returnResponseDto("Failure", HttpStatus.NOT_FOUND.toString(), data));
		}
	}

	public ResponseEntity<?> uploadDocument(MultipartFile file, String url, boolean flag) throws Exception {
		CloseableHttpClient httpClient = null;
		try {
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			String folderName = FilenameUtils.getBaseName(fileName) + ChatbotDmsConstants.DMS_FOLDER_SUFFIX;

			if (flag) {
				try {
					createFolder(folderName, url);
					try {
						createFolder(ChatbotDmsConstants.DMS_IMAGE_FOLDER, url + "/" + folderName);
						createFolder(ChatbotDmsConstants.DMS_TABLE_FOLDER, url + "/" + folderName);
					} catch (Exception e) {
						ChatbotDms chatbot = chRepo.findByDocumentName(folderName);
						deleteFolder(chatbot.getId());
						chRepo.delete(chatbot);
						throw new Exception(
								"Error while creating sub-folders" + e.getMessage() + " - " + e.getClass().getName());
					}
				} catch (Exception e) {
					log.error("Error while creating folder : " + e.getMessage());
					throw new Exception("Error while creating folder");
				}
			}

			httpClient = HttpClients.createDefault();
			HttpPost uploadFile = new HttpPost(flag ? url + "/" + folderName : url);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();

			builder.addTextBody("filename", fileName, ContentType.TEXT_PLAIN);
			builder.addTextBody("_charset_", "UTF-8", ContentType.TEXT_PLAIN);
			builder.addTextBody("propertyId[0]", "cmis:name", ContentType.TEXT_PLAIN);
			builder.addTextBody("propertyValue[0]", fileName, ContentType.TEXT_PLAIN);
			builder.addTextBody("propertyId[1]", "cmis:objectTypeId", ContentType.TEXT_PLAIN);
			builder.addTextBody("propertyValue[1]", "cmis:document", ContentType.TEXT_PLAIN);
			builder.addTextBody("succinct", "true", ContentType.TEXT_PLAIN);
			builder.addTextBody("includeAllowableActions", "true", ContentType.TEXT_PLAIN);
			builder.addTextBody("cmisaction", "createDocument", ContentType.TEXT_PLAIN);
			builder.addBinaryBody("media", file.getInputStream(), ContentType.APPLICATION_OCTET_STREAM, fileName);
			HttpEntity multipart = builder.build();

			uploadFile.setEntity(multipart);
			String token = getAccessToken();
			if (token == null) {
				return ResponseEntity.badRequest().body("Error while getting access token");
			}

			uploadFile.setHeader("Authorization", "Bearer " + token);
			CloseableHttpResponse response = httpClient.execute(uploadFile);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			if (statusCode == 404) {
				Map<String, Object> data = new HashMap<>();
				data.put("message", "Repository or folder not found");
				return ResponseEntity.badRequest().body(returnResponseDto("Failure", statusCode + "", data));
			} else if (statusCode == 409) {
				Map<String, Object> data = new HashMap<>();
				data.put("message", "Document with this name already exists");
				return ResponseEntity.badRequest().body(returnResponseDto("Failure", statusCode + "", data));
			} else if (statusCode >= 200 && statusCode < 300) {
				String result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
				JSONObject jsonObject = new JSONObject(result);

				if (!jsonObject.has("succinctProperties")) {
					return ResponseEntity.badRequest().body(jsonObject.toString());
				}
				String objectId = jsonObject.getJSONObject("succinctProperties").getString("cmis:objectId");
				JSONArray folderIdArray = jsonObject.getJSONObject("succinctProperties").getJSONArray("sap:parentIds");
				String folderId = null;
				if (folderIdArray.length() > 1) {
					for (int i = 0; i < folderIdArray.length(); i++) {
						folderId += folderIdArray.getString(i) + "  ";
					}
				} else {
					folderId = folderIdArray.getString(0);
				}

				Map<String, Object> data = new HashMap<>();
				data.put("message", "Document uploaded successfully");
				data.put("objectId", objectId);
				data.put("fileName", fileName);
				data.put("folderId", folderId);
				try {
					chRepo.save(returnChatbot(objectId, fileName, configData().get("repoName"), folderId, "file"));
				} catch (Exception e) {
					deleteObject(objectId);
					log.error("Error while saving object in database : " + e.getMessage());
					throw new Exception("Error while saving object in database");
				}
				httpClient.close();
				return ResponseEntity.ok(returnResponseDto("Success", statusCode + "", data));
			} else {
				Map<String, Object> data = new HashMap<>();
				data.put("message", "Error while uploading document");
				log.error("Error while uploading document : " + response.getEntity().toString());
				httpClient.close();
				return ResponseEntity.badRequest().body(returnResponseDto("Failure", statusCode + "", data));
			}

		} catch (Exception e) {
			log.error("Exception while uploading document : " + e.getClass().getName() + " - " + e.getMessage());
			httpClient.close();
			throw new Exception("Exception while uploading document: - " + e.getClass().getName());
		}
	}

	@Override
	public ResponseEntity<?> downloadObject(String objectId) throws Exception {

		String URL = returnURL() + "?objectId=" + objectId + "&cmisselector=content&download=attachment";

		String fileName = null;
		if (!chRepo.existsById(objectId)) {
			Map<String, Object> data = new HashMap<>();
			data.put("message", "Object not found in database");
			return ResponseEntity.badRequest()
					.body(returnResponseDto(ChatbotDmsConstants.DOWNLOAD_FAILURE, "404", data));
		} else {
			fileName = chRepo.findById(objectId).get().getDocumentName();
		}

		try {

			HttpClient httpClient = HttpClients.createDefault();
			HttpGet getFileRequest = new HttpGet(URL);
			String tokenString = getAccessToken();
			getFileRequest.setHeader("Authorization", "Bearer " + tokenString);

			HttpResponse response = httpClient.execute(getFileRequest);
			HttpEntity entity = response.getEntity();

			ContentType contentType = ContentType.getOrDefault(entity);

			byte[] content = null;
			if (entity != null) {
				content = EntityUtils.toByteArray(entity);
			}

			return ResponseEntity.ok().contentLength(content.length)
					.contentType(MediaType.parseMediaType(contentType.getMimeType()))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").body(content);

		} catch (Exception e) {
			log.error("Exception while downloading document : " + e.getMessage());
			throw new RuntimeException(
					"Exception while downloading document: - " + e.getMessage() + " - " + e.getClass().getName());
		}
	}

	private boolean deleteObject(String objectId) throws Exception {

		try {
			String URL = returnURL();
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost createFolder = new HttpPost(URL);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setBoundary("---011000010111000001101001");
			builder.addTextBody("cmisaction", "delete", ContentType.TEXT_PLAIN);
			builder.addTextBody("objectId", objectId, ContentType.TEXT_PLAIN);
			builder.addTextBody("allVersions", "true", ContentType.TEXT_PLAIN);
			HttpEntity multipart = builder.build();
			createFolder.setEntity(multipart);
			String token = getAccessToken();
			if (token == null) {
				return false;
			}
			createFolder.setHeader("Authorization", "Bearer " + token);
			createFolder.setHeader("Content-Type", "multipart/form-data; boundary=---011000010111000001101001");
			createFolder.setHeader("Accept", "*/*");
			createFolder.setHeader("DataServiceVersion", "2.0");

			try (CloseableHttpResponse response = httpClient.execute(createFolder)) {
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 404) {
					return false;
				} else if (statusCode >= 200 && statusCode < 300) {
					return true;
				} else {
					return false;
				}
			}

		} catch (Exception e) {
			log.error("Exception while deleting document : " + e.getMessage());
			throw new RuntimeException(
					"Exception while deleting document: - " + e.getMessage() + " - " + e.getClass().getName());
		}
	}

	private boolean createFolder(String folderName, String URL) {
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost createFolder = new HttpPost(URL);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setBoundary("---011000010111000001101001");
			builder.addTextBody("cmisaction", "createFolder", ContentType.TEXT_PLAIN);
			builder.addTextBody("propertyId[0]", "cmis:name", ContentType.TEXT_PLAIN);
			builder.addTextBody("propertyValue[0]", folderName, ContentType.TEXT_PLAIN);
			builder.addTextBody("propertyId[1]", "cmis:objectTypeId", ContentType.TEXT_PLAIN);
			builder.addTextBody("propertyValue[1]", "cmis:folder", ContentType.TEXT_PLAIN);
			builder.addTextBody("succinct", "true", ContentType.TEXT_PLAIN);
			HttpEntity multipart = builder.build();
			createFolder.setEntity(multipart);
			String token = getAccessToken();

			if (token == null) {
				return false;
			}
			createFolder.setHeader("Authorization", "Bearer " + token);
			createFolder.setHeader("Content-Type", "multipart/form-data; boundary=---011000010111000001101001");
			createFolder.setHeader("Accept", "application/json");
			createFolder.setHeader("DataServiceVersion", "2.0");

			try (CloseableHttpResponse response = httpClient.execute(createFolder)) {

				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();

				if (statusCode == 404) {
					return false;
				} else if (statusCode == 409) {
					return false;
				} else if (statusCode >= 200 && statusCode < 300) {
					String result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
					JSONObject jsonObject = new JSONObject(result);

					if (!jsonObject.has("succinctProperties")) {
						return false;
					}

					String objectId = jsonObject.getJSONObject("succinctProperties").getString("cmis:objectId");
					JSONArray folderIdArray = jsonObject.getJSONObject("succinctProperties")
							.getJSONArray("sap:parentIds");

					String folderId = null;

					if (folderIdArray.length() > 1) {
						for (int i = 0; i < folderIdArray.length(); i++) {
							folderId += folderIdArray.getString(i) + "  ";
						}
					} else {
						folderId = folderIdArray.getString(0);
					}
					try {
						chRepo.save(
								returnChatbot(objectId, folderName, configData().get("repoName"), folderId, "folder"));
					} catch (Exception e) {
						log.error("Exception while saving object to database : " + e.getMessage());
						deleteFolder(objectId);
						throw new Exception("Exception while saving object to database");
					}
					return true;
				} else {
					return false;
				}

			}

		} catch (Exception e) {
			log.error("Exception while creating folder : " + e.getMessage());
			throw new RuntimeException(
					"Exception while creating folder: - " + e.getMessage() + " - " + e.getClass().getName());
		}
	}

	public boolean deleteFolder(String folderId) throws Exception {

		try {
			String URL = returnURL();
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost deleteFolder = new HttpPost(URL);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setBoundary("---011000010111000001101001");
			builder.addTextBody("cmisaction", "deleteTree", ContentType.TEXT_PLAIN);
			builder.addTextBody("objectId", folderId, ContentType.TEXT_PLAIN);
			builder.addTextBody("allVersions", "true", ContentType.TEXT_PLAIN);
			builder.addTextBody("unfileObjects", "delete", ContentType.TEXT_PLAIN);
			builder.addTextBody("continueOnFailure", "true", ContentType.TEXT_PLAIN);
			HttpEntity multipart = builder.build();
			deleteFolder.setEntity(multipart);
			String token = getAccessToken();
			if (token == null) {
				return false;
			}
			deleteFolder.setHeader("Authorization", "Bearer " + token);
			deleteFolder.setHeader("Content-Type", "multipart/form-data; boundary=---011000010111000001101001");
			deleteFolder.setHeader("Accept", "*/*");
			deleteFolder.setHeader("DataServiceVersion", "2.0");

			try (CloseableHttpResponse response = httpClient.execute(deleteFolder)) {
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 404) {
					return false;
				} else if (statusCode >= 200 && statusCode < 300) {
					return true;
				} else {
					return false;
				}
			}

		} catch (Exception e) {

			log.error("Exception while deleting folder : " + e.getMessage());
			throw new RuntimeException(
					"Exception while deleting folder: - " + e.getMessage() + " - " + e.getClass().getName());
		}
	}

	private String getAccessToken() throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		Map<String, String> map = configData();
		String tokenEndpoint = map.get("tokenurl");
		HttpPost httpPost = new HttpPost(tokenEndpoint);
		httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
		String body = null;

		try {
			body = "grant_type="
					+ URLEncoder.encode(ChatbotDmsConstants.DMS_GRANT_TYPE, StandardCharsets.UTF_8.toString())
					+ "&client_id=" + URLEncoder.encode(map.get("clientId"), StandardCharsets.UTF_8.toString())
					+ "&client_secret=" + URLEncoder.encode(map.get("clientSecret"), StandardCharsets.UTF_8.toString())
					+ "&response_type="
					+ URLEncoder.encode(ChatbotDmsConstants.DMS_RESPONSE_TYPE, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			log.error("Exception while getting access token : " + e.getMessage());
			throw new Exception(
					"Exception while getting access token: " + e.getMessage() + " - " + e.getClass().getName());
		}
		StringEntity input = null;

		try {
			input = new StringEntity(body);
			httpPost.setEntity(input);
		} catch (UnsupportedEncodingException e) {
			log.error("Exception while getting access token : " + e.getMessage());
			throw new Exception("Exception while getting access token");
		}

		HttpResponse response = null;

		try {
			response = httpClient.execute(httpPost);
		} catch (IOException e) {
			log.error("Exception while getting access token : " + e.getMessage());
			throw new Exception("Exception while getting access token");
		}

		String result = null;
		try {
			result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} catch (UnsupportedOperationException | IOException e) {
			log.error("Exception while getting access token : " + e.getMessage());
			throw new Exception("Exception while getting access token");
		}

		JSONObject object = new JSONObject(result);
		if (!object.has("access_token")) {
			return null;
		}
		return object.getString("access_token").toString();
	}

	private ChatbotDmsResponseDto returnResponseDto(String status, String code, Map<String, Object> data) {
		ChatbotDmsResponseDto responseDto = new ChatbotDmsResponseDto();
		responseDto.setStatus(status);
		responseDto.setCode(code);
		responseDto.setData(data);
		return responseDto;
	}

	private ChatbotDms returnChatbot(String objectId, String fileName, String repositoryName, String parentFolderId,
			String documentType) {
		ChatbotDms chatbot = new ChatbotDms();
		chatbot.setId(objectId);
		chatbot.setDocumentName(fileName);
		chatbot.setRepoName(repositoryName);
		chatbot.setFolderId(parentFolderId);
		chatbot.setDocumentType(documentType);
		return chatbot;
	}

	private Map<String, String> configData() throws Exception {
		String desRes = destRepo.readDestinationDestination("cw-imo-dms", "");
		JSONObject jsonObject = new JSONObject(desRes);

		String tokenurl = jsonObject.getJSONObject("destinationConfiguration").getString("tokenServiceURL");
		String clientId = jsonObject.getJSONObject("destinationConfiguration").getString("clientId");
		String clientSecret = jsonObject.getJSONObject("destinationConfiguration").getString("clientSecret");
		String baseUrl = jsonObject.getJSONObject("destinationConfiguration").getString("URL");

		Map<String, String> map = new HashMap<>();
		map.put("tokenurl", tokenurl);
		map.put("clientId", clientId);
		map.put("clientSecret", clientSecret);
		map.put("baseUrl", baseUrl);

		return map;
	}

	private String returnURL() throws Exception {
		Map<String, String> map = configData();
		String URL = map.get("baseUrl") + ChatbotDmsConstants.DMS_REPOSITORY_ID + "/root";
		return URL;
	}

}
