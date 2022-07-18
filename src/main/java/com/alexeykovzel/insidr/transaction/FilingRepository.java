package com.alexeykovzel.insidr.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FilingRepository extends JpaRepository<Filing, String> {
}
