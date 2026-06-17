package com.six_m.uniform.domain.lote;

import com.six_m.uniform.domain.itemLote.ItemLote;
import com.six_m.uniform.domain.notaFiscal.NotaFiscal;
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
@Table(name="lote")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "nota_fiscal_id", nullable = false)
    private NotaFiscal notaFiscal;

    private String fornecedor;

    @Column(name = "data_entrega")
    private LocalDateTime dataEntrega;

    @OneToMany(mappedBy = "lote")
    private List<ItemLote> itens;
}
