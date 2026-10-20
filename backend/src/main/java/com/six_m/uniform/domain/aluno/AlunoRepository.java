package com.six_m.uniform.domain.aluno;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, UUID> {
    boolean existsByTurmaId(UUID turmaId);

    long countByTurmaId(UUID turmaId);

    @Query("select a.turma.id as turmaId, count(a) as quantidade from Aluno a where a.turma.id in :turmaIds group by a.turma.id")
    List<QuantidadeAlunosPorTurma> contarPorTurmas(@Param("turmaIds") Collection<UUID> turmaIds);

    interface QuantidadeAlunosPorTurma {
        UUID getTurmaId();

        long getQuantidade();
    }
}
