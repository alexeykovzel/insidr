package com.alexeykovzel.insidr;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class CIKDataService {
    private final static String CIK_URL = "https://www.sec.gov/Archives/edgar/cik-lookup-data.txt";
    private final static String CIK_PATH = "src/test/resources/cik.txt";
    private final static HttpClient CLIENT = HttpClient.newHttpClient();

    public HashMap<String, List<String>> getIndexes(int from, int to) {
        HashMap<String, List<String>> indexes = new HashMap<>();
        try (BufferedReader out = new BufferedReader(new FileReader(CIK_PATH))) {
            for (int i = 0; i < to; i++) {
                String data = out.readLine();
                if (i < from) continue;

                // Retrieve CIK and company name
                int cikIdx = data.indexOf(":");
                String cik = data.substring(cikIdx + 1, data.length() - 1);
                String company = data.substring(0, cikIdx);

                // Add company name to CIK
                if (!indexes.containsKey(cik)) indexes.put(cik, new ArrayList<>());
                indexes.get(cik).add(company);
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to read file");
        }
        return indexes;
    }

    public void saveIndexesToFile() {
        try {
            // Send GET request to retrieve CIK data
            HttpRequest request = HttpRequest.newBuilder(URI.create(CIK_URL)).build();
            HttpResponse<InputStream> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            // Copy response data to the specified file
            try (InputStream data = response.body(); OutputStream out = new FileOutputStream(CIK_PATH)) {
                IOUtils.copy(data, out);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("[ERROR] Failed to access CIK data");
        }
    }
}
