package com.six_m.uniform.domain.notaFiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotaFiscalRepository extends JpaRepository<NotaFiscal, UUID> {
    boolean existsByChaveAcesso(String chaveAcesso);
}
