package com.edstem.tdbvazhipadu.order.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.edstem.tdbvazhipadu.support.TestStaffAccount;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DailyLimitConcurrencyTest {

    private static final int DAILY_LIMIT = 5;
    private static final int SIMULTANEOUS_ORDERS = 20;

    @LocalServerPort
    private int port;

    @Test
    void twentySimultaneousOrdersForOneAgainstALimitOfFiveLetExactlyFiveThrough() throws Exception {
        RestClient client = RestClient.create("http://localhost:" + port);
        long offeringId = createLimitedOffering(client, login(client));
        LocalDate date = LocalDate.now().plusDays(90);
        String orderJson =
                "{\"devoteeName\":\"Lakshmi\",\"phoneNumber\":\"9876543210\",\"nakshatra\":\"ROHINI\",\"offeringDate\":\"%s\",\"items\":[{\"offeringId\":%d,\"quantity\":1}]}"
                        .formatted(date, offeringId);

        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> statuses = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(SIMULTANEOUS_ORDERS);
        try {
            for (int i = 0; i < SIMULTANEOUS_ORDERS; i++) {
                statuses.add(executor.submit(() -> {
                    start.await();
                    return client.post()
                            .uri("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(orderJson)
                            .exchange((request, response) ->
                                    response.getStatusCode().value());
                }));
            }
            start.countDown();
            List<Integer> results = new ArrayList<>();
            for (Future<Integer> status : statuses) {
                results.add(status.get(30, TimeUnit.SECONDS));
            }
            assertThat(results).filteredOn(status -> status == 201).hasSize(DAILY_LIMIT);
            assertThat(results).filteredOn(status -> status == 422).hasSize(SIMULTANEOUS_ORDERS - DAILY_LIMIT);
        } finally {
            executor.shutdownNow();
        }
    }

    private static String login(RestClient client) {
        String response = client.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"username\":\"%s\",\"password\":\"%s\"}"
                        .formatted(TestStaffAccount.USERNAME, TestStaffAccount.PASSWORD))
                .retrieve()
                .body(String.class);
        return JsonPath.read(response, "$.accessToken");
    }

    private static long createLimitedOffering(RestClient client, String token) {
        String response = client.post()
                .uri("/api/offerings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"type\":\"UDAYASTHAMANA_POOJA\",\"price\":500.00,\"active\":true,\"dailyLimit\":%d}"
                        .formatted(DAILY_LIMIT))
                .retrieve()
                .body(String.class);
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }
}
