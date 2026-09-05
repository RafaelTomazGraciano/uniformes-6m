package com.six_m.uniform.domain.relatorio;

import com.six_m.uniform.domain.relatorio.dto.RelatorioFiltroDTO;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.shared.enums.TipoFiltroRelatorio;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Component
public class RelatorioPeriodoResolver {

    public Periodo resolver(RelatorioFiltroDTO filtro) {
        if (filtro.tipo() == TipoFiltroRelatorio.MES) {
            return resolverPorMes(filtro);
        }
        return resolverPorAno(filtro);
    }

    private Periodo resolverPorMes(RelatorioFiltroDTO filtro) {
        if (filtro.mesInicio() == null) {
            throw new BadRequestException("Mês de início é obrigatório quando o filtro é por mês");
        }

        int anoFim = filtro.anoFim() != null ? filtro.anoFim() : filtro.anoInicio();
        int mesFim = filtro.mesFim() != null ? filtro.mesFim() : filtro.mesInicio();

        LocalDateTime inicio = LocalDateTime.of(filtro.anoInicio(), filtro.mesInicio(), 1, 0, 0, 0);
        LocalDateTime fim = YearMonth.of(anoFim, mesFim).atEndOfMonth().atTime(23, 59, 59);

        validarOrdem(inicio, fim);
        return new Periodo(inicio, fim);
    }

    private Periodo resolverPorAno(RelatorioFiltroDTO filtro) {
        int anoFim = filtro.anoFim() != null ? filtro.anoFim() : filtro.anoInicio();

        LocalDateTime inicio = LocalDateTime.of(filtro.anoInicio(), 1, 1, 0, 0, 0);
        LocalDateTime fim = LocalDateTime.of(anoFim, 12, 31, 23, 59, 59);

        validarOrdem(inicio, fim);
        return new Periodo(inicio, fim);
    }

    private void validarOrdem(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio.isAfter(fim)) {
            throw new BadRequestException("O período de início não pode ser posterior ao período de fim");
        }
    }
}