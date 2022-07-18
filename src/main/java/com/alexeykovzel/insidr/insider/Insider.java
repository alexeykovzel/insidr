package com.alexeykovzel.insidr.insider;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "insider")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Insider {

    @Id
    @Column(name = "cik")
    private String cik;

    @Column(name = "name")
    private String name;

    @Column(name = "company_cik")
    private String companyCik;

    @Column(name = "relationship")
    private String relationship;
}
