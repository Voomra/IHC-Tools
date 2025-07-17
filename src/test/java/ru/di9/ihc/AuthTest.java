package ru.di9.ihc;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthTest {
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
    void correctAuthTest() {
        wireMockServer.stubFor(post("/j_spring_security_check?ajax=true")
                .willReturn(WireMock.okJson("""
                        {"redirect":{"url":"/"},"alert":{"type":"none","message":""}}""")));

        var ihc = new IhcClient("http://localhost:%d".formatted(port));
        boolean result = ihc.auth("user1", "passwd1");

        assertTrue(result);
    }

    @Test
    void incorrectAuthTest() {
        wireMockServer.stubFor(post("/j_spring_security_check?ajax=true")
                .willReturn(WireMock.status(302)));

        var ihc = new IhcClient("http://localhost:%d".formatted(port));
        boolean result = ihc.auth("user1", "wrong_passwd");

        assertFalse(result);
    }
}
