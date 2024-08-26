package com.imo.workorder.operationTime.service.Impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.imo.workorder.operationTime.constants.WorkOrderDmsConstants;
import com.imo.workorder.operationTime.dto.WorkOrderDmsResponseDto;
import com.imo.workorder.operationTime.model.WorkOrderDms;
import com.imo.workorder.operationTime.repo.WorkOrderDmsRepo;
import com.imo.workorder.operationTime.service.WorkOrderDmsService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkOrderDmsServiceImpl implements WorkOrderDmsService {

	@Autowired
	private WorkOrderDmsRepo dmsrepo;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	private FileUploadService fileUploadService;
//	@Autowired
//	private DestinationReaderUtil destRepo;

	@Override
	public ResponseEntity<?> uploadWorkOrderDmsObj(MultipartFile file) throws Exception {

		String URL = returnURL1() + "/" + WorkOrderDmsConstants.DMS_FOLDER_NAME;

		CloseableHttpClient httpClient = null;
		try {
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			httpClient = HttpClients.createDefault();
			HttpPost uploadFile = new HttpPost(URL);
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
					dmsrepo.save(returnWorkOrderDms(objectId, fileName, "file", folderId,
							WorkOrderDmsConstants.DMS_REPO_NAME));
				} catch (Exception e) {
					deleteWorkOrderDmsObj(objectId, false);
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
	public ResponseEntity<?> downloadWorkOrderDmsObj(String objectId) throws Exception {
		String URL = returnURL1() + "?objectId=" + objectId + "&cmisselector=content&download=attachment";
		try {
			String filename = null;
			try {
				filename = dmsrepo.findById(objectId).get().getDocumentName();
			} catch (Exception e) {
				log.error("Object not found in database");
				Map<String, Object> data = new HashMap<>();
				data.put("message", "Object is not found in database");
				return ResponseEntity.ok().body(returnResponseDto("Failure", "404", data));
			}

			HttpClient httpClient = HttpClients.createDefault();
			HttpGet getFileRequest = new HttpGet(URL);
			String tokenString = getAccessToken();
			getFileRequest.setHeader("Authorization", "Bearer " + tokenString);

			HttpResponse response = httpClient.execute(getFileRequest);
			HttpEntity entity = response.getEntity();

			ContentType contentType = ContentType.get(entity);

			byte[] content = null;
			if (entity != null) {
				content = EntityUtils.toByteArray(entity);
			}

			return ResponseEntity.ok().contentLength(content.length)
					.contentType(MediaType.parseMediaType(contentType.getMimeType()))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(content);
		} catch (Exception e) {
			log.error("Error while downloading document : " + e.getMessage());
			throw new Exception("Exception while downloading document: - " + e.getClass().getName());
		}
	}

	@Override
	public ResponseEntity<?> deleteWorkOrderDmsObj(String objectId, boolean flag) throws Exception {

		String URL = returnURL1();

		if (flag && !dmsrepo.existsById(objectId)) {
			log.error("Object not found in database");
			Map<String, Object> data = new HashMap<>();
			data.put("message", "Object is not found in database");
			return ResponseEntity.ok().body(returnResponseDto("Failure", "404", data));
		}

		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost deleteObject = new HttpPost(URL);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setBoundary("---011000010111000001101001");
			builder.addTextBody("cmisaction", "delete", ContentType.TEXT_PLAIN);
			builder.addTextBody("objectId", objectId, ContentType.TEXT_PLAIN);
			builder.addTextBody("allVersions", "true", ContentType.TEXT_PLAIN);
			HttpEntity multipart = builder.build();
			deleteObject.setEntity(multipart);
			String token = getAccessToken();
			if (token == null) {
				return ResponseEntity.badRequest().body("Error while getting access token");
			}
			deleteObject.setHeader("Authorization", "Bearer " + token);
			deleteObject.setHeader("Content-Type", "multipart/form-data; boundary=---011000010111000001101001");
			deleteObject.setHeader("Accept", "*/*");
			deleteObject.setHeader("DataServiceVersion", "2.0");

			try (CloseableHttpResponse response = httpClient.execute(deleteObject)) {
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 404) {
					Map<String, Object> data = new HashMap<>();
					data.put("message", "File not found");
					return ResponseEntity.badRequest().body(returnResponseDto("Failure", statusCode + "", data));
				} else if (statusCode >= 200 && statusCode < 300) {
					Map<String, Object> data = new HashMap<>();
					data.put("message", "Document deleted successfully");
					try {
						if (flag) {
							dmsrepo.deleteById(objectId);
						}
					} catch (Exception e) {
						String status = restoreObject(objectId) ? "Document restored successfully"
								: "Document not restored";
						log.error("Exception while deleting object from database : status : " + status + " : "
								+ e.getMessage());
						throw new Exception("Error while deleting object from database");
					}
					return ResponseEntity.ok(returnResponseDto("Success", statusCode + "", data));
				} else {
					Map<String, Object> data = new HashMap<>();
					data.put("message", "Error while deleting document");
					log.error("Error while deleting document : " + response.getEntity().toString());
					return ResponseEntity.badRequest().body(returnResponseDto("Failure", statusCode + "", data));
				}

			}

		} catch (Exception e) {
			log.error("Exception while deleting document : ");
			throw new Exception("Exception while deleting document: - " + e.getClass().getName());
		}
	}

	@Override
	public String getAccessToken() throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		Map<String, String> map = getDestinationData();
//		String tokenEndpoint = map.get("tokenurl");
		String tokenEndpoint = WorkOrderDmsConstants.DMS_TOKEN_URL;
		HttpPost httpPost = new HttpPost(tokenEndpoint);
		httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
		String body = null;

		try {
			body = "grant_type="
					+ URLEncoder.encode(WorkOrderDmsConstants.DMS_GRANT_TYPE, StandardCharsets.UTF_8.toString())
					+ "&client_id="
					+ URLEncoder.encode(WorkOrderDmsConstants.DMS_CLIENT_ID, StandardCharsets.UTF_8.toString())
					+ "&client_secret="
					+ URLEncoder.encode(WorkOrderDmsConstants.DMS_CLIENT_SECRET, StandardCharsets.UTF_8.toString())
					+ "&response_type="
					+ URLEncoder.encode(WorkOrderDmsConstants.DMS_RESPONSE_TYPE, StandardCharsets.UTF_8.toString());
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
			log.error("Exception while getting access token and setting Entity: " + e.getMessage());
		}

		HttpResponse response = null;

		try {
			response = httpClient.execute(httpPost);
		} catch (IOException e) {
			log.error("Exception while getting access token. IO Exception: " + e.getMessage());
		}

		String result = null;
		try {
			result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} catch (UnsupportedOperationException | IOException e) {
			log.error("Exception while getting access token. UnsupportedOperationException : " + e.getMessage());
		}

		JSONObject object = new JSONObject(result);
		if (!object.has("access_token")) {
			return null;
		}
		return object.getString("access_token").toString();
	}

	private WorkOrderDmsResponseDto returnResponseDto(String status, String code, Map<String, Object> data) {
		WorkOrderDmsResponseDto responseDto = new WorkOrderDmsResponseDto();
		responseDto.setStatus(status);
		responseDto.setStatusCode(code);
		responseDto.setData(data);
		return responseDto;
	}

	private WorkOrderDms returnWorkOrderDms(String objectId, String documentName, String documentType, String folderId,
			String repoName) {
		WorkOrderDms workorder = new WorkOrderDms();
		workorder.setId(objectId);
		workorder.setDocumentName(documentName);
		workorder.setRepoName(repoName);
		workorder.setFolderId(folderId);
		workorder.setDocumentType(documentType);
		return workorder;
	}

	private boolean restoreObject(String objectId) throws Exception {
		String URL = returnURL1() + "?cmisation=restoreObject&objectId=" + objectId;

		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPut restoreObject = new HttpPut(URL);
			String token = getAccessToken();
			if (token == null) {
				log.error("Error while getting access token: " + token + " : " + objectId);
				return false;
			}
			restoreObject.setHeader("Authorization", "Bearer " + token);
			restoreObject.setHeader("Accept", "application/json");
			restoreObject.setHeader("DataServiceVersion", "2.0");

			try (CloseableHttpResponse response = httpClient.execute(restoreObject)) {
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 404) {
					log.error("Restore Object is not found " + statusCode + " : " + objectId);
					return false;
				} else if (statusCode >= 200 && statusCode < 300) {
					log.info("Document restored successfully: " + objectId + " : " + statusCode);
					return true;
				} else {
					log.error("Error while restoring document : " + response.getEntity().toString());
					return false;
				}
			}
		} catch (Exception e) {
			log.error("Exception while restoring document : " + e.getMessage());
			throw new RuntimeException(
					"Exception while restoring document: - " + e.getMessage() + " - " + e.getClass().getName());
		}
	}

