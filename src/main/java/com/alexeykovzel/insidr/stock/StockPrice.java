package com.alexeykovzel.insidr.stock;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "stock_prices")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "date")
    private Date date;

    @Column(name = "price")
    private Double price;

    @Column(name = "dividends")
    private Double dividends;

    public StockPrice(String symbol, Date date, Double price, Double dividends) {
        this.symbol = symbol;
        this.date = date;
        this.price = price;
        this.dividends = dividends;
    }
}
