package com.six_m.uniform.domain.uniforme;

import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name="uniforme")
@SQLDelete(sql = "UPDATE uniforme SET deletado = true WHERE id = ?")
@SQLRestriction("deletado = false")
public class Uniforme {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "tipo_id", nullable = false)
    private TipoUniforme tipoUniforme;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Tamanho tamanho;

    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Sexo sexo;

    @Builder.Default
    private Boolean deletado = false;
}
