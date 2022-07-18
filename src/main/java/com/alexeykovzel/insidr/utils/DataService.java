package com.alexeykovzel.insidr.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class DataService {

    public static InputStream getDataStream(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
            return client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
        } catch (IOException | InterruptedException e) {
            System.out.println("[ERROR] Failed to access data: " + e.getMessage());
            return null;
        }
    }
}
