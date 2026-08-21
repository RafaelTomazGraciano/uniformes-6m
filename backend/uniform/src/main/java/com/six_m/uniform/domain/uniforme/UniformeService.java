package com.six_m.uniform.domain.uniforme;

import com.six_m.uniform.domain.tipoUniforme.TipoUniforme;
import com.six_m.uniform.domain.tipoUniforme.TipoUniformeRepository;
import com.six_m.uniform.domain.uniforme.dto.RequestAtualizarUniformeDTO;
import com.six_m.uniform.domain.uniforme.dto.RequestCriarUniformeDTO;
import com.six_m.uniform.domain.uniforme.dto.ResponseUniformeDTO;
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
public class UniformeService {

    private final UniformeRepository uniformeRepository;
    private final TipoUniformeRepository tipoUniformeRepository;

    @Transactional
    public ResponseUniformeDTO criarUniforme(RequestCriarUniformeDTO dto) {
        TipoUniforme tipoUniforme = buscarTipoUniformeOuFalhar(dto.tipoUniformeId());

        Uniforme uniforme = Uniforme.builder()
                .tipoUniforme(tipoUniforme)
                .tamanho(dto.tamanho())
                .quantidade(dto.quantidade())
                .sexo(dto.sexo())
                .build();

        uniforme = uniformeRepository.save(uniforme);

        return toResponseDTO(uniforme);
    }

    @Transactional(readOnly = true)
    public Page<ResponseUniformeDTO> buscarTodosUniformes(Pageable pageable) {
        return uniformeRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponseUniformeDTO buscarUniforme(UUID id) {
        return toResponseDTO(buscarUniformeOuFalhar(id));
    }

    @Transactional
    public ResponseUniformeDTO atualizarUniforme(UUID id, RequestAtualizarUniformeDTO dto) {
        Uniforme uniforme = buscarUniformeOuFalhar(id);
        TipoUniforme tipoUniforme = buscarTipoUniformeOuFalhar(dto.tipoUniformeId());

        uniforme.setTipoUniforme(tipoUniforme);
        uniforme.setTamanho(dto.tamanho());
        uniforme.setQuantidade(dto.quantidade());
        uniforme.setSexo(dto.sexo());

        uniforme = uniformeRepository.save(uniforme);

        return toResponseDTO(uniforme);
    }

    @Transactional
    public ResponseUniformeDTO devolverUniforme(UUID id) {
        Uniforme uniforme = buscarUniformeOuFalhar(id);

        uniforme.devolver();

        uniforme = uniformeRepository.save(uniforme);

        return toResponseDTO(uniforme);
    }

    @Transactional
    public MessageResponseDTO deletarUniforme(UUID id) {
        Uniforme uniforme = buscarUniformeOuFalhar(id);
        uniformeRepository.delete(uniforme);
        return new MessageResponseDTO("Uniforme deletado com sucesso");
    }

    private Uniforme buscarUniformeOuFalhar(UUID id) {
        return uniformeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Uniforme não encontrado com o ID: " + id));
    }

    private TipoUniforme buscarTipoUniformeOuFalhar(UUID id) {
        return tipoUniformeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de uniforme não encontrado com o ID: " + id));
    }

    private ResponseUniformeDTO toResponseDTO(Uniforme uniforme) {
        return new ResponseUniformeDTO(
                uniforme.getId(),
                uniforme.getTipoUniforme().getId(),
                uniforme.getTipoUniforme().getTipo(),
                uniforme.getTamanho(),
                uniforme.getQuantidade(),
                uniforme.getSexo(),
                uniforme.getDevolvido()
        );
    }
}