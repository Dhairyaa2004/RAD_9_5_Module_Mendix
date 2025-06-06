package documentgeneration.implementation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.mendix.core.Core;
import com.mendix.http.HttpHeader;
import com.mendix.http.HttpMethod;
import com.mendix.http.HttpResponse;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.thirdparty.org.json.JSONException;
import com.mendix.thirdparty.org.json.JSONObject;

public class TokenRequest {
	private String grantType;
	private LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();

	public TokenRequest(String grantType) {
		this.grantType = grantType;
	}

	public TokenRequest addFormData(String key, String value) {
		data.put(key, value);
		return this;
	}

	public String getRequestBody() {
		StringBuilder requestBuilder = new StringBuilder();
		requestBuilder.append("grant_type=" + this.grantType);

		for (Entry<String, String> entry : data.entrySet()) {
			requestBuilder.append("&");
			requestBuilder.append(entry.getKey());
			requestBuilder.append("=");
			requestBuilder.append(entry.getValue());
		}

		return requestBuilder.toString();
	}

	public HttpResponse execute() {
		HttpHeader[] headers = new HttpHeader[] { new HttpHeader("Content-Type", "application/x-www-form-urlencoded") };

		return Core.http().executeHttpRequest(ConfigurationManager.getTokenEndpoint(), HttpMethod.POST, headers,
				new ByteArrayInputStream(this.getRequestBody().getBytes()));
	}

	public static JSONObject parseTokenResponse(IContext context, String response) {
		try {
			return new JSONObject(response);
		} catch (JSONException e) {
			throw new RuntimeException("Token response is not a valid JSON object");
		}
	}
	
	public static JSONObject parseTokenResponse(IContext context, HttpResponse response) throws IOException {
		try (InputStream is = response.getContent()) {
			String rawResponse = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			return TokenRequest.parseTokenResponse(context, rawResponse);
		}
	}
}
