package com.six_m.uniform.domain.uniforme;

import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.enums.Sexo;
import com.six_m.uniform.enums.Tamanho;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
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

    private Tamanho tamanho;

    private Integer quantidade;

    private Sexo sexo;

    private Boolean devolvido = false;

    private Boolean deletado = false;


    public void devolver() {
        this.devolvido = true;
    }
}
