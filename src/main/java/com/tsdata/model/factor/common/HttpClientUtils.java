package com.tsdata.model.factor.common;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import net.sf.json.JSONObject;

public class HttpClientUtils {

	public static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
    private static CloseableHttpClient httpClient;
    private static PoolingHttpClientConnectionManager manager; // 连接池管理类
    private static ScheduledExecutorService monitorExecutor; // 监控
    private final static Object syncLock = new Object(); // 相当于线程锁,用于线程安全
    private static final int CONNECT_TIMEOUT = HttpClientConfig.getHttpConnectTimeout();// 设置连接建立的超时时间为10s
    private static final int SOCKET_TIMEOUT = HttpClientConfig.getHttpSocketTimeout();
    private static final int MAX_CONN = HttpClientConfig.getHttpMaxPoolSize(); // 最大连接数
    private static final int Max_PRE_ROUTE = HttpClientConfig.getHttpMaxPoolSize();
    private static final int MAX_ROUTE = HttpClientConfig.getHttpMaxPoolSize();

    /**
     * 对http请求进行基本设置
     * 
     * @param httpRequestBase
     *            http请求
     */
    private static void setRequestConfig(HttpRequestBase httpRequestBase) {
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECT_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        httpRequestBase.setConfig(requestConfig);
    }

    public static CloseableHttpClient getHttpClient(String url) {
        String hostName = url.split("/")[2];
        // System.out.println(hostName);
        int port = 80;
        if (hostName.contains(":")) {
            String[] args = hostName.split(":");
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        }
        if (httpClient == null) {
            // 多线程下多个线程同时调用getHttpClient容易导致重复创建httpClient对象的问题,所以加上了同步锁
            synchronized (syncLock) {
                if (httpClient == null) {
                    httpClient = createHttpClient(hostName, port);
                    // 开启监控线程,对异常和空闲线程进行关闭
                    monitorExecutor = Executors.newScheduledThreadPool(1);
                    monitorExecutor.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            // 关闭异常连接
                            manager.closeExpiredConnections();
                            // 关闭5s空闲的连接
                            manager.closeIdleConnections(HttpClientConfig.getHttpIdelTimeout(), TimeUnit.MILLISECONDS);
                            logger.debug("close expired and idle for over 5s connection");
                        }
                    }, HttpClientConfig.getHttpMonitorInterval(), HttpClientConfig.getHttpMonitorInterval(),
                            TimeUnit.MILLISECONDS);
                }
            }
        }
        return httpClient;
    }

    /**
     * 根据host和port构建httpclient实例
     * 
     * @param host
     *            要访问的域名
     * @param port
     *            要访问的端口
     * @return
     */
    public static CloseableHttpClient createHttpClient(String host, int port) {
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", plainSocketFactory).register("https", sslSocketFactory).build();
        manager = new PoolingHttpClientConnectionManager(registry);
        // 设置连接参数
        manager.setMaxTotal(MAX_CONN); // 最大连接数
        manager.setDefaultMaxPerRoute(Max_PRE_ROUTE); // 路由最大连接数
        HttpHost httpHost = new HttpHost(host, port);
        manager.setMaxPerRoute(new HttpRoute(httpHost), MAX_ROUTE);
        // 请求失败时,进行请求重试
        HttpRequestRetryHandler handler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i > 3) {
                    // 重试超过3次,放弃请求
                    logger.error("retry has more than 3 time, give up request");
                    return false;
                }
                if (e instanceof NoHttpResponseException) {
                    // 服务器没有响应,可能是服务器断开了连接,应该重试
                    logger.error("receive no response from server, retry");
                    return true;
                }
                if (e instanceof SSLHandshakeException) {
                    // SSL握手异常
                    logger.error("SSL hand shake exception");
                    return false;
                }
                if (e instanceof InterruptedIOException) {
                    // 超时
                    logger.error("InterruptedIOException");
                    return false;
                }
                if (e instanceof UnknownHostException) {
                    // 服务器不可达
                    logger.error("server host unknown");
                    return false;
                }
                if (e instanceof ConnectTimeoutException) {
                    // 连接超时
                    logger.error("Connection Time out");
                    return false;
                }
                if (e instanceof SSLException) {
                    logger.error("SSLException");
                    return false;
                }
                HttpClientContext context = HttpClientContext.adapt(httpContext);
                HttpRequest request = context.getRequest();
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    // 如果请求不是关闭连接的请求
                    return true;
                }
                return false;
            }

			
        };
        CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).setRetryHandler(handler)
                .build();
        return client;
    }

    /**
     * 关闭连接池
     */
    public static void closeConnectionPool() {
        try {
            httpClient.close();
            manager.close();
            monitorExecutor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String doPostJson(String url, JSONObject json) {
        HttpPost httpPost = new HttpPost(url);
        setRequestConfig(httpPost);
        String resultString = "";
        CloseableHttpResponse response = null;
        try {
            // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
            // httpPost.setHeader("Content-Type",
            // "application/json;charset=utf8");
            httpPost.setHeader("Content-Type", "application/json");

            // 创建请求内容
            StringEntity entity = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            response = getHttpClient(url).execute(httpPost, HttpClientContext.create());
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            logger.error("httpclient的get请求失败,url:" + url, e);
            // e.printStackTrace();
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                logger.error("IOException的错误", e);
                // e.printStackTrace();
            }
        }
        return resultString;
    }
    
    public static String doPostJson(String url, String json) {
        HttpPost httpPost = new HttpPost(url);
        setRequestConfig(httpPost);
        String resultString = "";
        CloseableHttpResponse response = null;
        try {
            // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
            // httpPost.setHeader("Content-Type",
            // "application/json;charset=utf8");
            httpPost.setHeader("Content-Type", "application/json");

            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            response = getHttpClient(url).execute(httpPost, HttpClientContext.create());
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            logger.error("httpclient的get请求失败,url:" + url, e);
            // e.printStackTrace();
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                logger.error("IOException的错误", e);
                // e.printStackTrace();
            }
        }
        return resultString;
    }
    
    public static String doPostForm(String url, JSONObject params) {
        HttpPost httpPost = new HttpPost(url);
        setRequestConfig(httpPost);
        String resultString = "";
        CloseableHttpResponse response = null;
        try {

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            if (params != null) {
                for (Object key : params.keySet()) {
                    builder.addPart((String) key,
                            new StringBody(params.get(key).toString(), ContentType.create("text/plain", Consts.UTF_8)));
                }
            }

            HttpEntity reqEntity = builder.build();
            httpPost.setEntity(reqEntity);

            // 发起请求 并返回请求的响应
            response = getHttpClient(url).execute(httpPost, HttpClientContext.create());
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }
    
    public static String doPostFormMap(String url, Map<String, String> params) {
        HttpPost httpPost = new HttpPost(url);
        setRequestConfig(httpPost);
        String resultString = "";
        CloseableHttpResponse response = null;
        try {

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            if (params != null) {
                for (String key : params.keySet()) {
                    builder.addPart(key,
                            new StringBody(params.get(key), ContentType.create("text/plain", Consts.UTF_8)));
                }
            }

            HttpEntity reqEntity = builder.build();
            httpPost.setEntity(reqEntity);

            // 发起请求 并返回请求的响应
            response = getHttpClient(url).execute(httpPost, HttpClientContext.create());
            resultString = EntityUtils.toString(response.getEntity(), "utf-8");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }
}