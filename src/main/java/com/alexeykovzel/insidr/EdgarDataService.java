package com.alexeykovzel.insidr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class EdgarDataService {
    protected static final String COMPANY_TICKERS_URL = "https://www.sec.gov/files/company_tickers_exchange.json";
    protected static final String ARCHIVES_DATA_URL = "https://www.sec.gov/Archives/edgar/data/%s/%s/%s";
    protected static final String SUBMISSIONS_URL = "https://data.sec.gov/submissions/%s";
    protected static final String FORM_4_DATE_FORMAT = "yyyy-MM-dd";

    private static final int RATE_LIMIT_SLEEP_INTERVAL = 100;
    private static final int HTTP_REQUEST_TIMEOUT = 5000;

    protected JsonNode getJsonByUrl(String url) throws IOException {
        HttpResponse<InputStream> response = sendUrlRequest(url);
        return (response != null) ? new ObjectMapper().readTree(response.body()) : null;
    }

    protected JsonNode getXmlByUrl(String url) throws IOException {
        HttpResponse<InputStream> response = sendUrlRequest(url);
        return (response != null) ? new XmlMapper().readTree(response.body()) : null;
    }

    protected String addLeadingZeros(String value, int count) {
        return "0".repeat(count - value.length()) + value;
    }

    protected boolean hasOne(JsonNode node) {
        return (node != null) && (node.asInt() == 1);
    }

    protected void handleNode(JsonNode node, Consumer<JsonNode> action) {
        if (node.isArray()) {
            for (JsonNode obj : node) {
                action.accept(obj);
            }
        } else {
            action.accept(node);
        }
    }

    private HttpResponse<InputStream> sendUrlRequest(String url) {
        try {
            TimeUnit.MILLISECONDS.sleep(RATE_LIMIT_SLEEP_INTERVAL);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.of(HTTP_REQUEST_TIMEOUT, ChronoUnit.MILLIS))
                    .header("User-Agent", "Insidr alexey.kovzel@gmail.com")
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 429) {
                System.out.println("[ERROR] You have hit the rate limit of SEC");
            }
            return response;
        } catch (IOException | InterruptedException e) {
            System.out.println("[ERROR] " + e.getMessage() + ": URL=" + url);
            return null;
        }
    }
}
