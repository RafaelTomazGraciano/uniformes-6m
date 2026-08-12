package com.six_m.uniform.domain.uniforme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UniformeRepository extends JpaRepository<Uniforme, UUID> {
}
