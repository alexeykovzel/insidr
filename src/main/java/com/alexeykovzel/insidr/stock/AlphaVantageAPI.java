package com.alexeykovzel.insidr.stock;

import com.alexeykovzel.insidr.utils.DateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AlphaVantageAPI {
    private static final String HOST_URL = "https://alpha-vantage.p.rapidapi.com";
    private static final String STOCK_PRICES_URL = HOST_URL + "/query?function=%s&symbol=%s&datatype=json";
    private static final String RAPID_API_KEY = "3ceb078ca1mshc8e01fecbe71ffcp1a7cd1jsn0f938e080971";
    private static final String RAPID_API_HOST = "alpha-vantage.p.rapidapi.com";

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
                .uri(URI.create(String.format(STOCK_PRICES_URL, "TIME_SERIES_WEEKLY_ADJUSTED", symbol)))
                .header("X-RapidAPI-Key", RAPID_API_KEY)
                .header("X-RapidAPI-Host", RAPID_API_HOST)
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
