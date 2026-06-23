package com.six_m.uniform.domain.itemLote;

import com.six_m.uniform.domain.lote.Lote;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.enums.Sexo;
import com.six_m.uniform.enums.Tamanho;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name="item_lote")
public class ItemLote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "tipo_uniforme_id", nullable = false)
    private TipoUniforme tipoUniforme;

    @ManyToOne
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Enumerated(EnumType.STRING)
    private Tamanho tamanho;

    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

}
