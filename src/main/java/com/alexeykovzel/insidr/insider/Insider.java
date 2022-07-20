package com.alexeykovzel.insidr.insider;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "insiders")
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

    @ElementCollection
    @CollectionTable(name="relationships")
    private List<String> relationship;
}
