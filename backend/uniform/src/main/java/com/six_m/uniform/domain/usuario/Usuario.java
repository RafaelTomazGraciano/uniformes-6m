package com.six_m.uniform.domain.usuario;

import com.six_m.uniform.domain.escola.Escola;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="usuario")
@SQLDelete(sql = "UPDATE usuario SET deletado = true WHERE id = ?")
@SQLRestriction("deletado = false")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "escola_id", nullable = false)
    private Escola escola;

    private String nome;

    private String email;

    private String senha;

    private Boolean deletado = false;
}
