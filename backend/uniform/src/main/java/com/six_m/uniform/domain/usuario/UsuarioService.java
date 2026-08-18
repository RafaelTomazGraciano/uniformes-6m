package com.six_m.uniform.domain.usuario;

import com.six_m.uniform.domain.usuario.dto.*;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseUsuarioDTO registrarUsuario(RequestRegistrarUsuarioDTO dto) throws BadRequestException {
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new BadRequestException("Este email já está em uso");
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha()))
                .build();

        usuario = usuarioRepository.save(usuario);

        return new ResponseUsuarioDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    @Transactional
    public ResponseUsuarioDTO atualizarUsuario(RequestAtualizarUsuarioDTO dto) throws BadRequestException {
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new BadRequestException("Este email já está em uso");
        }

        String emailAutenticado = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new BadRequestException("Este email não existe"));

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());

        usuario = usuarioRepository.save(usuario);

        return new ResponseUsuarioDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    @Transactional
    public MessageResponseDTO deletarUsuario() throws BadRequestException {
        String emailAutenticado = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new BadRequestException("Este email não existe"));

        usuario.setEmail(usuario.getEmail() + "|_deleted_|" + usuario.getId());
        usuarioRepository.saveAndFlush(usuario);

        usuarioRepository.delete(usuario);

        return new MessageResponseDTO("Usuário deletado com sucesso");
    }

}
