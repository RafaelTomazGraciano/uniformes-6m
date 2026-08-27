package com.six_m.uniform.domain.uniforme;

import com.six_m.uniform.domain.tipoUniforme.TipoUniformeRepository;
import com.six_m.uniform.domain.uniforme.dto.ResponseUniformeDTO;
import com.six_m.uniform.exception.NotFoundException;
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

    @Transactional(readOnly = true)
    public Page<ResponseUniformeDTO> buscarTodosUniformes(Pageable pageable) {
        return uniformeRepository.findAll(pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ResponseUniformeDTO buscarUniforme(UUID id) {
        return toResponseDTO(buscarUniformeOuFalhar(id));
    }

    private Uniforme buscarUniformeOuFalhar(UUID id) {
        return uniformeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Uniforme não encontrado com o ID: " + id));
    }

    private ResponseUniformeDTO toResponseDTO(Uniforme uniforme) {
        return new ResponseUniformeDTO(
                uniforme.getId(),
                uniforme.getTipoUniforme().getId(),
                uniforme.getTipoUniforme().getTipo(),
                uniforme.getTamanho(),
                uniforme.getQuantidade(),
                uniforme.getSexo()
        );
    }
}