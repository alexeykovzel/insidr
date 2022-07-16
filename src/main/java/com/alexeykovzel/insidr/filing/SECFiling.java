package com.alexeykovzel.insidr.filing;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "filing")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SECFiling {

    @Id
    @Column(name = "accession_no")
    private String id;

    @Column(name = "filing_date")
    private Date filingDate;

    @Column(name = "report_date")
    private Date reportDate;
}
