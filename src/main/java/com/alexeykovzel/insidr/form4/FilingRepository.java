package com.alexeykovzel.insidr.form4;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FilingRepository extends JpaRepository<EdgarFiling, String> {

    @Query("SELECT f FROM EdgarFiling f WHERE NOT EXISTS (SELECT 1 FROM Transaction t WHERE t.accessionNo = f.accessionNo)")
    List<EdgarFiling> findFilingsWithNoTransactions();
}
