package ru.di9.ihc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IhcClient {
    private final String baseUrl;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper mapper;

    private boolean isAuth = false;

    public IhcClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClientBuilder.create().build();
        this.mapper = new ObjectMapper();
    }

    public boolean auth(String login, String password) {
        var httpPost = new HttpPost(URI.create("%s/j_spring_security_check?ajax=true".formatted(baseUrl)));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "%s/login/auth".formatted(baseUrl));
        httpPost.setEntity(new UrlEncodedFormEntity(List.of(
                new BasicNameValuePair("j_username", login),
                new BasicNameValuePair("j_password", password),
                new BasicNameValuePair("recaptcha", ""),
                new BasicNameValuePair("ihccaptcha", "")
        )));

        try {
            isAuth = httpClient.execute(httpPost, resp -> {
                if (resp.getCode() != 200) {
                    return false;
                }

                AuthResponse authResponse = mapper.readValue(resp.getEntity().getContent(), AuthResponse.class);
                return "none".equalsIgnoreCase(authResponse.alert().type());
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return isAuth;
    }

    public List<Domain> getDomains() {
        if (!isAuth) {
            throw new RuntimeException("IS NOT AUTH");
        }

        HttpGet httpGet = new HttpGet(URI.create("%s/dnsZone/list".formatted(baseUrl)));
        httpGet.setHeader("Accept", "text/html");

        try {
            return httpClient.execute(httpGet, resp -> {
                if (resp.getCode() != 200) {
                    return Collections.emptyList();
                }

                List<Domain> list = new ArrayList<>();
                Document document = Jsoup.parse(resp.getEntity().getContent(), StandardCharsets.UTF_8.name(), baseUrl);
                Elements elements = document.select("li[class='zoneList__zone'] a");
                for (Element element : elements) {
                    int id = Integer.parseInt(element.attr("href").substring(1).split("/")[2]);
                    String name = element.text();
                    String punycode = null;
                    if (name.contains(" (")) {
                        String[] split = name.split(" \\(", 2);
                        name = split[0];
                        punycode = split[1].substring(0, split[1].length() - 1);
                    }
                    list.add(new Domain(id, name, punycode));
                }

                return list;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<DomainRecord> getDomainRecords(Domain domain) {
        if (!isAuth) {
            throw new RuntimeException("IS NOT AUTH");
        }

        HttpPost httpPost = new HttpPost(URI.create("%s/dnsZone/records".formatted(baseUrl)));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "%s/dnsZone/index/%d".formatted(baseUrl, domain.id()));
        httpPost.setEntity(new UrlEncodedFormEntity(List.of(
                new BasicNameValuePair("id", String.valueOf(domain.id()))
        )));

        try {
            return httpClient.execute(httpPost, resp -> {
                if (resp.getCode() != 200) {
                    return Collections.emptyList();
                }

                DomainRecordsResponse response = mapper.readValue(resp.getEntity().getContent(), DomainRecordsResponse.class);
                List<DomainRecord> list = new ArrayList<>();

                for (DomainRecordsResponse.Record rec : response.data().records()) {
                    list.add(new DomainRecord(
                            rec.id(),
                            rec.readOnly(),
                            rec.name(),
                            RecordType.valueOf(rec.type()),
                            rec.content(),
                            rec.prio()
                    ));
                }

                list.sort(Comparator.comparingInt(DomainRecord::getId));
                return list;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateDomainRecord(Domain domain, DomainRecord domainRecord) {
        if (!isAuth) {
            throw new RuntimeException("IS NOT AUTH");
        }

        var httpPost = new HttpPost(URI.create("%s/dnsZone/updateRecord".formatted(baseUrl)));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader("Referer", "%s/dnsZone/index/%d".formatted(baseUrl, domain.id()));
        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
        httpPost.setEntity(new UrlEncodedFormEntity(List.of(
                new BasicNameValuePair("name", domainRecord.getName()),
                new BasicNameValuePair("content", domainRecord.getContent()),
                new BasicNameValuePair("id", String.valueOf(domain.id())),
                new BasicNameValuePair("recordId", String.valueOf(domainRecord.getId())),
                new BasicNameValuePair("type", domainRecord.getType().name())
        )));

        try {
            httpClient.execute(httpPost, resp -> {
                if (resp.getCode() != 200) {
                    throw new RuntimeException(resp.toString());
                }
                return null;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
