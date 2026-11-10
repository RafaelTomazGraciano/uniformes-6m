package com.six_m.uniform.domain.requisicao;

import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.StatusRequisicao;
import com.six_m.uniform.shared.enums.Tamanho;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name="requisicao")
public class Requisicao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "tipo_uniforme_id", nullable = false)
    private TipoUniforme tipoUniforme;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Tamanho tamanho;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Sexo sexo;

    private Integer quantidade;

    private String observacao;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private StatusRequisicao status = StatusRequisicao.PENDENTE;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;
}
