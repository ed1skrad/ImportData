package com.notes.scheduled.config;

import com.notes.scheduled.model.old.Client;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class OldSystemClient {

    private final RestTemplate restTemplate;
    private final String baseUrl = "http://localhost:8080";

    public OldSystemClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Client> getClients() {
        Client[] clients = restTemplate.getForObject(baseUrl + "/clients", Client[].class);
        return clients != null ? Arrays.asList(clients) : List.of();
    }
}
