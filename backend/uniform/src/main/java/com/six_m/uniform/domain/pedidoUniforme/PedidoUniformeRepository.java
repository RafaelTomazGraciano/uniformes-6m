package com.six_m.uniform.domain.pedidoUniforme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PedidoUniformeRepository extends JpaRepository<PedidoUniforme, UUID> {
}
