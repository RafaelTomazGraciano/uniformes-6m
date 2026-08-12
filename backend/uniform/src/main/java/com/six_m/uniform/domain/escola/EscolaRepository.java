package com.six_m.uniform.domain.escola;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EscolaRepository extends JpaRepository<Escola, UUID> {
}
