package com.tsdata.model.factor.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.lang.math.NumberUtils;

import com.tsdata.model.factor.vo.MyAuthenticator;

import sun.net.www.protocol.http.Handler;

public class HttpsPoster {
	private static boolean isIgnoreCertificate = PropertiesUtil.getBooleanValue("isIgnoreCertificate");
	private static SSLContext sslContext;
	static HostnameVerifier hnv = new MyHostnameVerifier();

	public static KeyStore getKeyStore(String password, String keyStorePath) {
		KeyStore ks = null;

		InputStream is = null;
		try {
			ks = KeyStore.getInstance("JKS");
			is = HttpsPoster.class.getClassLoader().getResourceAsStream(keyStorePath);

			ks.load(is, password.toCharArray());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ks;
	}

	public static SSLContext getSSLContext(String password, String keyStorePath, String trustStorePath)
			throws Exception {
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

		KeyStore keyStore = getKeyStore(password, keyStorePath);

		keyManagerFactory.init(keyStore, password.toCharArray());

		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());

		KeyStore trustStore = getKeyStore(password, trustStorePath);

		trustManagerFactory.init(trustStore);

		SSLContext ctx = SSLContext.getInstance("TLS");

		ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

		return ctx;
	}

	public static void initHttpsURLConnection(String password, String keyStorePath, String trustStorePath)
			throws Exception {
		try {
			sslContext = getSSLContext(password, keyStorePath, trustStorePath);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		if (isIgnoreCertificate) {
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			sslContext.init(null, new TrustManager[] { tm }, null);
		}
	}

	public static String sendPost(String url, String param, boolean isProxy) throws IOException {
		HttpURLConnection conn = null;
		OutputStream out = null;
		BufferedReader in = null;
		StringBuffer sb = new StringBuffer();
		try {
			URL url1 = new URL(null, url, new Handler());
			if (isProxy) {
				InetSocketAddress addr = new InetSocketAddress(PropertiesUtil.getStringValue("proxyHost"),
						PropertiesUtil.getIntegerValue("proxyPort").intValue());

				Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
				conn = (HttpURLConnection) url1.openConnection(proxy);
			} else {
				conn = (HttpURLConnection) url1.openConnection();
			}

			conn.setConnectTimeout(NumberUtils.toInt(PropertiesUtil.getStringValue("connnectimeOut"), 3) * 1000);
			conn.setReadTimeout(NumberUtils.toInt(PropertiesUtil.getStringValue("readtimeOut"), 5) * 1000);

			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Length", String.valueOf((param.getBytes()).length));

			conn.setUseCaches(false);

			out = conn.getOutputStream();
			out.write(param.getBytes("UTF-8"));

			out.flush();
			conn.getOutputStream().close();
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
		} finally {

			if (null != out) {
				out.close();
			}
			if (null != in) {
				in.close();
			}
			if (null != conn) {
				conn.disconnect();
			}
		}
		return sb.toString();
	}

	public static String post(String url, String post_str, boolean isProxy) throws Exception {
		HttpsURLConnection urlCon = null;
		OutputStream out = null;
		BufferedReader in = null;
		StringBuffer sb = new StringBuffer();
		try {
			URL url1 = new URL(null, url, new Handler());
			if (isProxy) {
				InetSocketAddress addr = new InetSocketAddress(PropertiesUtil.getStringValue("proxyHost"),
						PropertiesUtil.getIntegerValue("proxyPort").intValue());

				Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
				urlCon = (HttpsURLConnection) url1.openConnection(proxy);
			} else {
				urlCon = (HttpsURLConnection) url1.openConnection();
			}
			urlCon.setSSLSocketFactory(sslContext.getSocketFactory());
			urlCon.setHostnameVerifier(hnv);
			urlCon.setConnectTimeout(NumberUtils.toInt(PropertiesUtil.getStringValue("connnectimeOut"), 3) * 1000);
			urlCon.setReadTimeout(NumberUtils.toInt(PropertiesUtil.getStringValue("readtimeOut"), 5) * 1000);
			urlCon.setDoInput(true);
			urlCon.setDoOutput(true);
			urlCon.setRequestMethod("POST");
			urlCon.setRequestProperty("Content-Length", String.valueOf((post_str.getBytes()).length));

			urlCon.setUseCaches(false);

			out = urlCon.getOutputStream();
			out.write(post_str.getBytes("UTF-8"));
			out.flush();
			urlCon.getOutputStream().close();
			in = new BufferedReader(new InputStreamReader(urlCon.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
		} finally {
			if (null != out) {
				out.close();
			}
			if (null != in) {
				in.close();
			}
			if (null != urlCon) {
				urlCon.disconnect();
			}
		}
		return sb.toString();
	}

	public static void setDefaultAuthentication() {
		MyAuthenticator auth = new MyAuthenticator("bairong", "Proxy_br");
		Authenticator.setDefault((Authenticator) auth);
	}

	static class MyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	public static class SavingTrustManager implements X509TrustManager {
		private final X509TrustManager tm;

		private X509Certificate[] chain;

		public SavingTrustManager(X509TrustManager tm) {
			this.tm = tm;
		}

		public X509TrustManager getTM() {
			return this.tm;
		}

		public X509Certificate[] getChain() {
			return this.chain;
		}

		public X509Certificate[] getAcceptedIssuers() {
			throw new UnsupportedOperationException();
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			throw new UnsupportedOperationException();
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			this.chain = chain;
			this.tm.checkServerTrusted(chain, authType);
		}
	}
}
