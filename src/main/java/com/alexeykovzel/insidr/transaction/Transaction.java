package com.alexeykovzel.insidr.transaction;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "transaction")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "filing_id")
    private String filingId;

    @Column(name = "insider_cik")
    private String insiderCik;

    @Column(name = "security_title")
    private String securityTitle;

    @Column(name = "code")
    private String code;

    @Column(name = "date")
    private Date date;

    @Column(name = "share_price")
    private Double sharePrice;

    @Column(name = "share_count")
    private Double shareCount;

    @Column(name = "left_shares")
    private Double leftShares;

    @Column(name = "is_direct")
    private Boolean isDirect;
}
