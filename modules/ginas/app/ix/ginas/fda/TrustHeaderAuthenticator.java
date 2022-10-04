package ix.ginas.fda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.annotation.JSONP;
import ix.core.auth.AuthenticationCredentials;
import ix.core.auth.Authenticator;
import ix.core.models.UserProfile;
import ix.core.util.CachedSupplier;
import ix.ncats.controllers.auth.Authentication;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import play.Logger;
import play.Play;
import play.mvc.Http;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;
import scala.Option;
import scala.util.parsing.json.JSON;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by katzelda on 5/4/16.
 */
public class TrustHeaderAuthenticator implements Authenticator {

	CachedSupplier<Boolean> trustheader = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getBoolean("ix.authentication.trustheader",false);
	});


	CachedSupplier<String> usernameheader = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getString("ix.authentication.usernameheader");
	});
	CachedSupplier<String> useremailheader = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getString("ix.authentication.useremailheader");
	});
	CachedSupplier<String> initialtokensupplier = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getString("initialtoken");
	});
	CachedSupplier<String> uuidheader = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getString("ix.authentication.uuidheader");
	});

	CachedSupplier<String> initialauthurl = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getString("initialauthurl");
	});
	CachedSupplier<String> subscriptionurl = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getString("subscriptionurl");
	});
	CachedSupplier<String> firstnameheader = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getString("ix.authentication.firstnameheader");
	});
	CachedSupplier<String> lastnameheader = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getString("ix.authentication.lastnameheader");
	});
	CachedSupplier<String> groupmembershipheader = CachedSupplier.of(() -> {
		return Play.application()
				.configuration()
				.getString("ix.authentication.groupmembershipheader");
	});

	@Override
	public UserProfile authenticate(AuthenticationCredentials credentials) {
		Logger.trace("in TrustHeaderAuthenticator authenticate");
		if(!trustheader.get())return null;
		if(usernameheader.get()==null)return null;

		Http.Context context = credentials.getContext();
		if(context == null)return null;
		return loginUserFromHeader(context.request());
	}

	private UserProfile loginUserFromHeader(Http.Request r) {
		try {
			UserInfo ui=getUserInfoFromHeaders(r);
			if (ui!= null && ui.username != null) {
				return Authentication.setUserProfileSessionUsing(ui.username, ui.email, ui.firstName);
			}
			Logger.trace("allowing caller to handle redirection ");
		} catch (Exception e) {
			Logger.warn("Error authenticating", e);
		}
		return null;
	}

	private class UserInfo{
		String username;
		String email;
		String firstName;

		UserInfo(String username, String email, String firstName){
			this.username=username;
			this.email=email;
			this.firstName =firstName;
		}
		UserInfo(String username, String email){
			this.username=username;
			this.email=email;
		}
	}


	private UserInfo getUserInfoFromHeaders(Http.Request r){
		Logger.trace("starting in getUserInfoFromHeaders");
		String username = r.getHeader(usernameheader.get());
		String useremail = r.getHeader(useremailheader.get());
		String uuid =r.getHeader(uuidheader.get());
		String firstName = r.getHeader(firstnameheader.get());
		String lastName = r.getHeader(lastnameheader.get());
		String groupMembership =r.getHeader(groupmembershipheader.get());
		/*Logger.debug("groupmembershipheader: " + groupmembershipheader.get()
			+ "; value: " + groupMembership);*/

		groupMembership =formatGroups(groupMembership);
		//todo: remove this debug info!
		String msg = String.format("username: %s; useremail: %s; firstName: %s, lastName: %s, groups: %s",
				username, useremail, firstName, lastName, groupMembership);
		Logger.trace(msg);
		if( groupMembership==null ||groupMembership.length()==0) {
			Logger.trace("No group memberships found for user " + username);
		}
		String initialToken = getFirstAuthenticationToken();

		if(!getAuthorizationInfo(initialToken, uuid, useremail, groupMembership)){
			Logger.error("user is NOT authorized for GSRS!");
			//todo: percolate message to UI
			return null;
		}
		return new UserInfo(username, useremail, firstName);
	}

	private String formatGroups(String inputGroups) {
		if( inputGroups ==null || inputGroups.length()==0) {
			Logger.info("in formatGroups, inputGroups is empty/null");
			return null;
		}
		String[] groups =inputGroups.split(",");
		List<String> cleanGroups = new ArrayList<>();
		for(String rawGroup : groups) {
			String gr = rawGroup.trim();
			if( !gr.startsWith("\"")) gr="\"" + gr;
			if( !gr.endsWith("\"")) gr= gr +"\"";
			cleanGroups.add(gr);
		}
		return String.join(",", cleanGroups);
	}
	private boolean getAuthorizationInfo(String bearerToken, String uuid, String email, String groupInfo) {
		Logger.trace("starting in getAuthorizationInfo");
		Map<String, String> headers = new HashMap<>();
		String bearerValue = String.format("Bearer %s",bearerToken);
		headers.put("Authorization", bearerValue);
		headers.put("Accept", "application/json");
		headers.put("Content-Type", "application/json");
		String authorizationRequest ="";
		if(groupInfo == null || groupInfo.length()==0) {
			authorizationRequest = String.format("{ \"uuid\": \"%s\", \"email\": \"%s\"}",
					uuid, email);
		} else {
			authorizationRequest = String.format("{ \"uuid\": \"%s\", \"email\": \"%s\", \"groups\":[%s]}",
					uuid, email, groupInfo);
		}
		Logger.trace("authorizationRequest: " + authorizationRequest);
		String authorizationUrl = subscriptionurl.get(); //authorization means checking the subscription status of the user
		//Logger.debug("authorizationUrl: " + authorizationUrl);
		String authorizationData = performPostUsingClient(authorizationUrl, authorizationRequest, headers, null);
		return parseRawAuth(authorizationData);
	}

	private boolean parseRawAuth(String rawAuth) {
		ObjectMapper mapper = new ObjectMapper();
		DeserializationConfig config = mapper.getDeserializationConfig();
		mapper.setConfig(config.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
		try {
			JsonNode node = mapper.readValue(rawAuth, JsonNode.class);
			if( node == null) {
				Logger.error("top-level auth node null!");
				return false;
			}
			JsonNode applicationsNode= node.get("applications");
			if( applicationsNode== null ) {
				Logger.error("applicationsNode null!");
				return false;
			}
			//Logger.debug("applicationNode: " + applicationsNode.asText());
			List<JsonNode> applicationRecords =applicationsNode.findValues("records");
			JsonNode productNode =applicationRecords.get(0).findValue("products");
			if(productNode == null) {
				Logger.error("productsNode null!");
				return false;
			}
			List<JsonNode> productRecords= productNode.findValues("records");
			if( productRecords == null || productRecords.size() ==0) {
				Logger.error("productsRecords null or empty!");
				return false;
			}
			JsonNode featuresNode= productRecords.get(0).findValue("features");
			if( featuresNode==null ){
				Logger.error("featuresNode not found!");
				return false;
			}
			List<JsonNode> featuresRecords = featuresNode.findValues("records");
			if( featuresRecords == null || featuresRecords.size()==0) {
				Logger.error("featuresRecords null or empty!");
				return false;
			}
			if(featuresRecords.get(0).findValue("status") != null){
				return featuresRecords.get(0).findValue("status").toString().trim().equals("1");
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return false;
	}

	private String getFirstAuthenticationToken() {
		Logger.trace("starting in getFirstAuthenticationToken");
		String requestData="grant_type=client_credentials";
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", "Basic " +  initialtokensupplier.get());
		//Logger.debug("auth: " + "Basic " +  initialtokensupplier.get());
		headers.put("Accept", "application/json");
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		String url = initialauthurl.get();
		//Logger.debug("getFirstAuthenticationToken using url: " + url);
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("grant_type", "client_credentials"));
		String info = performPostUsingClient(url, null, headers, params);
		Logger.debug("received info: " +info);
		ObjectMapper mapper = new ObjectMapper();
		DeserializationConfig config = mapper.getDeserializationConfig();
		mapper.setConfig(config.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
		try {
			JsonNode node = mapper.readValue(info, JsonNode.class);
			JsonNode tokenNode= node.get("access_token");
			if( tokenNode!= null && tokenNode.asText() != null) {
				Logger.trace("tokenNode: " + tokenNode.asText());
				return tokenNode.asText();
			} else {
				Logger.warn("tokenNode not parsed");
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String performPostUsingClient(String url, String data, Map<String, String> headers,
												 List<NameValuePair> parameters) {
		Logger.trace("starting in performPostUsingClient");
		HttpClient client = HttpClients.createDefault();
				//HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		for(String header : headers.keySet()) {
			post.setHeader(header, headers.get(header));
			Logger.trace("set header " + header + " to " + headers.get(header));
		}

		try {
			if( data !=null && data.length()>0) {
				StringEntity postData = new StringEntity(data);
				//Logger.trace("created postData using " + data);
				//post.addHeader("Content-Type", "application/json");
				//post.addHeader("Accept", "application/json");
				post.setEntity(postData);
			}
			if( parameters != null && parameters.size() >0) {
				Logger.trace("using parameters");
				parameters.forEach(p-> Logger.trace("name: " + p.getName() + "; value: " + p.getValue()));
				post.setEntity(new UrlEncodedFormEntity( parameters, "UTF-8"));
			}
			Logger.trace("about to call client.execute");
			HttpResponse response = client.execute(post);
			String resultMessage = String.format("result: status: %d; reason: %s",
				response.getStatusLine().getStatusCode(),
						response.getStatusLine().getReasonPhrase());
			Logger.trace(resultMessage);
			InputStream is = response.getEntity().getContent();
			StringBuilder textBuilder = new StringBuilder();
			try (Reader reader = new BufferedReader(new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name())))) {
				int c = 0;
				while ((c = reader.read()) != -1) {
					textBuilder.append((char) c);
				}
			}
			is.close();
			String result = textBuilder.toString();
			Logger.trace("got result: " + result);
			return result;
		} catch (IOException ex) {
			Logger.error( "Error making httpClient call: " + ex.getMessage());
			ex.printStackTrace();
		}
		return null;
	}

}
