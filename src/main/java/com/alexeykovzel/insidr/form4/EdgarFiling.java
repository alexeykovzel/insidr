package com.alexeykovzel.insidr.form4;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "edgar_filings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class EdgarFiling {

    @Id
    @Column(name = "accession_no")
    private String accessionNo;

    @Column(name = "company_cik")
    private String companyCik;

    @Column(name = "filing_date")
    private Date date;
}
