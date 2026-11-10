package com.six_m.uniform.domain.lote;

import com.six_m.uniform.domain.itemLote.ItemLote;
import com.six_m.uniform.domain.itemLote.ItemLoteService;
import com.six_m.uniform.domain.itemLote.dto.ResponseItemLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestAtualizarLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestCriarLoteDTO;
import com.six_m.uniform.domain.lote.dto.RequestItemEntradaDTO;
import com.six_m.uniform.domain.lote.dto.ResponseLoteDTO;
import com.six_m.uniform.domain.notaFiscal.NotaFiscal;
import com.six_m.uniform.domain.notaFiscal.NotaFiscalService;
import com.six_m.uniform.domain.uniforme.CombinacaoUniforme;
import com.six_m.uniform.domain.uniforme.UniformeService;
import com.six_m.uniform.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;
    private final NotaFiscalService notaFiscalService;
    private final ItemLoteService itemLoteService;
    private final UniformeService uniformeService;

    @Transactional
    public ResponseLoteDTO criarLote(RequestCriarLoteDTO dto) {
        NotaFiscal notaFiscal = notaFiscalService.criarParaLote(dto.chaveAcesso());

        uniformeService.travarEstoquePorCombinacao(combinacoesDosItens(dto.itens()));

        // Sem data o lote sumia dos relatórios por período: vale o momento do registro.
        Lote lote = Lote.builder()
                .notaFiscal(notaFiscal)
                .fornecedor(dto.fornecedor())
                .dataEntrega(dto.dataEntrega() != null ? dto.dataEntrega() : LocalDateTime.now())
                .build();

        lote = loteRepository.save(lote);

        List<ItemLote> itens = itemLoteService.criarItensParaLote(lote, dto.itens());

        for (ItemLote item : itens) {
            uniformeService.darEntrada(item.getTipoUniforme().getId(), item.getTamanho(), item.getSexo(), item.getQuantidade());
        }

        return toResponseDTO(lote, itens);
    }

    @Transactional(readOnly = true)
    public Page<ResponseLoteDTO> buscarTodosLotes(Pageable pageable) {
        Page<Lote> pagina = loteRepository.findAll(pageable);

        List<UUID> loteIds = pagina.getContent().stream().map(Lote::getId).toList();
        Map<UUID, List<ItemLote>> itensPorLote = itemLoteService.buscarItensPorLotes(loteIds);

        return pagina.map(lote -> toResponseDTO(lote, itensPorLote.getOrDefault(lote.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public ResponseLoteDTO buscarLote(UUID id) {
        Lote lote = buscarLoteOuFalhar(id);
        List<ItemLote> itens = itemLoteService.buscarItensPorLote(id);
        return toResponseDTO(lote, itens);
    }

    @Transactional
    public ResponseLoteDTO atualizarLote(UUID id, RequestAtualizarLoteDTO dto) {
        Lote lote = buscarLoteOuFalhar(id);
        NotaFiscal notaFiscal = notaFiscalService.atualizarParaLote(lote.getNotaFiscal(), dto.chaveAcesso());

        List<ItemLote> itensAntigos = itemLoteService.buscarItensPorLote(id);

        List<CombinacaoUniforme> combinacoes = new ArrayList<>(combinacoesDosItens(dto.itens()));
        itensAntigos.forEach(item -> combinacoes.add(
                new CombinacaoUniforme(item.getTipoUniforme().getId(), item.getTamanho(), item.getSexo())));
        uniformeService.travarEstoquePorCombinacao(combinacoes);

        List<ItemLote> itensNovos = itemLoteService.criarItensParaLote(lote, dto.itens());
        for (ItemLote itemNovo : itensNovos) {
            uniformeService.darEntrada(itemNovo.getTipoUniforme().getId(), itemNovo.getTamanho(), itemNovo.getSexo(), itemNovo.getQuantidade());
        }

        for (ItemLote itemAntigo : itensAntigos) {
            uniformeService.estornarEntrada(itemAntigo.getTipoUniforme().getId(), itemAntigo.getTamanho(), itemAntigo.getSexo(), itemAntigo.getQuantidade());
        }
        itemLoteService.deletarItensPorLote(itensAntigos);

        lote.setNotaFiscal(notaFiscal);
        lote.setFornecedor(dto.fornecedor());
        // Editar sem informar a data mantém a original, em vez de apagá-la.
        if (dto.dataEntrega() != null) {
            lote.setDataEntrega(dto.dataEntrega());
        } else if (lote.getDataEntrega() == null) {
            lote.setDataEntrega(LocalDateTime.now());
        }
        lote = loteRepository.save(lote);

        return toResponseDTO(lote, itensNovos);
    }

    private List<CombinacaoUniforme> combinacoesDosItens(List<RequestItemEntradaDTO> itens) {
        return itens.stream()
                .map(item -> new CombinacaoUniforme(item.tipoUniformeId(), item.tamanho(), item.sexo()))
                .toList();
    }

    private Lote buscarLoteOuFalhar(UUID id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lote não encontrado com o ID: " + id));
    }

    private ResponseLoteDTO toResponseDTO(Lote lote, List<ItemLote> itens) {
        List<ResponseItemLoteDTO> itensDto = itens.stream()
                .map(item -> new ResponseItemLoteDTO(
                        item.getId(),
                        item.getTipoUniforme().getId(),
                        item.getTipoUniforme().getTipo(),
                        lote.getId(),
                        lote.getFornecedor(),
                        item.getTamanho(),
                        item.getQuantidade(),
                        item.getSexo()
                ))
                .toList();

        return new ResponseLoteDTO(
                lote.getId(),
                lote.getNotaFiscal().getId(),
                lote.getNotaFiscal().getChaveAcesso(),
                lote.getFornecedor(),
                lote.getDataEntrega(),
                itensDto
        );
    }
}