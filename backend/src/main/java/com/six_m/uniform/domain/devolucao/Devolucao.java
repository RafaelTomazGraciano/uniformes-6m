package com.six_m.uniform.domain.devolucao;

import com.six_m.uniform.domain.pedidoUniforme.PedidoUniforme;
import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.shared.enums.SituacaoDevolucao;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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
@Table(name="devolucao")
public class Devolucao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "pedido_uniforme_id", nullable = false)
    private PedidoUniforme pedidoUniforme;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Builder.Default
    private SituacaoDevolucao situacao = SituacaoDevolucao.BOM_ESTADO;

    @Column(name = "data_devolucao", nullable = false, updatable = false)
    private LocalDateTime dataDevolucao;

}