//	private Map<String, String> getDestinationData() throws Exception {
//		String desRes = destRepo.readDestinationDestination("cw-imo-dms", "");
//		JSONObject jsonObject = new JSONObject(desRes);
//
//		String tokenurl = jsonObject.getJSONObject("destinationConfiguration").getString("tokenServiceURL");
//		String clientId = jsonObject.getJSONObject("destinationConfiguration").getString("clientId");
//		String clientSecret = jsonObject.getJSONObject("destinationConfiguration").getString("clientSecret");
//		String baseUrl = jsonObject.getJSONObject("destinationConfiguration").getString("URL");
//
//		Map<String, String> map = new HashMap<>();
//		map.put("tokenurl", tokenurl);
//		map.put("clientId", clientId);
//		map.put("clientSecret", clientSecret);
//		map.put("baseUrl", baseUrl);
//
//		return map;
//	}
//
//	private String returnURL() throws Exception {
//		Map<String, String> map = getDestinationData();
//		String URL = map.get("baseUrl") + "/browser/" + WorkOrderDmsConstants.DMS_REPO_ID + "/root";
//		return URL;
//	}

	private String returnURL1() throws Exception {
		String URL = WorkOrderDmsConstants.DMS_URL + WorkOrderDmsConstants.DMS_REPO_ROOT;
		return URL;
	}

	@Override
	public ResponseEntity<?> uploadMultipleFiles(MultipartFile[] files) throws Exception {
		log.info("{} files found to upload", files.length);
		if (files.length == 0) {
			Map<String, Object> data = new HashMap<>();
			data.put("message", "No files found to upload");
			return ResponseEntity.badRequest().body(returnResponseDto("Failure", "400", data));
		}

		String URL = returnURL1() + "/" + WorkOrderDmsConstants.DMS_FOLDER_NAME;
		Map<String, String> successUpload = new HashMap<>();
		Map<String, String> failureUpload = new HashMap<>();

		log.info("Started processing the files");

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (int i = 0; i < files.length; i += 2) {
			MultipartFile file1 = files[i];
			MultipartFile file2 = (i + 1 < files.length) ? files[i + 1] : null; // Handle the case when there is an odd
																				// number of files

			UploadFileProcessing task = new UploadFileProcessing(file1, file2, URL, fileUploadService);
			CompletableFuture<Void> future = CompletableFuture.runAsync(task, taskExecutor);
			futures.add(future);
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		log.info("Files processing completed");

		// Collect results
		Map<String, Object> data = new HashMap<>();
		data.put("successUpload", successUpload);
		data.put("failureUpload", failureUpload);
		return ResponseEntity.ok().body(returnResponseDto("Success", "200", data));
	}

}
