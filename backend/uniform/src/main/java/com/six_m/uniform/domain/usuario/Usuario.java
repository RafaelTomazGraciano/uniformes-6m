package com.six_m.uniform.domain.usuario;

import com.six_m.uniform.domain.escola.Escola;
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
@Table(name="usuario")
@SQLDelete(sql = "UPDATE usuario SET deletado = true WHERE id = ?")
@SQLRestriction("deletado = false")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;

    private String email;

    private String senha;

    @Builder.Default
    private Boolean deletado = false;
}
