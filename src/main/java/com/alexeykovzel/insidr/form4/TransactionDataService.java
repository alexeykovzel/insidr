package com.alexeykovzel.insidr.form4;

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
        saveAllFilings(companies);
        saveAllTransactions();
    }

    private void saveAllFilings(List<Company> companies) {
        if (filingRepository.count() != 0) return;
        ProgressBar bar = new ProgressBar("Retrieving filing data...", companies.size());
        for (int i = 0; i < companies.size(); i++) {
            String cik = companies.get(i).getCik();
            filingRepository.saveAll(getForm4Filings(cik));
            bar.update(i + 1);
        }
    }

    public List<Filing> getForm4Filings(String cik) {
        List<Filing> filings = new ArrayList<>();
        try {
            // define nodes for accessing filing data
            String url = String.format(SUBMISSIONS_URL, getCikFilename(cik));
            JsonNode root = new ObjectMapper().readTree(getDataStream(url));
            JsonNode recent = root.get("filings").get("recent");
            JsonNode accessions = recent.get("accessionNumber");
            JsonNode documents = recent.get("primaryDocument");
            JsonNode dates = recent.get("filingDate");

            // iterate filing fields for each accession number
            for (int i = 0; i < accessions.size(); i++) {
                // skip non-insider transactions (not of 4-th form)
                if (!documents.get(i).asText().equals("xslF345X03/form4.xml")) continue;
                // get filing instance from retrieved data
                String accessionNo = accessions.get(i).asText();
                Date date = parseDate(dates.get(i).asText());
                filings.add(new Filing(accessionNo, cik, date));
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not parse SEC filing: " + e.getMessage());
        }
        return filings;
    }

    private void saveAllTransactions() {
        if (transactionRepository.count() != 0) return;
        List<Filing> filings = filingRepository.findAll();
        ProgressBar bar = new ProgressBar("Retrieving transaction data...", filings.size());
        for (int i = 0; i < filings.size(); i++) {
            transactionRepository.saveAll(getTransactions(filings.get(i)));
            bar.update(i + 1);
        }
    }

    public List<Transaction> getTransactions(Filing filing) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            // access form 4 data from EDGAR
            String formattedAccessionNo = filing.getAccessionNo().replace("-", "");
            String url = String.format(FORM_4_URL, filing.getCompanyCik(), formattedAccessionNo);
            JsonNode root = new XmlMapper().readTree(getDataStream(url));

            // save retrieved data
            String insiderCik = saveReportingInsider(root);
            transactions.addAll(getRootTransactions(root, filing.getAccessionNo(), insiderCik));
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("[ERROR] Could not read form 4: " + e.getMessage());
        }
        return transactions;
    }

    private List<Transaction> getRootTransactions(JsonNode root, String accessionNo, String insiderCik) {
        List<Transaction> transactions = new ArrayList<>();
        for (boolean isDerivative : new boolean[]{true, false}) {
            JsonNode node = getTransactionsNode(root, isDerivative);
            if (node == null) continue;
            handleNode(node, (o) -> {
                Transaction transaction = getTransactionOfJson(o, accessionNo, insiderCik);
                transactions.add(transaction);
            });
        }
        return transactions;
    }

    private Transaction getTransactionOfJson(JsonNode o, String accessionNo, String insiderCik) {
        JsonNode ownership = o.get("ownershipNature").get("directOrIndirectOwnership");
        JsonNode amounts = o.get("transactionAmounts");
        JsonNode postAmounts = o.get("postTransactionAmounts");
        JsonNode sharePrice = amounts.get("transactionPricePerShare").get("value");
        JsonNode coding = o.get("transactionCoding");

        return Transaction.builder()
                .accessionNo(accessionNo)
                .insiderCik(insiderCik)
                .securityTitle(o.get("securityTitle").get("value").asText())
                .code(coding.get("transactionCode").asText())
                .date(parseDate(o.get("transactionDate").get("value").asText()))
                .isDirect(ownership.get("value").asText().equals("D"))
                .shareCount(amounts.get("transactionShares").get("value").asDouble())
                .sharePrice((sharePrice != null) ? sharePrice.asDouble() : null)
                .leftShares(postAmounts.get("sharesOwnedFollowingTransaction").get("value").asDouble())
                .build();
    }

    private JsonNode getTransactionsNode(JsonNode root, boolean isDerivative) {
        String tableTag = isDerivative ? "derivativeTable" : "nonDerivativeTable";
        String transactionsTag = isDerivative ? "derivativeTransaction" : "nonDerivativeTransaction";
        return root.has(tableTag) ? root.get(tableTag).get(transactionsTag) : null;
    }

    private String saveReportingInsider(JsonNode root) {
        JsonNode owner = root.get("reportingOwner");
        JsonNode ownerId = owner.get("reportingOwnerId");
        String insiderCik = ownerId.get("rptOwnerCik").asText();
        if (!insiderRepository.existsById(insiderCik)) {
            String companyCik = root.get("issuer").get("issuerCik").asText();
            String insiderName = ownerId.get("rptOwnerName").asText();
            List<String> relationships = getRelationshipTitles(owner.get("reportingOwnerRelationship"));
            insiderRepository.save(new Insider(insiderCik, insiderName, companyCik, relationships));
        }
        return insiderCik;
    }

    /**
     * Defines insider relationship to the company by retrieving titles from the 4-th form.
     * Titles: Director, Officer, 10% Owner, Other
     *
     * @param relationship json node that contains information about insider relationship to the company
     * @return relationship titles of string type
     */
    private List<String> getRelationshipTitles(JsonNode relationship) {
        List<String> titles = new ArrayList<>();
        JsonNode isDirector = relationship.get("isDirector");
        JsonNode isOfficer = relationship.get("isOfficer");
        JsonNode isTenPercentOwner = relationship.get("isTenPercentOwner");
        JsonNode isOther = relationship.get("isOther");

        if (hasOne(isDirector)) titles.add("Director");
        if (hasOne(isOfficer)) titles.add(relationship.get("officerTitle").asText());
        if (hasOne(isTenPercentOwner)) titles.add("10% Owner");
        if (hasOne(isOther)) titles.add("Other");
        return titles;
    }

    private boolean hasOne(JsonNode node) {
        return (node != null) && (node.asInt() == 1);
    }

    /**
     * Parses dates according to the format defined by SEC.
     *
     * @param date string date e.g. "yyyy-MM-dd"
     * @return parsed date from the input
     */
    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat(SEC_DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            System.out.println("[ERROR] Could not parse date: " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts a CIK number to its JSON filename. Used to retrieve data from EDGAR.
     * e.g. 50863 -> CIK0000050863.json
     *
     * @param cik company identifier
     * @return filename with 'CIK' and 10 digits in front, and file format at the end
     */
    private String getCikFilename(String cik) {
        return "CIK" + "0".repeat(10 - cik.length()) + cik + ".json";
    }
}
