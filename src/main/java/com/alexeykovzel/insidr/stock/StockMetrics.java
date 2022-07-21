package com.alexeykovzel.insidr.stock;

import org.springframework.stereotype.Service;

@Service
public class StockMetrics {

    public double calculateRoi(double price, double entryPoint, int quantity, int dividends) {
        return ((price - entryPoint) * quantity + dividends) / entryPoint * quantity;
    }

    public double calculateAnnualisedRoi(double roi, double years) {
        return Math.pow(1 + roi, 1 / years) - 1;
    }
}
