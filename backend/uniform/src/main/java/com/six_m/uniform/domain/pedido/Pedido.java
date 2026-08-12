package com.six_m.uniform.domain.pedido;

import com.six_m.uniform.domain.aluno.Aluno;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniforme;
import com.six_m.uniform.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_efetivada")
    private LocalDateTime dataEfetivada;

    @OneToMany(mappedBy = "pedido")
    private List<PedidoUniforme> itens;
}
