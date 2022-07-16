package com.alexeykovzel.insidr.company;

import com.alexeykovzel.insidr.DataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CompanyDataService extends DataService {
    private final static String COMPANY_TICKERS_URL = "https://www.sec.gov/files/company_tickers_exchange.json";
    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyDataService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @PostConstruct
    public void init() {
        if (companyRepository.count() == 0) {
            System.out.print("Retrieving company data... ");
            double t1 = System.currentTimeMillis();
            companyRepository.saveAll(getAllCompanies());
            double t2 = System.currentTimeMillis();
            System.out.printf("Time spent: %.2f seconds\n", (t2 - t1) / 1000);
        }
    }

    public List<Company> getAllCompanies() {
        List<Company> companies = new ArrayList<>();
        try {
            JsonNode root = new ObjectMapper().readTree(getDataStream(COMPANY_TICKERS_URL));
            JsonNode items = root.get("data");
            for (JsonNode item : items) {
                String cik = item.get(0).asText();
                String title = item.get(1).asText();
                String symbol = item.get(2).asText();
                String exchange = item.get(3).asText();
                companies.add(new Company(cik, title, symbol, null, exchange));
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not access company data");
        }
        return companies;
    }
}
