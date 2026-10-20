package com.six_m.uniform.domain;

import com.six_m.uniform.domain.relatorio.Periodo;
import com.six_m.uniform.domain.relatorio.RelatorioPeriodoResolver;
import com.six_m.uniform.domain.relatorio.dto.RelatorioFiltroDTO;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.shared.enums.TipoFiltroRelatorio;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class RelatorioPeriodoResolverTest {

    private final RelatorioPeriodoResolver resolver = new RelatorioPeriodoResolver();

    @Test
    void deveResolverUmMesUnico() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2026, 3, null, null);

        Periodo periodo = resolver.resolver(filtro);

        assertEquals(LocalDateTime.of(2026, 3, 1, 0, 0, 0), periodo.inicio());
        assertEquals(LocalDateTime.of(2026, 3, 31, 23, 59, 59), periodo.fim());
    }

    @Test
    void deveResolverIntervaloDeMeses() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2026, 1, 2026, 6);

        Periodo periodo = resolver.resolver(filtro);

        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0, 0), periodo.inicio());
        assertEquals(LocalDateTime.of(2026, 6, 30, 23, 59, 59), periodo.fim());
    }

    @Test
    void deveResolverIntervaloDeMesesAtravessandoAno() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2025, 11, 2026, 2);

        Periodo periodo = resolver.resolver(filtro);

        assertEquals(LocalDateTime.of(2025, 11, 1, 0, 0, 0), periodo.inicio());
        assertEquals(LocalDateTime.of(2026, 2, 28, 23, 59, 59), periodo.fim());
    }

    @Test
    void deveLancarExcecaoQuandoMesInicioNaoInformadoParaFiltroPorMes() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2026, null, null, null);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> resolver.resolver(filtro));

        assertEquals("Mês de início é obrigatório quando o filtro é por mês", exception.getMessage());
    }

    @Test
    void deveResolverUmAnoUnico() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.ANO, 2026, null, null, null);

        Periodo periodo = resolver.resolver(filtro);

        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0, 0), periodo.inicio());
        assertEquals(LocalDateTime.of(2026, 12, 31, 23, 59, 59), periodo.fim());
    }

    @Test
    void deveResolverIntervaloDeAnos() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.ANO, 2024, null, 2026, null);

        Periodo periodo = resolver.resolver(filtro);

        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0, 0), periodo.inicio());
        assertEquals(LocalDateTime.of(2026, 12, 31, 23, 59, 59), periodo.fim());
    }

    @Test
    void deveLancarExcecaoQuandoInicioPosteriorAoFimPorMes() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.MES, 2026, 6, 2026, 1);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> resolver.resolver(filtro));

        assertEquals("O período de início não pode ser posterior ao período de fim", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoInicioPosteriorAoFimPorAno() {
        RelatorioFiltroDTO filtro = new RelatorioFiltroDTO(TipoFiltroRelatorio.ANO, 2026, null, 2024, null);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> resolver.resolver(filtro));

        assertEquals("O período de início não pode ser posterior ao período de fim", exception.getMessage());
    }
}