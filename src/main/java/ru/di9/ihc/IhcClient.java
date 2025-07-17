package ru.di9.ihc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class IhcClient {
    private final String baseUrl;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper mapper;

    public IhcClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClientBuilder.create().build();
        this.mapper = new ObjectMapper();
    }

    public boolean auth(String login, String password) {
        HttpPost httpPost = new HttpPost(URI.create("%s/j_spring_security_check?ajax=true".formatted(baseUrl)));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "%s/login/auth".formatted(baseUrl));
        httpPost.setEntity(new UrlEncodedFormEntity(List.of(
                new BasicNameValuePair("j_username", login),
                new BasicNameValuePair("j_password", password),
                new BasicNameValuePair("recaptcha", ""),
                new BasicNameValuePair("ihccaptcha", "")
        )));

        boolean authResult;
        try {
            authResult = httpClient.execute(httpPost, resp -> {
                if (resp.getCode() != 200) {
                    return false;
                }

                AuthResponse authResponse = mapper.readValue(resp.getEntity().getContent(), AuthResponse.class);
                return "none".equalsIgnoreCase(authResponse.alert().type());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return authResult;
    }
}
