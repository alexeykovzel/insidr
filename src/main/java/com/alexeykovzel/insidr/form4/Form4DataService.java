package com.alexeykovzel.insidr.form4;

import com.alexeykovzel.insidr.insider.Insider;
import com.alexeykovzel.insidr.insider.InsiderRepository;
import com.alexeykovzel.insidr.utils.DateUtils;
import com.alexeykovzel.insidr.EdgarDataService;
import com.alexeykovzel.insidr.company.Company;
import com.alexeykovzel.insidr.company.CompanyRepository;
import com.alexeykovzel.insidr.utils.ProgressBar;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class Form4DataService extends EdgarDataService {
    private final CompanyRepository companyRepository;
    private final FilingRepository filingRepository;
    private final TransactionRepository transactionRepository;
    private final InsiderRepository insiderRepository;

    @Autowired
    public Form4DataService(CompanyRepository companyRepository,
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
        saveAllFilings();
        saveAllTransactions();
    }

    private void saveAllFilings() {
        List<Company> companies = companyRepository.findAll();
        ProgressBar bar = new ProgressBar("Retrieving filing data...", companies.size());
        filingRepository.deleteAll();
        for (int i = 0; i < companies.size(); i++) {
            String cik = companies.get(i).getCik();
            filingRepository.saveAll(getForm4Filings(cik));
            bar.update(i + 1);
        }
    }

    private void saveAllTransactions() {
        List<EdgarFiling> filings = filingRepository.findFilingsWithNoTransactions();
        ProgressBar bar = new ProgressBar("Retrieving transaction data...", filings.size());
        for (int i = 0; i < filings.size(); i++) {
            transactionRepository.saveAll(getTransactions(filings.get(i)));
            bar.update(i + 1);
        }
    }

    private List<EdgarFiling> getForm4Filings(String cik) {
        List<EdgarFiling> filings = new ArrayList<>();
        String url = String.format(SUBMISSIONS_URL, "CIK" + cik + ".json");
        try {
            // define nodes for accessing filing data
            JsonNode root = getJsonByUrl(url);
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
                Date date = new DateUtils().parse(dates.get(i).asText(), FORM_4_DATE_FORMAT);
                filings.add(new EdgarFiling(accessionNo, cik, date));
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("[ERROR] Could not parse SEC filing: " + e.getMessage());
        }
        return filings;
    }

    private List<Transaction> getTransactions(EdgarFiling filing) {
        List<Transaction> transactions = new ArrayList<>();
        String accessionNo = filing.getAccessionNo();
        try {
            // access filing data as F-4 from SEC
            String formattedAccessionNo = accessionNo.replace("-", "");
            String url = String.format(ARCHIVES_DATA_URL, filing.getCompanyCik(), formattedAccessionNo, "form4.xml");
            JsonNode root = getXmlByUrl(url);

            // save retrieved data to the database
            String insiderCik = saveReportingInsider(root);

            // save derivative and non-derivative transactions
            for (boolean isDerivative : new boolean[]{true, false}) {
                String tableTag = isDerivative ? "derivativeTable" : "nonDerivativeTable";
                String transactionsTag = isDerivative ? "derivativeTransaction" : "nonDerivativeTransaction";
                JsonNode node = root.has(tableTag) ? root.get(tableTag).get(transactionsTag) : null;
                if (node != null) handleNode(node, (data) -> {
                    Transaction transaction = buildTransaction(data, accessionNo, insiderCik);
                    transactions.add(transaction);
                });
            }
        } catch (IOException | NullPointerException ignored) {
        }
        return transactions;
    }

    private Transaction buildTransaction(JsonNode data, String accessionNo, String insiderCik) {
        JsonNode ownership = data.get("ownershipNature").get("directOrIndirectOwnership");
        JsonNode amounts = data.get("transactionAmounts");
        JsonNode postAmounts = data.get("postTransactionAmounts");
        JsonNode sharePrice = amounts.get("transactionPricePerShare").get("value");
        JsonNode coding = data.get("transactionCoding");

        return Transaction.builder()
                .accessionNo(accessionNo)
                .insiderCik(insiderCik)
                .securityTitle(data.get("securityTitle").get("value").asText())
                .code(coding.get("transactionCode").asText())
                .date(new DateUtils().parse(data.get("transactionDate").get("value").asText(), FORM_4_DATE_FORMAT))
                .isDirect(ownership.get("value").asText().equals("D"))
                .shareCount(amounts.get("transactionShares").get("value").asDouble())
                .sharePrice((sharePrice != null) ? sharePrice.asDouble() : null)
                .leftShares(postAmounts.get("sharesOwnedFollowingTransaction").get("value").asDouble())
                .build();
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
}
