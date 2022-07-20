package com.alexeykovzel.insidr.company;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "companies")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Company {

    @Id
    @Column(name = "cik")
    private String cik;

    @Column(name = "title")
    private String title;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "exchange")
    private String exchange;
}
