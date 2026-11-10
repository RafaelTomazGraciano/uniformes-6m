package com.six_m.uniform.domain.aluno;

import com.six_m.uniform.domain.turma.Turma;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name="aluno")
@SQLDelete(sql = "UPDATE aluno SET deletado = true WHERE id = ?")
@SQLRestriction("deletado = false")
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    private String nome;

    private String nomeResponsavel;

    private String telefoneResponsavel;

    @Builder.Default
    private Boolean deletado = false;

    /* Aluno que saiu da escola fica inativo em vez de ser excluído: as entregas
       que ele recebeu continuam apontando para este cadastro. */
    @Builder.Default
    private Boolean ativo = true;

    private LocalDateTime dataSaida;

    public boolean isAtivo() {
        return !Boolean.FALSE.equals(ativo);
    }
}
