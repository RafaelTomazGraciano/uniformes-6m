package com.six_m.uniform.domain.aluno;

import com.six_m.uniform.domain.escola.Escola;
import com.six_m.uniform.domain.turma.Turma;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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

    @Builder.Default
    private Boolean deletado = false;
}
