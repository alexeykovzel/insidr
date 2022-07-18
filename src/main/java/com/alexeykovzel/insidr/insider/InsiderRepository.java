package com.alexeykovzel.insidr.insider;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InsiderRepository extends JpaRepository<Insider, String> {
}
