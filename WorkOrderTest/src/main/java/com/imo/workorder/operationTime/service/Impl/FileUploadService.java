package com.imo.workorder.operationTime.service.Impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.imo.workorder.operationTime.constants.WorkOrderDmsConstants;
import com.imo.workorder.operationTime.model.WorkOrderDms;
import com.imo.workorder.operationTime.repo.WorkOrderDmsRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileUploadService {

	@Autowired
	private WorkOrderDmsRepo dmsRepo;

	@Async("taskExecutor")
	public CompletableFuture<String> uploadFile(MultipartFile file, String URL) {
		return CompletableFuture.supplyAsync(() -> {
			String message = null;
			log.info("Starting Thread - Processing Payload {}", file.getOriginalFilename());
			try {
				message = uploadFileToDms(file, URL);
				log.info("Message : " + message);
			} catch (Exception e) {
				log.error("Error while uploading document : {}", e.getMessage());
			}
			log.info("Ending Thread - Processing Payload {}", file.getOriginalFilename());
			return message;
		});
	}

	private String uploadFileToDms(MultipartFile file, String URL) throws Exception {
		if (file == null || file.isEmpty()) {
			return "Failure Due to Empty File";
		}
		log.info("Started Uploading The file: {}", file.getOriginalFilename());
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			log.info("fileName: {}", fileName);

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
			log.info("File Content Type: {}", file.getContentType());
			builder.addBinaryBody("media", file.getInputStream(), ContentType.APPLICATION_OCTET_STREAM, fileName);
			HttpEntity multipart = builder.build();

			uploadFile.setEntity(multipart);
			String token = getAccessToken();
			if (token == null) {
				return "Failure Due to DMS Access Token";
			}

			uploadFile.setHeader("Authorization", "Bearer " + token);
			try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();

				if (statusCode == 404) {
					return "Failure Due to Repository or folder not found " + statusCode;
				} else if (statusCode == 409) {
					return "Failure Due to Document already exists " + statusCode;
				} else if (statusCode >= 200 && statusCode < 300) {
					String result = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
					JSONObject jsonObject = new JSONObject(result);

					if (!jsonObject.has("succinctProperties")) {
						return "Failure Due to Document not uploaded";
					}
					String objectId = jsonObject.getJSONObject("succinctProperties").getString("cmis:objectId");
					JSONArray folderIdArray = jsonObject.getJSONObject("succinctProperties")
							.getJSONArray("sap:parentIds");
					StringBuilder folderIdBuilder = new StringBuilder();
					for (int i = 0; i < folderIdArray.length(); i++) {
						folderIdBuilder.append(folderIdArray.getString(i)).append("  ");
					}
					String folderId = folderIdBuilder.toString().trim();
					try {
						log.info("Object Id: {}", objectId);
						dmsRepo.save(returnWorkOrderDms(objectId, fileName, "file", folderId,
								WorkOrderDmsConstants.DMS_REPO_NAME));
					} catch (Exception e) {
						if (!deleteWorkOrderDmsObj(objectId, false)) {
							log.error("Error while deleting object from database : {}", e.getMessage());
							throw new Exception("Error while deleting object from database", e);
						}
						log.error("Error while saving object in database : {}", e.getMessage());
						throw new Exception("Error while saving object in database", e);
					}
					return "Success@" + objectId;
				} else {
					return "Failure Due to Document not uploaded " + statusCode;
				}
			}
		} catch (

		Exception e) {
			log.error("Exception while uploading document : {} - {}", e.getClass().getName(), e.getMessage());
			throw new Exception("Exception while uploading document: - " + e.getClass().getName(), e);
		}
	}

	protected String getAccessToken() throws Exception {
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

	public boolean deleteWorkOrderDmsObj(String objectId, boolean flag) throws Exception {

		String URL = returnURL1();

		if (flag && !dmsRepo.existsById(objectId)) {
			return false;
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
				return false;
			}
			deleteObject.setHeader("Authorization", "Bearer " + token);
			deleteObject.setHeader("Content-Type", "multipart/form-data; boundary=---011000010111000001101001");
			deleteObject.setHeader("Accept", "*/*");
			deleteObject.setHeader("DataServiceVersion", "2.0");

			try (CloseableHttpResponse response = httpClient.execute(deleteObject)) {
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 404) {
					return false;
				} else if (statusCode >= 200 && statusCode < 300) {
					try {
						if (flag) {
							dmsRepo.deleteById(objectId);
						}
					} catch (Exception e) {
						String status = restoreObject(objectId) ? "Document restored successfully"
								: "Document not restored";
						log.error("Exception while deleting object from database : status : " + status + " : "
								+ e.getMessage());
						throw new Exception("Error while deleting object from database");
					}
					return true;
				} else {
					return false;
				}

			}

		} catch (Exception e) {
			log.error("Exception while deleting document : ");
			throw new Exception("Exception while deleting document: - " + e.getClass().getName());
		}
	}

	private String returnURL1() throws Exception {
		String URL = WorkOrderDmsConstants.DMS_URL + WorkOrderDmsConstants.DMS_REPO_ROOT;
		return URL;
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
}
