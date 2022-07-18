package com.alexeykovzel.insidr.transaction;

import com.alexeykovzel.insidr.insider.Insider;
import com.alexeykovzel.insidr.insider.InsiderRepository;
import com.alexeykovzel.insidr.utils.DataService;
import com.alexeykovzel.insidr.company.Company;
import com.alexeykovzel.insidr.company.CompanyRepository;
import com.alexeykovzel.insidr.utils.ProgressBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class TransactionDataService extends DataService {
    private static final String FORM_4_URL = "https://www.sec.gov/Archives/edgar/data/%s/%s/form4.xml";
    private static final String SUBMISSIONS_URL = "https://data.sec.gov/submissions/%s";
    private static final String SEC_DATE_FORMAT = "yyyy-MM-dd";

    private final CompanyRepository companyRepository;
    private final FilingRepository filingRepository;
    private final TransactionRepository transactionRepository;
    private final InsiderRepository insiderRepository;

    @Autowired
    public TransactionDataService(CompanyRepository companyRepository,
                                  FilingRepository filingRepository,
                                  TransactionRepository transactionRepository,
                                  InsiderRepository insiderRepository) {
        this.companyRepository = companyRepository;
        this.filingRepository = filingRepository;
        this.transactionRepository = transactionRepository;
        this.insiderRepository = insiderRepository;
    }

    @PostConstruct
    public void init() {
        List<Company> companies = List.of(companyRepository.findById("50863").get()); // for testing
//        List<Company> companies = companyRepository.findAll();
        saveFilings(companies);
        saveTransactions();
    }

    private void saveFilings(List<Company> companies) {
        if (filingRepository.count() != 0) return;
        ProgressBar filingsBar = new ProgressBar("Retrieving filing data...", companies.size());
        for (int i = 0; i < companies.size(); i++) {
            String cik = companies.get(i).getCik();
            List<Filing> filings = getForm4Filings(cik);
            filingRepository.saveAll(filings);
            filingsBar.update(i + 1);
        }
    }

    private void saveTransactions() {
        if (transactionRepository.count() != 0) return;
        List<Filing> filings = filingRepository.findAll();
        ProgressBar transactionsBar = new ProgressBar("Retrieving transaction data...", filings.size());
        for (int i = 0; i < filings.size(); i++) {
            Filing filing = filings.get(i);
            try {
                List<Transaction> transactions = getTransactions(filing.getCompanyCik(), filing.getAccessionNo());
                transactionRepository.saveAll(transactions);
                transactionsBar.update(i + 1);
            } catch (NullPointerException e) {
                System.out.println(filing.getCompanyCik() + " " + filing.getAccessionNo());
                e.printStackTrace();
            }
        }
    }

    public List<Filing> getForm4Filings(String cik) {
        List<Filing> filings = new ArrayList<>();
        try {
            // define nodes for accessing filing data
            String url = String.format(SUBMISSIONS_URL, getFilename(cik));
            JsonNode root = new ObjectMapper().readTree(getDataStream(url));
            JsonNode recent = root.get("filings").get("recent");
            JsonNode accessions = recent.get("accessionNumber");
            JsonNode dates = recent.get("filingDate");
            JsonNode forms = recent.get("form");

            // iterate filing fields for each accession number
            for (int i = 0; i < accessions.size(); i++) {
                // skip non-insider transactions (not of 4-th form)
                if (!forms.get(i).asText().equals("4")) continue;
                // save transaction data
                String id = accessions.get(i).asText();
                Date date = parseDate(dates.get(i).asText());
                filings.add(new Filing(id, cik, date));
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not parse SEC filing: " + e.getMessage());
        }
        return filings;
    }

    public List<Transaction> getTransactions(String companyCik, String filingId) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            // retrieve form 4 from SEC
            String url = String.format(FORM_4_URL, companyCik, filingId.replace("-", ""));
            JsonNode root = new XmlMapper().readTree(getDataStream(url));
            JsonNode error = root.get("Code");
            if (error != null && error.asText().equals("NoSuchKey")) {
                System.out.println("Corrupted filing: CIK=" + companyCik + " ID=" + filingId);
                // TODO: Delete corrupted filing.
                return transactions;
            }

            // define nodes for accessing insider data
            JsonNode owner = root.get("reportingOwner");
            JsonNode ownerId = owner.get("reportingOwnerId");

            // save reporting insider (if doesn't exist)
            String insiderCik = ownerId.get("rptOwnerCik").asText();
            if (!insiderRepository.existsById(insiderCik)) {
                String insiderName = ownerId.get("rptOwnerName").asText();
                String relationship = getRelationshipTitle(owner.get("reportingOwnerRelationship"));
                insiderRepository.save(new Insider(insiderCik, insiderName, companyCik, relationship));
            }

            // save transaction data
            if (root.has("derivativeTable")) {
                JsonNode derivativeTransactions = root.get("derivativeTable").get("derivativeTransaction");
                transactions.addAll(getTransactionsOfJson(filingId, insiderCik, derivativeTransactions));
            }
            if (root.has("nonDerivativeTable")) {
                JsonNode nonDerivativeTransactions = root.get("nonDerivativeTable").get("nonDerivativeTransaction");
                transactions.addAll(getTransactionsOfJson(filingId, insiderCik, nonDerivativeTransactions));
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not read form 4: " + e.getMessage());
        }
        return transactions;
    }

    private String getRelationshipTitle(JsonNode relationship) {
        JsonNode isDirector = relationship.get("isDirector");
        JsonNode isOfficer = relationship.get("isOfficer");
        if (isDirector != null && isDirector.asInt() == 1) return "Director";
        if (isOfficer != null && isOfficer.asInt() == 1) {
            return relationship.get("officerTitle").asText();
        }
        System.out.println("[ERROR] Unknown relationship: " + relationship);
        return null;
    }

    private List<Transaction> getTransactionsOfJson(String filingId, String insiderCik, JsonNode obj) {
        List<Transaction> transactions = new ArrayList<>();
        if (obj.isArray()) {
            for (JsonNode o : obj) {
                transactions.add(getTransactionOfJson(filingId, insiderCik, o));
            }
        } else {
            transactions.add(getTransactionOfJson(filingId, insiderCik, obj));
        }
        return transactions;
    }

    private Transaction getTransactionOfJson(String filingId, String insiderCik, JsonNode obj) {
        JsonNode ownership = obj.get("ownershipNature").get("directOrIndirectOwnership");
        JsonNode amounts = obj.get("transactionAmounts");
        JsonNode postAmounts = obj.get("postTransactionAmounts");
        JsonNode sharePrice = amounts.get("transactionPricePerShare").get("value");
        JsonNode coding = obj.get("transactionCoding");

        return Transaction.builder()
                .filingId(filingId)
                .insiderCik(insiderCik)
                .securityTitle(obj.get("securityTitle").get("value").asText())
                .code(coding.get("transactionCode").asText())
                .date(parseDate(obj.get("transactionDate").get("value").asText()))
                .isDirect(ownership.get("value").asText().equals("D"))
                .shareCount(amounts.get("transactionShares").get("value").asDouble())
                .sharePrice((sharePrice != null) ? sharePrice.asDouble() : null)
                .leftShares(postAmounts.get("sharesOwnedFollowingTransaction").get("value").asDouble())
                .build();
    }

    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat(SEC_DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            System.out.println("[ERROR] Could not parse date: " + e.getMessage());
            return null;
        }
    }

    private String getFilename(String cik) {
        return "CIK" + "0".repeat(10 - cik.length()) + cik + ".json";
    }
}
