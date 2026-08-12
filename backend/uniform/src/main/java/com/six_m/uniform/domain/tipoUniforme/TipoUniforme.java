package com.six_m.uniform.domain.tipoUniforme;

import com.six_m.uniform.domain.escola.Escola;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name="tipo_uniforme")
public class TipoUniforme {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String tipo;
}
