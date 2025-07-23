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
import static org.junit.jupiter.api.Assertions.*;

class GetDomainRecordsTest {
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
        wireMockServer.stubFor(post("/dnsZone/records")
                .willReturn(WireMock.okJson(IOUtils.resourceToString("/ihc-domain.json", StandardCharsets.UTF_8))));

        var ihc = new IhcClient("http://localhost:%d".formatted(port));
        ihc.auth("user1", "passwd1");
        int domainId = ihc.getDomains().getFirst().id();
        List<DomainRecord> domainRecords = ihc.getDomainRecords(domainId);

        assertEquals(7, domainRecords.size());

        DomainRecord record1 = domainRecords.getFirst();
        assertEquals(7000001, record1.getId());
        assertTrue(record1.isReadOnly());
        assertEquals(RecordType.SOA, record1.getType());
        assertEquals("", record1.getName());
        assertEquals("ns1.ihc.ru. info.ihc.ru. 2014120801 10800 3600 604800 3600", record1.getContent());
        assertNull(record1.getPriority());

        assertThrows(RuntimeException.class, () -> record1.setName("xx"));

        DomainRecord record2 = domainRecords.get(4);
        assertEquals(7000005, record2.getId());
        assertFalse(record2.isReadOnly());
        assertEquals(RecordType.MX, record2.getType());
        assertEquals("", record2.getName());
        assertEquals("mx.yandex.ru", record2.getContent());
        assertEquals(10, record2.getPriority());

        assertDoesNotThrow(() -> record2.setName("xx"));
    }
}
