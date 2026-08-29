package com.six_m.uniform.domain;

import com.six_m.uniform.domain.itemLote.ItemLote;
import com.six_m.uniform.domain.itemLote.ItemLoteService;
import com.six_m.uniform.domain.lote.Lote;
import com.six_m.uniform.domain.lote.LoteRepository;
import com.six_m.uniform.domain.lote.LoteService;
import com.six_m.uniform.domain.lote.dto.RequestAtualizarLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestCriarLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestItemEntradaDTO;
import com.six_m.uniform.domain.lote.dto.ResponseLoteDTO;
import com.six_m.uniform.domain.notaFiscal.NotaFiscal;
import com.six_m.uniform.domain.notaFiscal.NotaFiscalService;
import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoteServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private NotaFiscalService notaFiscalService;

    @Mock
    private ItemLoteService itemLoteService;

    @Mock
    private UniformeService uniformeService;

    @InjectMocks
    private LoteService loteService;


    @Test
    void deveCriarLoteComSucesso() {
        RequestItemEntradaDTO itemDto = new RequestItemEntradaDTO(UUID.randomUUID(), Tamanho.M, Sexo.MASCULINO, 10);
        RequestCriarLoteDTO dto = new RequestCriarLoteDTO("chave-1", "Fornecedor A", null, List.of(itemDto));

        NotaFiscal notaFiscal = NotaFiscal.builder().id(UUID.randomUUID()).chaveAcesso("chave-1").build();
        when(notaFiscalService.criarParaLote("chave-1")).thenReturn(notaFiscal);

        UUID loteId = UUID.randomUUID();
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> {
            Lote salvo = invocation.getArgument(0);
            salvo.setId(loteId);
            return salvo;
        });

        TipoUniforme tipoUniforme = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        ItemLote itemSalvo = ItemLote.builder().id(UUID.randomUUID()).tipoUniforme(tipoUniforme).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).quantidade(10).build();
        when(itemLoteService.criarItensParaLote(any(Lote.class), eq(dto.itens()))).thenReturn(List.of(itemSalvo));

        ResponseLoteDTO response = loteService.criarLote(dto);

        assertEquals(loteId, response.id());
        assertEquals("chave-1", response.notaFiscalChaveAcesso());
        assertEquals(1, response.itens().size());
        verify(uniformeService).darEntrada(tipoUniforme.getId(), Tamanho.M, Sexo.MASCULINO, 10);
    }

    @Test
    void deveBuscarTodosLotesPaginadoComItens() {
        UUID loteId = UUID.randomUUID();
        Lote lote = Lote.builder().id(loteId).notaFiscal(NotaFiscal.builder().id(UUID.randomUUID()).chaveAcesso("chave-1").build()).fornecedor("Fornecedor A").build();

        TipoUniforme tipoUniforme = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        ItemLote item = ItemLote.builder().id(UUID.randomUUID()).lote(lote).tipoUniforme(tipoUniforme).tamanho(Tamanho.M).sexo(Sexo.MASCULINO).quantidade(10).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(loteRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(lote), pageable, 1));
        when(itemLoteService.buscarItensPorLotes(List.of(loteId))).thenReturn(Map.of(loteId, List.of(item)));

        var resultado = loteService.buscarTodosLotes(pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().getFirst().itens().size());
        verify(itemLoteService, never()).buscarItensPorLote(any());
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaLotes() {
        Pageable pageable = PageRequest.of(0, 10);
        when(loteRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var resultado = loteService.buscarTodosLotes(pageable);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarLotePorIdComItens() {
        UUID loteId = UUID.randomUUID();
        Lote lote = Lote.builder().id(loteId).notaFiscal(NotaFiscal.builder().id(UUID.randomUUID()).chaveAcesso("chave-1").build()).fornecedor("Fornecedor A").build();

        when(loteRepository.findById(loteId)).thenReturn(Optional.of(lote));
        when(itemLoteService.buscarItensPorLote(loteId)).thenReturn(List.of());

        ResponseLoteDTO response = loteService.buscarLote(loteId);

        assertEquals(loteId, response.id());
        assertTrue(response.itens().isEmpty());
    }

    @Test
    void deveLancarExcecaoQuandoLoteNaoExisteAoBuscar() {
        UUID id = UUID.randomUUID();
        when(loteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> loteService.buscarLote(id));
    }

    @Test
    void deveAtualizarLoteEstornandoItensAntigosEDandoEntradaNosNovos() {
        UUID loteId = UUID.randomUUID();
        NotaFiscal notaFiscal = NotaFiscal.builder().id(UUID.randomUUID()).chaveAcesso("chave-1").build();
        Lote loteExistente = Lote.builder().id(loteId).notaFiscal(notaFiscal).fornecedor("Fornecedor A").build();

        TipoUniforme tipoUniforme = TipoUniforme.builder().id(UUID.randomUUID()).tipo("Camiseta").build();
        ItemLote itemAntigo = ItemLote.builder()
                .id(UUID.randomUUID())
                .tipoUniforme(tipoUniforme)
                .tamanho(Tamanho.M)
                .sexo(Sexo.MASCULINO)
                .quantidade(5)
                .build();

        RequestItemEntradaDTO itemNovoDto = new RequestItemEntradaDTO(tipoUniforme.getId(), Tamanho.G, Sexo.FEMININO, 8);
        RequestAtualizarLoteDTO dto = new RequestAtualizarLoteDTO("chave-1", "Fornecedor Atualizado", null, List.of(itemNovoDto));

        when(loteRepository.findById(loteId)).thenReturn(Optional.of(loteExistente));
        when(notaFiscalService.atualizarParaLote(notaFiscal, "chave-1")).thenReturn(notaFiscal);
        when(itemLoteService.buscarItensPorLote(loteId)).thenReturn(List.of(itemAntigo));

        ItemLote itemNovo = ItemLote.builder().id(UUID.randomUUID()).tipoUniforme(tipoUniforme).tamanho(Tamanho.G).sexo(Sexo.FEMININO).quantidade(8).build();
        when(itemLoteService.criarItensParaLote(loteExistente, dto.itens())).thenReturn(List.of(itemNovo));
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseLoteDTO response = loteService.atualizarLote(loteId, dto);

        verify(uniformeService).estornarEntrada(tipoUniforme.getId(), Tamanho.M, Sexo.MASCULINO, 5);
        verify(itemLoteService).deletarItensPorLote(List.of(itemAntigo));
        verify(uniformeService).darEntrada(tipoUniforme.getId(), Tamanho.G, Sexo.FEMININO, 8);
        assertEquals("Fornecedor Atualizado", response.fornecedor());
    }

    @Test
    void deveLancarExcecaoQuandoLoteNaoExisteAoAtualizar() {
        UUID loteId = UUID.randomUUID();
        RequestAtualizarLoteDTO dto = new RequestAtualizarLoteDTO("chave-1", "Fornecedor A", null, List.of());

        when(loteRepository.findById(loteId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> loteService.atualizarLote(loteId, dto));
        verify(notaFiscalService, never()).atualizarParaLote(any(), any());
        verify(itemLoteService, never()).buscarItensPorLote(any());
    }

    @Test
    void devePropagarExcecaoDeItemDuplicadoAoCriarLote() {
        RequestItemEntradaDTO item1 = new RequestItemEntradaDTO(UUID.randomUUID(), Tamanho.M, Sexo.MASCULINO, 5);
        RequestCriarLoteDTO dto = new RequestCriarLoteDTO("chave-1", "Fornecedor A", null, List.of(item1, item1));

        NotaFiscal notaFiscal = NotaFiscal.builder().id(UUID.randomUUID()).chaveAcesso("chave-1").build();
        when(notaFiscalService.criarParaLote("chave-1")).thenReturn(notaFiscal);
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemLoteService.criarItensParaLote(any(Lote.class), eq(dto.itens())))
                .thenThrow(new BadRequestException("Item duplicado no lote: mesmo tipo de uniforme, tamanho e sexo informados mais de uma vez"));

        assertThrows(BadRequestException.class, () -> loteService.criarLote(dto));
    }
}