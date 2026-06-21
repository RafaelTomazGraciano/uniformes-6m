package com.six_m.uniform.domain.lote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoteRepository extends JpaRepository<Lote, UUID> {
}
