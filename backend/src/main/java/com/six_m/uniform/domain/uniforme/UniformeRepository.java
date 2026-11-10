package com.six_m.uniform.domain.uniforme;

import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UniformeRepository extends JpaRepository<Uniforme, UUID> {
    boolean existsByTipoUniformeId(UUID tipoUniformeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Uniforme> findByTipoUniformeIdAndTamanhoAndSexo(UUID tipoUniformeId, Tamanho tamanho, Sexo sexo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Uniforme u where u.id = :id")
    Optional<Uniforme> buscarComLockPorId(@Param("id") UUID id);

    /* Os enums do Postgres viram texto aqui para a projeção não depender de como o driver os entrega. */
    @Query(value = """
            select v.uniforme_id as "uniformeId",
                   v.tipo_uniforme_id as "tipoUniformeId",
                   v.tipo as "tipo",
                   v.tamanho::text as "tamanho",
                   v.sexo::text as "sexo",
                   v.quantidade as "quantidade",
                   v.estoque_baixo as "estoqueBaixo",
                   v.situacao as "situacao"
            from vw_estoque v
            order by lower(v.tipo), v.tamanho, v.sexo
            """, nativeQuery = true)
    List<EstoqueView> buscarPainelEstoque();
}
