package com.tsdata.model.factor.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

public class HttpConnectionManager4 {
	private static String CONTENTTYPE = PropertiesUtil.getStringValue("contentType");

	private static int connectimeOut = NumberUtils.toInt(PropertiesUtil.getStringValue("connectimeOut"), 3) * 1000;
	private static int readtimeOut = NumberUtils.toInt(PropertiesUtil.getStringValue("readtimeOut"), 5) * 1000;
	private static int pooltimeOut = NumberUtils.toInt(PropertiesUtil.getStringValue("pooltimeOut"), 5) * 1000;

	private static String keytoolPwd = PropertiesUtil.getStringValue("keytool.pwd");
	private static String keystorePath = PropertiesUtil.getStringValue("keyStorePath");
	private static String trustStorePath = PropertiesUtil.getStringValue("trustStorePath");

	private static boolean isIgnoreCertificate = PropertiesUtil.getBooleanValue("isIgnoreCertificate");

	private static boolean isProxy = PropertiesUtil.getBooleanValue("isProxy");
	private static String proxyHost = PropertiesUtil.getStringValue("proxyHost");
	private static int proxyPort = NumberUtils.toInt(PropertiesUtil.getStringValue("proxyPort"), 80);

	private static int maxTotal = NumberUtils.toInt(PropertiesUtil.getStringValue("maxTotal"), 800);
	private static int maxPerRoute = NumberUtils.toInt(PropertiesUtil.getStringValue("maxPerRoute"), 100);

	private static final Object syncLock = new Object();
	private static PoolingHttpClientConnectionManager cm;
	private static CloseableHttpClient httpClient;

	static {
		try {
			SSLContext sslcontext = null;
			try {
				if (isIgnoreCertificate) {
					sslcontext = SSLContexts.custom().loadTrustMaterial((TrustStrategy) new TrustSelfSignedStrategy())
							.build();
				} else {
					sslcontext = HttpsPoster.getSSLContext(keytoolPwd, keystorePath, trustStorePath);
				}

			} catch (KeyManagementException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyStoreException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			SSLConnectionSocketFactory sslsf = isIgnoreCertificate
					? new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
					: new SSLConnectionSocketFactory(sslcontext, HttpsPoster.hnv);

			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslsf).build();

			cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			cm.setMaxTotal(maxTotal);
			cm.setDefaultMaxPerRoute(maxPerRoute);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("SSLv3");

		X509TrustManager trustManager = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString)
					throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString)
					throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

	public static CloseableHttpClient getHttpClient() {
		if (httpClient == null) {
			synchronized (syncLock) {
				if (httpClient == null) {
					httpClient = createHttpClient();
				}
			}
		}
		return httpClient;
	}

	public static CloseableHttpClient createHttpClient() {
		RequestConfig requestConfig = null;
		if (isProxy) {
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			requestConfig = RequestConfig.custom().setConnectTimeout(connectimeOut)
					.setConnectionRequestTimeout(pooltimeOut).setSocketTimeout(readtimeOut).setProxy(proxy)
					.setCookieSpec("best-match").build();
		} else {
			requestConfig = RequestConfig.custom().setConnectTimeout(connectimeOut)
					.setConnectionRequestTimeout(pooltimeOut).setSocketTimeout(readtimeOut).setCookieSpec("best-match")
					.build();
		}

		HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				if (executionCount >= 3) {
					return false;
				}
				if (exception instanceof org.apache.http.NoHttpResponseException) {
					return true;
				}
				if (exception instanceof javax.net.ssl.SSLHandshakeException) {
					return false;
				}
				if (exception instanceof java.io.InterruptedIOException) {
					return false;
				}
				if (exception instanceof java.net.UnknownHostException) {
					return false;
				}
				if (exception instanceof org.apache.http.conn.ConnectTimeoutException) {
					return false;
				}
				if (exception instanceof javax.net.ssl.SSLException) {
					return false;
				}

				HttpClientContext clientContext = HttpClientContext.adapt(context);
				HttpRequest request = clientContext.getRequest();

				if (!(request instanceof org.apache.http.HttpEntityEnclosingRequest))
					return true;
				return false;
			}
		};

		LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();
		httpClient = HttpClients.custom().setConnectionManager((HttpClientConnectionManager) cm)
				.setDefaultRequestConfig(requestConfig).setRedirectStrategy((RedirectStrategy) redirectStrategy)
				.setRetryHandler(httpRequestRetryHandler).build();

		return httpClient;
	}

	private static void setPostParams(HttpPost httpost, Map<String, Object> params) {
		if (CONTENTTYPE.equals("1")) {
			setKeyValueParams(httpost, params);
		} else if (CONTENTTYPE.equals("2")) {
			setJsonParams(httpost, params);
		} else {
			setKeyValueParams(httpost, params);
		}
	}

	private static void setKeyValueParams(HttpPost httpost, Map<String, Object> params) {
		try {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			Set<String> keySet = params.keySet();
			for (String key : keySet) {
				nvps.add(new BasicNameValuePair(key, params.get(key).toString()));
			}
			httpost.setEntity((HttpEntity) new UrlEncodedFormEntity(nvps, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private static void setJsonParams(HttpPost httpost, Map<String, Object> params) {
		StringEntity entity = new StringEntity(JSONObject.fromObject(params).toString(), "UTF-8");
		entity.setContentEncoding("UTF-8");
		entity.setContentType("application/json");
		httpost.setEntity((HttpEntity) entity);
	}

	public static String post(String url, Map<String, Object> params) throws Exception {
    HttpPost httppost = new HttpPost(url);
    setPostParams(httppost, params);
    CloseableHttpResponse response = null;
    String result = "";
    try {
      response = getHttpClient().execute((HttpUriRequest)httppost);
      HttpEntity entity = response.getEntity();
      result = EntityUtils.toString(entity, "UTF-8");
      EntityUtils.consume(entity);
    } catch (Exception e) {
      JSONObject jsonjsonObject2 = new JSONObject();
      jsonjsonObject2.put("code", "900005");
      jsonjsonObject2.put("message", "HttpClient"+ e);
      return jsonjsonObject2.toString();
    } finally {
      try {
        if (response != null)
          response.close(); 
        httppost.releaseConnection();
      } catch (IOException e) {
        e.printStackTrace();
        JSONObject jsonjsonObject2 = new JSONObject();
        jsonjsonObject2.put("code", "900006");
        jsonjsonObject2.put("message", "post"+ e);
        return jsonjsonObject2.toString();
      } 
    } 
    return result;
  }

}
