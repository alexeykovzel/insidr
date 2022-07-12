package com.alexeykovzel.insidr.cik;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CIKRepository extends JpaRepository<CentralIndexKey, String> {
}
