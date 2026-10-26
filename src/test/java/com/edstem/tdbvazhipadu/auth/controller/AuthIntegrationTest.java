package com.edstem.tdbvazhipadu.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.tdbvazhipadu.support.TestStaffAccount;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class AuthIntegrationTest {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final String OFFERING_JSON = "{\"type\":\"MALA\",\"price\":20.00,\"active\":true}";

    private final MockMvc mockMvc;

    @Test
    void staffTokenFromLoginOpensOrderSearchAndOfferingManagement() throws Exception {
        String token = login(TestStaffAccount.USERNAME, TestStaffAccount.PASSWORD);

        mockMvc.perform(get("/api/orders").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/offerings")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OFFERING_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(TestStaffAccount.USERNAME, "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }

    @Test
    void orderSearchWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", PROBLEM_JSON));
    }

    @Test
    void orderSearchWithInvalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", PROBLEM_JSON));
    }

    @Test
    void managingOfferingsWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/offerings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OFFERING_JSON))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/offerings/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OFFERING_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void viewingOfferingsStaysPublic() throws Exception {
        mockMvc.perform(get("/api/offerings")).andExpect(status().isOk());
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    private static String loginJson(String username, String password) {
        return "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password);
    }
}
