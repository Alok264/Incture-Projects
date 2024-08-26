package cw.imo.chatbot.utils;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Base64;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.jdbc.CfJdbcEnv;

@Component
public class DestinationReaderUtil {
	private String uri;

	private String tokenUrl;

	private String clientId;

	private String clientSecret;

	public String accessToken() throws JsonMappingException, JsonProcessingException {
		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		CfCredentials cfCredentials = cfJdbcEnv.findCredentialsByTag("destination");
		System.out.println("Got desc connection" + cfCredentials.getName());
		Map<String, Object> map = cfCredentials.getMap();
		uri = (String) map.get("uri");
		tokenUrl = (String) map.get("url");
		clientId = (String) map.get("clientid");
		clientSecret = (String) map.get("clientsecret");
		System.err.println("Destination datails" + map);
		String url = tokenUrl + "/oauth/token?grant_type=client_credentials";
		RestTemplate template = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		Base64 encode = Base64.encode(clientId + ":" + clientSecret);
		headers.add("Authorization", "Basic " + encode.toString());
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = template.postForEntity(url, entity, String.class);
		System.err.println("res" + response);
		return new ObjectMapper().readTree(response.getBody()).get("access_token").asText();
	}

	public String readDestinationDestination(String destinationName, String body)
			throws ClientProtocolException, IOException {
		System.err.println("DestinationAccessHelper.readDestinationDestination()");
		HttpHeaders headers = new HttpHeaders();
		String accessToken = accessToken();
		System.out.println("accessToken: " + accessToken);
		System.err.println("DestinationAccessHelper.readDestinationDestination() " + accessToken);
		headers.add("Authorization", "Bearer " + accessToken);
		String url = uri + "/destination-configuration/v1/destinations/" + destinationName;
		HttpResponse httpResponse = null;
		String jsonString = null;
		HttpRequestBase httpRequestBase = null;
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		httpRequestBase = new HttpGet(url);
		httpRequestBase.addHeader("Authorization", "Bearer " + accessToken);
		httpResponse = httpClient.execute(httpRequestBase);
		jsonString = EntityUtils.toString(httpResponse.getEntity());
		System.err.println("Destination response :" + jsonString);
		return jsonString;
	}

}