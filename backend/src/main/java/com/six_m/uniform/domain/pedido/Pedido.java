package com.six_m.uniform.domain.pedido;

import com.six_m.uniform.domain.aluno.Aluno;
import com.six_m.uniform.domain.pedidoUniforme.PedidoUniforme;
import com.six_m.uniform.domain.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name="pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /* IGNORE: se o aluno não puder ser carregado (ex.: cadastro excluído antes da
       saída existir), a entrega continua listável em vez de derrubar a página. */
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    /* A mesma coluna, só leitura: mantém o id do aluno mesmo quando `aluno` vem nulo. */
    @Setter(AccessLevel.NONE)
    @Column(name = "aluno_id", insertable = false, updatable = false)
    private UUID alunoId;

    /* Usuário também tem soft delete: quem registrou pode ter sido removido depois. */
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_efetivada")
    private LocalDateTime dataEfetivada;

    @OneToMany(mappedBy = "pedido")
    private List<PedidoUniforme> itens;
}
