package com.alexeykovzel.insidr.company;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CompanyDataService {
    private final static String URL = "https://www.sec.gov/Archives/edgar/cik-lookup-data.txt";
    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyDataService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @PostConstruct
    public void init() {
        if (companyRepository.count() == 0) {
            System.out.println("Retrieving company data...");
            double t1 = System.currentTimeMillis();
            companyRepository.saveAll(getAllCompanies());
            double t2 = System.currentTimeMillis();
            System.out.printf("Time spent: %.2f seconds\n", (t2 - t1) / 1000);
        }
    }

    public List<Company> getAllCompanies() {
        List<Company> companies = new ArrayList<>();
        Map<String, List<String>> indexes = new HashMap<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(getIndexStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                int cikIdx = line.indexOf(":");
                String index = line.substring(cikIdx + 1, line.length() - 1);
                String company = line.substring(0, cikIdx);
                if (!indexes.containsKey(index)) indexes.put(index, new ArrayList<>());
                indexes.get(index).add(company);
            }
            for (Map.Entry<String, List<String>> index : indexes.entrySet()) {
                companies.add(new Company(index.getKey(), index.getValue()));
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to read indexes");
        }
        return companies;
    }

    public void saveIndexesToFile(String filepath) {
        try (OutputStream out = new FileOutputStream(filepath)) {
            IOUtils.copy(getIndexStream(), out);
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to save CIK data to file");
        }
    }

    public InputStream getIndexStream() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).build();
            return client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
        } catch (IOException | InterruptedException e) {
            System.out.println("[ERROR] Failed to access CIK data");
            return null;
        }
    }
}
