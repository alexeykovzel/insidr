package com.alexeykovzel.insidr.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {

    @Query("SELECT c.symbol FROM Company c WHERE NOT EXISTS (SELECT 1 FROM StockPrice sp WHERE sp.symbol = c.symbol)")
    List<String> findSymbolsWithNoStockPrices();
}
