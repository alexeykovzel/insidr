package com.alexeykovzel.insidr.filing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FilingRepository extends JpaRepository<SECFiling, String> {
}
