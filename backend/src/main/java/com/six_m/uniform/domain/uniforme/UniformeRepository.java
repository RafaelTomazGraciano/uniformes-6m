package com.six_m.uniform.domain.uniforme;

import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UniformeRepository extends JpaRepository<Uniforme, UUID> {
    boolean existsByTipoUniformeId(UUID tipoUniformeId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)

    Optional<Uniforme> findByTipoUniformeIdAndTamanhoAndSexo(UUID tipoUniformeId, Tamanho tamanho, Sexo sexo);
}
