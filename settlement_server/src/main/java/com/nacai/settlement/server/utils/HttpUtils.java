package com.nacai.settlement.server.utils;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by shan on 2018/5/29.
 */
public class HttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    private static int SOCKET_TIMEOUT = 40000;
    private static int CONNECT_TIMEOUT = 40000;
    private static int MAX_TOTAL = 100;

    /**
     * Http Post（支持http、https）
     *
     * @param url
     * @param params
     * @return
     */
    public static String post(String url, Map<String, Object> params) {

        String result = null;

        // 组装参数
        List<NameValuePair> paramList = new LinkedList<NameValuePair>();

        if (params != null) {
            //params.forEach((k, v) -> paramList.add(
              //      new BasicNameValuePair(k, v == null ? "" : v.toString())));
        }

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(paramList, Consts.UTF_8));

        CloseableHttpResponse response = null;
        try {
            // 获取连接客户端
            CloseableHttpClient httpClient = getHttpClient();
            // 发起请求
            response = httpClient.execute(httpPost);
            int respCode = response.getStatusLine().getStatusCode();

            // 如果是重定向
            if (HttpStatus.SC_MOVED_TEMPORARILY == respCode) {
                String locationUrl = response.getLastHeader("Location").getValue();
                post(locationUrl, params);
            }

            //响应200
            if (HttpStatus.SC_OK == respCode) {
                // 获得响应实体
                HttpEntity entity = response.getEntity();
                // 获取响应字符串
                result = EntityUtils.toString(entity, Consts.UTF_8);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

        }

        return result;
    }

    /**
     * 获取HttpClient实例
     *
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
        try {

            SSLContext sslContext = SSLContext.getInstance("SSLv3");

            // 取消检测SSL
            X509TrustManager x509TrustManager = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            };
            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);

            //SSL Socket连接配置
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", sslConnectionSocketFactory)
                    .build();

            //连接池配置
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
            cm.setMaxTotal(MAX_TOTAL);
            cm.setDefaultMaxPerRoute(cm.getMaxTotal());

            //请求配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .build();

            //创建HttpClient
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    .setDefaultCookieStore(new BasicCookieStore())
                    .setConnectionManager(cm)
                    .setDefaultRequestConfig(requestConfig)
                    .build();

            return httpclient;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return HttpClients.createDefault();
    }

    public static void main(String[] args) {
        System.out.println(HttpUtils.post("https://www.baidu.com", null));
    }
}
