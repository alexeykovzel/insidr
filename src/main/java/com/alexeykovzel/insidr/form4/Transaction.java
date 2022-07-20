package com.alexeykovzel.insidr.form4;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "transactions")
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

    @Column(name = "accession_no")
    private String accessionNo;

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
