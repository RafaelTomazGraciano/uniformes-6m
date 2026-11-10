package com.six_m.uniform.domain.uniforme;

import com.six_m.uniform.domain.tipoUniforme.TipoUniformeService;
import com.six_m.uniform.domain.uniforme.dto.ResponseEstoquePainelDTO;
import com.six_m.uniform.domain.uniforme.dto.ResponseUniformeDTO;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UniformeService {

    private final UniformeRepository uniformeRepository;
    private final TipoUniformeService tipoUniformeService;

    @Transactional(readOnly = true)
    public Page<ResponseUniformeDTO> buscarTodosUniformes(Pageable pageable) {
        return uniformeRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponseUniformeDTO buscarUniforme(UUID id) {
        return toResponseDTO(buscarUniformeEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ResponseEstoquePainelDTO> buscarPainelEstoque() {
        return uniformeRepository.buscarPainelEstoque().stream()
                .map(this::toPainelDTO)
                .toList();
    }

    /* Duas entregas com várias peças travando as mesmas linhas em ordem diferente
       se esperam para sempre (deadlock). Travando tudo de antemão, sempre na mesma
       ordem, a segunda só espera a primeira terminar. Linhas que não existem são
       ignoradas: quem as usa depois é que responde com 404. */
    @Transactional
    public void travarEstoque(Collection<UUID> uniformeIds) {
        uniformeIds.stream()
                .distinct()
                .sorted()
                .forEach(uniformeRepository::buscarComLockPorId);
    }

    @Transactional
    public void travarEstoquePorCombinacao(Collection<CombinacaoUniforme> combinacoes) {
        combinacoes.stream()
                .distinct()
                .sorted()
                .forEach(c -> uniformeRepository.findByTipoUniformeIdAndTamanhoAndSexo(c.tipoUniformeId(), c.tamanho(), c.sexo()));
    }

    @Transactional
    public Uniforme darEntrada(UUID tipoUniformeId, Tamanho tamanho, Sexo sexo, Integer quantidade) {
        Uniforme uniforme = uniformeRepository.findByTipoUniformeIdAndTamanhoAndSexo(tipoUniformeId, tamanho, sexo)
                .orElseGet(() -> Uniforme.builder()
                        .tipoUniforme(tipoUniformeService.buscarTipoUniformeEntidade(tipoUniformeId))
                        .tamanho(tamanho)
                        .sexo(sexo)
                        .quantidade(0)
                        .build());

        uniforme.setQuantidade(uniforme.getQuantidade() + quantidade);

        return uniformeRepository.save(uniforme);
    }

    @Transactional
    public void estornarEntrada(UUID tipoUniformeId, Tamanho tamanho, Sexo sexo, Integer quantidade) {
        Uniforme uniforme = uniformeRepository.findByTipoUniformeIdAndTamanhoAndSexo(tipoUniformeId, tamanho, sexo)
                .orElseThrow(() -> new NotFoundException("Uniforme não encontrado para estornar a entrada anterior"));

        if (uniforme.getQuantidade() < quantidade) {
            throw new BadRequestException("Não é possível estornar a entrada: o estoque ficaria negativo, pois parte das unidades já saiu em pedidos");
        }

        uniforme.setQuantidade(uniforme.getQuantidade() - quantidade);

        uniformeRepository.save(uniforme);
    }

    @Transactional
    public void darSaida(UUID uniformeId, Integer quantidade) {
        Uniforme uniforme = uniformeRepository.buscarComLockPorId(uniformeId)
                .orElseThrow(() -> new NotFoundException("Uniforme não encontrado com o ID: " + uniformeId));

        if (uniforme.getQuantidade() < quantidade) {
            throw new BadRequestException("Quantidade solicitada (" + quantidade + ") maior que o estoque disponível (" + uniforme.getQuantidade() + ")");
        }

        uniforme.setQuantidade(uniforme.getQuantidade() - quantidade);
        uniformeRepository.save(uniforme);
    }

    @Transactional
    public void estornarSaida(UUID uniformeId, Integer quantidade) {
        Uniforme uniforme = uniformeRepository.buscarComLockPorId(uniformeId)
                .orElseThrow(() -> new NotFoundException("Uniforme não encontrado com o ID: " + uniformeId));

        uniforme.setQuantidade(uniforme.getQuantidade() + quantidade);
        uniformeRepository.save(uniforme);
    }

    public Uniforme buscarUniformeEntidade(UUID id) {
        return uniformeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Uniforme não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Uniforme> buscarTodosUniformesEntidades() {
        return uniformeRepository.findAll();
    }

    private ResponseEstoquePainelDTO toPainelDTO(EstoqueView linha) {
        return new ResponseEstoquePainelDTO(
                linha.getUniformeId(),
                linha.getTipoUniformeId(),
                linha.getTipo(),
                Tamanho.valueOf(linha.getTamanho()),
                Sexo.valueOf(linha.getSexo()),
                linha.getQuantidade(),
                linha.getEstoqueBaixo(),
                SituacaoEstoque.valueOf(linha.getSituacao())
        );
    }

    private ResponseUniformeDTO toResponseDTO(Uniforme uniforme) {
        return new ResponseUniformeDTO(
                uniforme.getId(),
                uniforme.getTipoUniforme().getId(),
                uniforme.getTipoUniforme().getTipo(),
                uniforme.getTamanho(),
                uniforme.getQuantidade(),
                uniforme.getSexo(),
                uniforme.getTipoUniforme().getEstoqueBaixo(),
                uniforme.estaComEstoqueBaixo()
        );
    }
}