package ru.di9.ihc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class IhcClient {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public IhcClient(String baseUrl) {
        this.baseUrl = baseUrl;

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .cookieHandler(new CookieManager())
                .build();

        this.mapper = new ObjectMapper();
    }

    public boolean auth(String login, String password) {
        var request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(
                        "j_username=%s&j_password=%s&recaptcha=&ihccaptcha=".formatted(login, password)
                ))
                .uri(URI.create("%s/j_spring_security_check?ajax=true".formatted(baseUrl)))
                .setHeader("Accept", "application/json")
                .setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .setHeader("Referer", "%s/login/auth".formatted(baseUrl))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() != 200) {
            return false;
        }

        AuthResponse authResponse;
        try {
            String json = response.body();
            authResponse = mapper.readValue(json, AuthResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return "none".equalsIgnoreCase(authResponse.alert().type());
    }

    public void getDomainList() {
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("%s/dnsZone/list".formatted(baseUrl)))
                .setHeader("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("not 200");
        }

        String body = response.body();
        System.out.println(body);
    }
}
