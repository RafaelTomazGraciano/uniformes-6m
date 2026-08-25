package com.six_m.uniform.domain.tipoUniforme;

import com.six_m.uniform.domain.itemLote.ItemLoteRepository;
import com.six_m.uniform.domain.tipoUniforme.dto.RequestAtualizarTipoUniformeDTO;
import com.six_m.uniform.domain.tipoUniforme.dto.RequestCriarTipoUniformeDTO;
import com.six_m.uniform.domain.tipoUniforme.dto.ResponseTipoUniformeDTO;
import com.six_m.uniform.domain.uniforme.UniformeRepository;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TipoUniformeService {

    private final TipoUniformeRepository tipoUniformeRepository;
    private final UniformeRepository uniformeRepository;
    private final ItemLoteRepository itemLoteRepository;

    @Transactional
    public ResponseTipoUniformeDTO criarTipoUniforme(RequestCriarTipoUniformeDTO dto) {
        TipoUniforme tipoUniforme = TipoUniforme.builder()
                .tipo(dto.tipo())
                .build();

        tipoUniforme = tipoUniformeRepository.save(tipoUniforme);

        return toResponseDTO(tipoUniforme);
    }

    @Transactional(readOnly = true)
    public Page<ResponseTipoUniformeDTO> buscarTodosTiposUniforme(Pageable pageable) {
        return tipoUniformeRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponseTipoUniformeDTO buscarTipoUniforme(UUID id) {
        return toResponseDTO(buscarTipoUniformeOuFalhar(id));
    }

    @Transactional
    public ResponseTipoUniformeDTO atualizarTipoUniforme(UUID id, RequestAtualizarTipoUniformeDTO dto) {
        TipoUniforme tipoUniforme = buscarTipoUniformeOuFalhar(id);

        tipoUniforme.setTipo(dto.tipo());

        tipoUniforme = tipoUniformeRepository.save(tipoUniforme);

        return toResponseDTO(tipoUniforme);
    }

    @Transactional
    public MessageResponseDTO deletarTipoUniforme(UUID id) {
        TipoUniforme tipoUniforme = buscarTipoUniformeOuFalhar(id);

        if (uniformeRepository.existsByTipoUniformeId(id)) {
            throw new BadRequestException("Não é possível excluir o tipo de uniforme: existem uniformes vinculados a ele");
        }

        if (itemLoteRepository.existsByTipoUniformeId(id)) {
            throw new BadRequestException("Não é possível excluir o tipo de uniforme: existem itens em lote vinculados a ele");
        }

        tipoUniformeRepository.delete(tipoUniforme);

        return new MessageResponseDTO("Tipo de uniforme deletado com sucesso");
    }

    private TipoUniforme buscarTipoUniformeOuFalhar(UUID id) {
        return tipoUniformeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de uniforme não encontrado com o ID: " + id));
    }

    private ResponseTipoUniformeDTO toResponseDTO(TipoUniforme tipoUniforme) {
        return new ResponseTipoUniformeDTO(tipoUniforme.getId(), tipoUniforme.getTipo());
    }
}