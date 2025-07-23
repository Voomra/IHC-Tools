package ru.di9.ihc;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GetDomainsTest {
    static int port;
    static WireMockServer wireMockServer;

    @BeforeAll
    static void beforeAll() {
        port = RandomUtils.nextInt(9000, 9999);
        wireMockServer = new WireMockServer(port);
        wireMockServer.start();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @AfterEach
    void after() {
        wireMockServer.resetAll();
    }

    @Test
    void test() throws IOException {
        wireMockServer.stubFor(post("/j_spring_security_check?ajax=true")
                .willReturn(WireMock.okJson("""
                        {"redirect":{"url":"/"},"alert":{"type":"none","message":""}}""")));
        wireMockServer.stubFor(get("/dnsZone/list")
                .willReturn(WireMock.ok(IOUtils.resourceToString("/ihc-dns.html", StandardCharsets.UTF_8))
                        .withHeader("Content-Type", "text/html")));

        var ihc = new IhcClient("http://localhost:%d".formatted(port));
        ihc.auth("user1", "passwd1");
        List<Domain> domains = ihc.getDomains();

        assertEquals(3, domains.size());
        assertEquals(new Domain(111111, "example-1.ru", null), domains.get(0));
        assertEquals(new Domain(222222, "example-2.ru", null), domains.get(1));
        assertEquals(new Domain(333333, "пример-3.рф", "xn---3-mlcluqhd.xn--p1ai"), domains.get(2));
    }
}
