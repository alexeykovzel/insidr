package com.alexeykovzel.insidr.filing;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "form4")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Form4Data {

    @Id
    @Column(name = "accession_no")
    private String accessionNo;

    @Column(name = "issuer_cik")
    private String issuerCik;

    @Column(name = "owner_cik")
    private String ownerCik;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "is_director")
    private boolean isDirector;

    @Column(name = "security_title")
    private String securityTitle;

    @Column(name = "swap_involved")
    private boolean swapInvolved;

    @Column(name = "transaction_date")
    private Date transactionDate;

    @Column(name = "transaction_code")
    private String transactionCode;

    @Column(name = "ownership")
    private String ownership;

    @Column(name = "share_count")
    private double shareCount;

    @Column(name = "share_price")
    private double sharePrice;

    @Column(name = "shares_owned")
    private double sharesOwned;
}
