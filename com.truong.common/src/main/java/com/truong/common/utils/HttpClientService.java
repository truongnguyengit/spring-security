package com.truong.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpClientService {

	public static <T> T get(String url, Map<String, String> headers, Map<String, String> params, Class<T> objectclass,
			int timeout) {
		try {

			CloseableHttpClient httpClient = HttpClients.createDefault();

			URIBuilder builder = new URIBuilder(url);

			for (Map.Entry<String, String> entry : params.entrySet()) {
				builder.setParameter(entry.getKey().toString(), entry.getValue().toString());
			}

			HttpGet httpGet = new HttpGet(builder.build());
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpGet.addHeader(entry.getKey().toString(), entry.getValue().toString());
			}

			CloseableHttpResponse response = httpClient.execute(httpGet);

			String jsonString = EntityUtils.toString(response.getEntity());

			ObjectMapper mapper = new ObjectMapper();
			T data = (T) mapper.readValue(jsonString, objectclass);

			return data;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static <T> T post(String url, Map<String, String> headers, Object params, Class<T> objectclass,
			int timeout) {
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();

			URIBuilder builder = new URIBuilder(url);

			HttpPost httpPost = new HttpPost(builder.build());
			httpPost.addHeader("Content-Type", "application/json");
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpPost.addHeader(entry.getKey().toString(), entry.getValue().toString());
			}

			ObjectMapper mapper = new ObjectMapper();
			String jsonParams = mapper.writeValueAsString(params);
			StringEntity requestEntity = new StringEntity(jsonParams, ContentType.APPLICATION_JSON);

			httpPost.setEntity(requestEntity);

			CloseableHttpResponse response = httpClient.execute(httpPost);

			String jsonString = EntityUtils.toString(response.getEntity());

			T data = (T) mapper.readValue(jsonString, objectclass);

			return data;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static <T> T formUrlEncoded(String url, Map<String, String> headers, Map<String, String> params,
			Class<T> objectclass, int timeout) {
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();

			URIBuilder builder = new URIBuilder(url);

			HttpPost httpPost = new HttpPost(builder.build());

			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpPost.addHeader(entry.getKey().toString(), entry.getValue().toString());
			}

			List<NameValuePair> formParams = params.entrySet().stream()
					.map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
					.collect(Collectors.toList());

			httpPost.setEntity(new UrlEncodedFormEntity(formParams));

			CloseableHttpResponse response = httpClient.execute(httpPost);

			String jsonString = EntityUtils.toString(response.getEntity());

			ObjectMapper mapper = new ObjectMapper();
			T data = (T) mapper.readValue(jsonString, objectclass);

			return data;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static <T> T formUrlEncodedSpring3(String url, Map<String, String> headers, Map<String, String> params,
			Class<T> objectclass, int timeout) {

// 1. Cấu hình Timeout theo chuẩn HttpClient 5 (Sử dụng đơn vị Milliseconds)
		ConnectionConfig connectionConfig = ConnectionConfig.custom()
		        .setConnectTimeout(Timeout.ofMilliseconds(timeout)) // Thời gian thiết lập kết nối socket
		        .build();
		
		PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
		        .setDefaultConnectionConfig(connectionConfig)
		        .build();

		// 3. Cấu hình Response Timeout thông qua RequestConfig
		RequestConfig requestConfig = RequestConfig.custom()
		        .setResponseTimeout(Timeout.ofMilliseconds(timeout)) // Thời gian chờ server phản hồi dữ liệu (Read Timeout)
		        .build();

// 2. Sử dụng Try-with-resources để tự động đóng HttpClient sau khi chạy xong
		try (CloseableHttpClient httpClient = HttpClients.custom()
		        .setConnectionManager(connectionManager)
		        .setDefaultRequestConfig(requestConfig)
		        .build()) {

			URIBuilder builder = new URIBuilder(url);
			HttpPost httpPost = new HttpPost(builder.build());

// Thêm Headers
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}

// Chuyển đổi Params thành Form Url Encoded
			List<NameValuePair> formParams = params.entrySet().stream()
					.map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
					.collect(Collectors.toList());

// Ép mã hóa UTF_8 để tránh lỗi font tiếng Việt khi truyền param
			httpPost.setEntity(new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));

			BasicHttpClientResponseHandler responseHandler = new BasicHttpClientResponseHandler();

// 3. Sử dụng Response Handler (Lambda) để xử lý kết quả, xóa bỏ lỗi Deprecated
			String jsonString = httpClient.execute(httpPost, responseHandler);

// 4. Parse chuỗi JSON nhận được thành Object Generic T
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(jsonString, objectclass);

		} catch (Exception ex) {
// Trên Production bạn nên dùng log.error(ex.getMessage(), ex) thay vì printStackTrace
			ex.printStackTrace();
			return null;
		}
	}

}
