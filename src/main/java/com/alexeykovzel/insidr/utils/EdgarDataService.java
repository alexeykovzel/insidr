package com.alexeykovzel.insidr.utils;

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

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final int RATE_LIMIT_SLEEP_INTERVAL = 100;
    private static final int HTTP_REQUEST_TIMEOUT = 5000;

    protected JsonNode getJsonByUrl(String url) throws IOException {
        InputStream in = sendUrlRequest(url).body();
        return (in != null) ? new ObjectMapper().readTree(in) : null;
    }

    protected JsonNode getXmlByUrl(String url) throws IOException {
        InputStream in = sendUrlRequest(url).body();
        return (in != null) ? new XmlMapper().readTree(in) : null;
    }

    protected HttpResponse<InputStream> sendUrlRequest(String url) {
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

    protected Date parseDate(String date) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            System.out.println("[ERROR] Could not parse date: " + e.getMessage());
            return null;
        }
    }

    /**
     * Applies an action on the node or its children depending on whether it is an array
     * or a single value, respectively.
     *
     * @param node   node on which an action should be applied
     * @param action action that consumes a json node as one of its parameters
     */
    protected void handleNode(JsonNode node, Consumer<JsonNode> action) {
        if (node.isArray()) {
            for (JsonNode obj : node) {
                action.accept(obj);
            }
        } else {
            action.accept(node);
        }
    }

    /**
     * Adds leading zeros in front of a given value. The zero count is calculated so that the final
     * value contains 'count' number of symbols.
     *
     * @param value initial value
     * @param count resulting number of symbols
     * @return value with leading zeros in front
     */
    protected String addLeadingZeros(String value, int count) {
        return "0".repeat(count - value.length()) + value;
    }

    /**
     * Removes leading zeros of a given value.
     * e.g. 0000050863 to 50863
     *
     * @param value initial value
     * @return value without leading zeros
     */
    protected String removeLeadingZeros(String value) {
        return value.replaceFirst("^0+(?!$)", "");
    }

    /**
     * Checks whether a node contains '1'. In this case, a value is considered 'true'.
     *
     * @param node json node that is being checked
     * @return true if the node contains '1'
     */
    protected boolean hasOne(JsonNode node) {
        return (node != null) && node.canConvertToInt() && (node.asInt() == 1);
    }
}
