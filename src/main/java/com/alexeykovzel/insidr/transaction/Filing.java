package com.alexeykovzel.insidr.transaction;

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
public class Filing {

    @Id
    @Column(name = "accession_no")
    private String accessionNo;

    @Column(name = "company_cik")
    private String companyCik;

    @Column(name = "date")
    private Date date;
}
