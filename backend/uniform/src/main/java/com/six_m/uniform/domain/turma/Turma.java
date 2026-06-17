package com.six_m.uniform.domain.turma;

import com.six_m.uniform.domain.escola.Escola;
import com.six_m.uniform.enums.Ensino;
import com.six_m.uniform.enums.Turno;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="turma")
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "escola_id", nullable = false)
    private Escola escola;

    private String nome;

    private Turno turno;

    private Ensino ensino;

}
