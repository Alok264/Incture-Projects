package cw.imo.chatbot.constants;

import org.springframework.stereotype.Component;

@Component
public class ChatbotDmsConstants {

	public static final String SUCCESS = "Success";
	public static final String UPLOAD_SUCCESS = "Uploaded successfully";
	public static final String UPLOAD_FAILURE = "Uploading failed";
	public static final String UPDATE_SUCCESS = "Updated successfully";
	public static final String UPDATE_FAILURE = "Updation failed";
	public static final String DOWNLOAD_SUCCESS = "Downloaded successfully";
	public static final String DOWNLOAD_FAILURE = "Downloading failed";
	public static final String REJECT_SUCCESS = "Rejected Successfully";
	public static final String REJECT_FAILURE = "Rejection failed";
	public static final String DELETE_SUCCESS = "Deleted Successfully";
	public static final String DELETE_FAILURE = "Deletion failed";
	public static final String FAILURE = "Failure";
	public static final String CREATE = "Create";
	public static final String UPDATE = "Update";
	public static final String CODE_SUCCESS = "0";
	public static final String CODE_FAILURE = "1";
	public static final Integer APPENDINGIDLENGTH = 12;
//	public static final String DMS_TOKEN_ENDPOINT = "https://incture-cherrywork-dev.authentication.eu10.hana.ondemand.com/oauth/token";
//	public static final String DMS_CLIENT_ID = "sb-e97bacae-17d5-4d01-874c-2f4647fe4d2e!b197806|sdm-di-SDM_DI_PROD-prod!b41064";
//	public static final String DMS_CLIENT_SECRET = "YqHluyJbxqMPe3JdnF/O1ODmoHM=";
	public static final String DMS_GRANT_TYPE = "client_credentials";
	public static final String DMS_SCOPE = "generate-ads-output";
	public static final String DMS_RESPONSE_TYPE = "token";
//	public static final String DMS_URL = "https://api-sdm-di.cfapps.eu10.hana.ondemand.com/browser/";
	public static final String DMS_REPOSITORY_ID = "e95a444c-5dbd-4d94-bf02-162beacc665e";
	public static final String DMS_REPO_ROOT = "root";
	public static final String DMS_REPOSITORY_NAME = "chatbot";
	public static final String DMS_PDF_FOLDER = "pdf";
	public static final String DMS_IMAGE_FOLDER = "images";
	public static final String DMS_TABLE_FOLDER = "tables";
	public static final String DMS_FOLDER_SUFFIX = "_folder";

}
