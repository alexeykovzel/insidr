package com.alexeykovzel.insidr.stock;

import com.alexeykovzel.insidr.utils.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@PropertySource("classpath:api.properties")
public class AlphaVantageAPI {
    private static final String HOST_URL = "https://alpha-vantage.p.rapidapi.com";
    private static final String FUNCTION_URL = HOST_URL + "/query?function=%s&symbol=%s&datatype=%s";

    @Value("${rapid.api.key}")
    private String rapidApiKey;

    @Value("${rapid.api.host}")
    private String rapidApiHost;

    public List<StockPrice> getStockPrices(String symbol) {
        List<StockPrice> stockPrices = new ArrayList<>();

        // retrieve time series for a given symbol using API
        JsonNode timeSeries = getTimeSeriesBySymbol(symbol);
        if (timeSeries == null) return stockPrices;

        // save the stock price and dividends for each date
        Iterator<String> dates = timeSeries.fieldNames();
        while (dates.hasNext()) {
            String dateValue = dates.next();
            JsonNode stats = timeSeries.get(dateValue);
            Date date = new DateUtils().parse(dateValue, "yyyy-dd-MM");
            double price = stats.get("5. adjusted close").asDouble();
            double dividends = stats.get("7. dividend amount").asDouble();
            stockPrices.add(new StockPrice(symbol, date, price, dividends));
        }
        return stockPrices;
    }

    private JsonNode getTimeSeriesBySymbol(String symbol) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(FUNCTION_URL, "TIME_SERIES_WEEKLY_ADJUSTED", symbol, "json")))
                .header("X-RapidAPI-Key", rapidApiKey)
                .header("X-RapidAPI-Host", rapidApiHost)
                .build();
        try {
            InputStream in = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
            return new ObjectMapper().readTree(in).get("Weekly Adjusted Time Series");
        } catch (IOException | InterruptedException e) {
            System.out.println("[ERROR] Could not access stock price: " + e.getMessage());
            return null;
        }
    }
}
