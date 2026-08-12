package com.six_m.uniform.domain.pedidoUniforme;

import com.six_m.uniform.domain.pedido.Pedido;
import com.six_m.uniform.domain.uniforme.Uniforme;
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
@Table(name="pedido_uniforme")
public class PedidoUniforme {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "uniforme_id", nullable = false)
    private Uniforme uniforme;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    private Integer quantidade;
}
