package com.alexeykovzel.insidr.cik;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Component;

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
public class CIKDataService {
    private final static String URL = "https://www.sec.gov/Archives/edgar/cik-lookup-data.txt";
    private final static String FILEPATH = "src/main/resources/cik-data.txt";

    public List<CentralIndexKey> getIndexes(int from, int to) {
        // TODO: Handle invalid input.
        if (to <= from) return null;
        if (from < 0) return null;

        List<CentralIndexKey> indexes =  new ArrayList<>();
        HashMap<String, List<String>> indexMap = new HashMap<>();
        try (BufferedReader out = new BufferedReader(new FileReader(FILEPATH))) {
            for (int i = 0; i < to; i++) {
                String data = out.readLine();
                if (data == null) break;
                if (i < from) continue;

                // Retrieve CIK and company name
                int cikIdx = data.indexOf(":");
                String cik = data.substring(cikIdx + 1, data.length() - 1);
                String company = data.substring(0, cikIdx);

                // Add company name to CIK
                if (!indexMap.containsKey(cik)) indexMap.put(cik, new ArrayList<>());
                indexMap.get(cik).add(company);
            }
            for (Map.Entry<String, List<String>> entries : indexMap.entrySet()) {
                indexes.add(new CentralIndexKey(entries.getKey(), entries.getValue()));
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to read file");
        }
        return indexes;
    }

    public List<CentralIndexKey> getAllIndexes() {
        return getIndexes(0, Integer.MAX_VALUE);
    }

    public void saveIndexesToFile(String filepath) {
        try {
            // Send GET request to retrieve CIK data
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            // Copy response data to the specified file
            try (InputStream data = response.body(); OutputStream out = new FileOutputStream(filepath)) {
                IOUtils.copy(data, out);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("[ERROR] Failed to access CIK data");
        }
    }
}
