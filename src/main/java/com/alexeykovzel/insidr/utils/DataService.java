package com.alexeykovzel.insidr.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

public abstract class DataService {

    protected static InputStream getDataStream(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.of(2, ChronoUnit.SECONDS))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
        } catch (IOException | InterruptedException e) {
            System.out.println("[ERROR] Failed to access data: " + e.getMessage());
            return null;
        }
    }

    protected void handleNode(JsonNode node, Consumer<JsonNode> action) {
        if (!node.isArray()) {
            action.accept(node);
            return;
        }
        for (JsonNode obj : node) {
            action.accept(obj);
        }
    }
}
