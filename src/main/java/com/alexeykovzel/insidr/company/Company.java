package com.alexeykovzel.insidr.company;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "company")

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Company {

    @Id
    private String cik;

    @Column(name = "symbol")
    private String symbol;

    @ElementCollection
    @CollectionTable(name="company_names")
    private List<String> names;

    public Company(String cik, List<String> names) {
        this.cik = cik;
        this.names = names;
    }
}
