package ru.di9.ihc;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateDomainRecordTest {
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

    @BeforeEach
    void before() throws IOException {
        wireMockServer.stubFor(post("/j_spring_security_check?ajax=true")
                .willReturn(WireMock.okJson("""
                        {"redirect":{"url":"/"},"alert":{"type":"none","message":""}}""")));
        wireMockServer.stubFor(get("/dnsZone/list")
                .willReturn(WireMock.ok(IOUtils.resourceToString("/ihc-dns.html", StandardCharsets.UTF_8))
                        .withHeader("Content-Type", "text/html")));
        wireMockServer.stubFor(post("/dnsZone/records")
                .willReturn(WireMock.okJson(IOUtils.resourceToString("/ihc-domain.json", StandardCharsets.UTF_8))));
    }

    @AfterEach
    void after() {
        wireMockServer.resetAll();
    }

    @Test
    void test() {
        wireMockServer.stubFor(post("/dnsZone/updateRecord")
                .willReturn(WireMock.okJson("""
                        {
                          "alert": {
                            "type": "success",
                            "message": "<span class='alert__msg'>Операция выполнена успешно</span>"
                          },
                          "data": {
                            "success": true
                          }
                        }""")));

        var client = new IhcClient("http://localhost:%d".formatted(port));
        boolean auth = client.auth("user1", "passwd1");
        assertTrue(auth);

        List<Domain> domains = client.getDomains();
        Optional<Domain> optDomain = domains.stream().filter(it -> it.name().equals("example-2.ru")).findFirst();
        assertTrue(optDomain.isPresent());

        List<DomainRecord> records = client.getDomainRecords(optDomain.get());
        Optional<DomainRecord> optRecord = records.stream().filter(it -> it.getName().equals("_acme-challenge")).findFirst();
        assertTrue(optRecord.isPresent());

        optRecord.get().setContent("SOME-TEST-01");
        assertDoesNotThrow(() -> client.updateDomainRecord(optDomain.get(), optRecord.get()));
    }
}
