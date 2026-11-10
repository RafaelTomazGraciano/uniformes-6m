package com.six_m.uniform.domain.requisicao;

import com.six_m.uniform.shared.enums.StatusRequisicao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface RequisicaoRepository extends JpaRepository<Requisicao, UUID> {
    Page<Requisicao> findByStatusIn(Collection<StatusRequisicao> status, Pageable pageable);
}
