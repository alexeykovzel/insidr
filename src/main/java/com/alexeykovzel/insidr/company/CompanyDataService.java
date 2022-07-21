package com.alexeykovzel.insidr.company;

import com.alexeykovzel.insidr.utils.EdgarDataService;
import com.alexeykovzel.insidr.utils.ProgressBar;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class CompanyDataService extends EdgarDataService {
    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyDataService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @PostConstruct
    public void init() {
        ProgressBar bar = new ProgressBar("Initialing company data...", 1);
        if (companyRepository.count() == 0) {
            companyRepository.saveAll(getAllCompanies());
        }
        bar.update(1);
    }

    public List<Company> getAllCompanies() {
        List<Company> companies = new ArrayList<>();
        try {
            JsonNode items = getJsonByUrl(COMPANY_TICKERS_URL).get("data");
            for (JsonNode item : items) {
                String cik = addLeadingZeros(item.get(0).asText(), 10);
                String title = item.get(1).asText();
                String symbol = item.get(2).asText();
                String exchange = item.get(3).asText();
                companies.add(new Company(cik, title, symbol, exchange));
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("[ERROR] Could not access company data: " + e.getMessage());
        }
        return companies;
    }
}
