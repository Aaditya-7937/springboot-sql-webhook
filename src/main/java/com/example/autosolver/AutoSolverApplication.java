/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.example.autosolver;

/**
 *
 * @author aadit
 */
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class AutoSolverApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoSolverApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }

    // ---------------- DTOs ----------------
    public static class GenerateWebhookRequest {
        public String name;
        public String regNo;
        public String email;
        public GenerateWebhookRequest() {}
        public GenerateWebhookRequest(String name, String regNo, String email) {
            this.name = name; this.regNo = regNo; this.email = email;
        }
    }

    public static class GenerateWebhookResponse {
        @JsonProperty("webhook")
        public String webhook;
        @JsonProperty("webhookUrl")
        public String webhookUrl;
        @JsonProperty("accessToken")
        public String accessToken;

        public String resolvedWebhook() {
            if (webhook != null && !webhook.isBlank()) return webhook;
            if (webhookUrl != null && !webhookUrl.isBlank()) return webhookUrl;
            return null;
        }
    }

    public static class SubmitRequest {
        public String finalQuery;
        public SubmitRequest() {}
        public SubmitRequest(String finalQuery) { this.finalQuery = finalQuery; }
    }
}

@Component
class StartupRunner implements ApplicationRunner {
    private final RestTemplate rest;
    private final String generateUrl;
    private final String defaultSubmitUrl;
    private final String name;
    private final String regNo;
    private final String email;

    public StartupRunner(RestTemplate rest,
                         @Value("${api.generate}") String generateUrl,
                         @Value("${api.defaultSubmit}") String defaultSubmitUrl,
                         @Value("${solver.name}") String name,
                         @Value("${solver.regNo}") String regNo,
                         @Value("${solver.email}") String email) {
        this.rest = rest;
        this.generateUrl = generateUrl;
        this.defaultSubmitUrl = defaultSubmitUrl;
        this.name = name;
        this.regNo = regNo;
        this.email = email;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("[AutoSolver] Starting flow...");

        // 1) Generate webhook
        AutoSolverApplication.GenerateWebhookRequest req =
                new AutoSolverApplication.GenerateWebhookRequest(name, regNo, email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AutoSolverApplication.GenerateWebhookRequest> entity =
        new HttpEntity<AutoSolverApplication.GenerateWebhookRequest>(req, headers);


        ResponseEntity<AutoSolverApplication.GenerateWebhookResponse> genResp =
                rest.exchange(generateUrl, HttpMethod.POST, entity, AutoSolverApplication.GenerateWebhookResponse.class);

        AutoSolverApplication.GenerateWebhookResponse body = genResp.getBody();
        if (body == null) throw new IllegalStateException("No response body from generateWebhook");

        String token = body.accessToken;
        String webhookUrl = body.resolvedWebhook();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            System.out.println("[AutoSolver] No webhook returned, defaulting to testWebhook endpoint.");
            webhookUrl = defaultSubmitUrl;
        }

        System.out.println("[AutoSolver] webhookUrl=" + webhookUrl);
        System.out.println("[AutoSolver] accessToken present? " + (token != null && !token.isBlank()));

        // 2) Decide odd/even by last two digits of regNo
        int lastTwo = extractLastTwoDigits(regNo);
        boolean odd = (lastTwo % 2 == 1);
        String chosenSqlPath = odd ? "queries/question1.sql" : "queries/question2.sql";
        System.out.println("[AutoSolver] regNo lastTwo=" + lastTwo + " => " + (odd ? "ODD (Q1)" : "EVEN (Q2)") + ", loading: " + chosenSqlPath);

        // 3) Load SQL from resources
        ClassPathResource cpr = new ClassPathResource(chosenSqlPath);
        String finalQuery = StreamUtils.copyToString(cpr.getInputStream(), StandardCharsets.UTF_8).trim();
        if (finalQuery.isBlank()) throw new IllegalStateException("Final SQL is empty. Put your query in " + chosenSqlPath);

        // 4) Save locally as proof
        File out = new File("target/finalQuery.sql");
        out.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(finalQuery.getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("[AutoSolver] Saved final SQL to " + out.getAbsolutePath());

        // 5) Submit final SQL to webhook with Authorization header = accessToken (as-is)
        HttpHeaders submitHeaders = new HttpHeaders();
        submitHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isBlank()) submitHeaders.set("Authorization", token);
        AutoSolverApplication.SubmitRequest submitPayload = new AutoSolverApplication.SubmitRequest(finalQuery);
        HttpEntity<AutoSolverApplication.SubmitRequest> submitEntity = new HttpEntity<AutoSolverApplication.SubmitRequest>(submitPayload, submitHeaders);


        ResponseEntity<String> submitResp = rest.exchange(webhookUrl, HttpMethod.POST, submitEntity, String.class);
        System.out.println("[AutoSolver] Submission status: " + submitResp.getStatusCode());
        System.out.println("[AutoSolver] Submission response body: " + submitResp.getBody());
    }

    private int extractLastTwoDigits(String regNo) {
        if (regNo == null) return 0;
        String digits = regNo.replaceAll("[^0-9]", "");
        if (digits.length() == 0) return 0;
        if (digits.length() == 1) return Integer.parseInt(digits);
        return Integer.parseInt(digits.substring(digits.length() - 2));
    }
}
