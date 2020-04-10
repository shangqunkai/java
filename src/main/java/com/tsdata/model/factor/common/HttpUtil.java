package com.tsdata.model.factor.common;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
 
public class HttpUtil {
	public static class Request {

		public static final String METHOD_GET = "GET";
		public static final String METHOD_POST = "POST";

	}
		
	public static class Response {
		public Response(int status, Map<String, String> headers, String responseBody) {
			this.status = status;
			this.responseBody = responseBody;
			this.headers = headers;
		}

		public final int status;
		public final Map<String, String> headers;
		public String responseBody;
	}
	
	public static Response doDispatcher(String requestUrl, String method) {

		return doDispatcher(requestUrl, method, null,null);
	}

	public static Response doDispatcher(String requestUrl, String method, String requestBody) {

		return doDispatcher(requestUrl, method, requestBody,null);
	}

	public static Response doDispatcher(String requestUrl, String method, String requestBody,
			Map<String, String> headers) {
		CloseableHttpClient httpclient = findCloseableHttpClient(requestUrl);
		CloseableHttpResponse resp = null;
		String responseBody = null;
		
		try {
			if (HttpGet.METHOD_NAME.equals(method)) {
				HttpGet httpGet = new HttpGet(requestUrl);
				resp = httpclient.execute(httpGet);
			}
			if (HttpPost.METHOD_NAME.equals(method)) {
				HttpPost httpPost = new HttpPost(requestUrl);
				System.out.println(requestUrl);
				System.out.println(requestBody);
				fillHeaders(httpPost, headers);
				httpPost.setEntity(new StringEntity(requestBody, Charset.forName("utf-8")));

				resp = httpclient.execute(httpPost);
			}

			responseBody = EntityUtils.toString(resp.getEntity(),Charset.forName("utf-8"));
			resp.close();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				httpclient.close();
			} catch (IOException e1) {
			}
		}
		return new Response(resp.getStatusLine().getStatusCode(), headers, responseBody);
	}
	
	private static CloseableHttpClient findCloseableHttpClient(String requestUrl) {

		if (requestUrl != null && requestUrl.startsWith("https")) {
			return HttpsClients.getHttpsClient();
		}
		if (requestUrl != null && requestUrl.startsWith("http")) {
			return HttpClients.createDefault();
		}
		throw new RuntimeException("request protocol not supported");

	}
	
	private static void fillHeaders(HttpPost httpPost, Map<String, String> headers) {
		if (headers != null) {
			for (Entry<String, String> kvs : headers.entrySet()) {
				httpPost.addHeader(kvs.getKey(), kvs.getValue());
			}
		}
	}

	private static class HttpsClients {
		public static CloseableHttpClient getHttpsClient() {
			try {
				SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(anyTrustStrategy).build();
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, anyHostnameVerifier);
				return HttpClients.custom().setSSLSocketFactory(sslsf).build();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		private static TrustStrategy anyTrustStrategy = new TrustStrategy() {
			@Override
			public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				return true;
			}
		};

		private static HostnameVerifier anyHostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};
	}

}
