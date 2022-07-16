package com.alexeykovzel.insidr.filing;

import com.alexeykovzel.insidr.DataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FilingDataService extends DataService {
    private static final String FORM_4_URL = "https://www.sec.gov/Archives/edgar/data/%s/%s/form4.xml";
    private static final String SUBMISSIONS_URL = "https://data.sec.gov/submissions/%s";
    private static final String SEC_DATE_FORMAT = "yyyy-MM-dd";

    public Form4Data getForm4Data(String cik, String id) {
        String url = String.format(FORM_4_URL, cik, id.replace("-", ""));
        try {
//            XmlMapper mapper = new XmlMapper();
            new ObjectMapper().readTree(getDataStream(url));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<SECFiling> getFilings(String cik) {
        List<SECFiling> filings = new ArrayList<>();
        try {
            DateFormat dateFormat = new SimpleDateFormat(SEC_DATE_FORMAT);
            String url = String.format(SUBMISSIONS_URL, getFilename(cik));

            // define nodes for accessing filing data
            JsonNode root = new ObjectMapper().readTree(getDataStream(url));
            JsonNode recent = root.get("filings").get("recent");
            JsonNode accessionNode = recent.get("accessionNumber");
            JsonNode filingDateNode = recent.get("filingDate");
            JsonNode reportDateNode = recent.get("reportDate");

            // iterate filing fields for each accession number
            for (int i = 0; i < accessionNode.size(); i++) {
                String id = accessionNode.get(i).asText();
                Date filingDate = dateFormat.parse(filingDateNode.get(i).asText());
                Date reportDate = dateFormat.parse(reportDateNode.get(i).asText());
                filings.add(new SECFiling(id, filingDate, reportDate));
            }
        } catch (IOException | ParseException e) {
            System.out.println("[ERROR] Could not parse SEC filing");
        }
        return filings;
    }

    private String getFilename(String cik) {
        return "CIK" + "0".repeat(10 - cik.length()) + cik + ".json";
    }
}
