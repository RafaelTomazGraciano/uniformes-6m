package com.six_m.uniform.domain.itemLote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemLoteRepository extends JpaRepository<ItemLote, UUID> {
    boolean existsByTipoUniformeId(UUID tipoUniformeId);
}
