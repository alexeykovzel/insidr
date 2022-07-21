package com.alexeykovzel.insidr.stock;

import com.alexeykovzel.insidr.company.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class StockMarketService {
    private final CompanyRepository companyRepository;
    private final StockPriceRepository stockPriceRepository;
    private final AlphaVantageAPI alphaVantageAPI;

    @Autowired
    public StockMarketService(CompanyRepository companyRepository,
                              StockPriceRepository stockPriceRepository,
                              AlphaVantageAPI alphaVantageAPI) {
        this.companyRepository = companyRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.alphaVantageAPI = alphaVantageAPI;
    }

    @PostConstruct
    public void init() {
        saveAllStockPrices();
    }

    private void saveAllStockPrices() {
        List<String> symbols = companyRepository.findSymbolsWithNoStockPrices();
        for (String symbol : symbols) {
            List<StockPrice> stockPrices = alphaVantageAPI.getStockPrices(symbol);
            stockPriceRepository.saveAll(stockPrices);
        }
    }
}
