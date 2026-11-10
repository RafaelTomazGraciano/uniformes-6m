package com.six_m.uniform.domain.usuario;

import com.six_m.uniform.domain.usuario.dto.*;
import com.six_m.uniform.exception.BadRequestException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        String emailAutenticado = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new BadRequestException("Este email não existe"));

        // Manter o próprio email é permitido; só bloqueia se ele pertence a outro usuário.
        UUID idUsuario = usuario.getId();
        boolean emailDeOutroUsuario = usuarioRepository.findByEmail(dto.email())
                .filter(existente -> !existente.getId().equals(idUsuario))
                .isPresent();
        if (emailDeOutroUsuario) {
            throw new BadRequestException("Este email já está em uso");
        }

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());

        usuario = usuarioRepository.save(usuario);

        return new ResponseUsuarioDTO(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    @Transactional
    public MessageResponseDTO trocarSenha(RequestTrocarSenhaDTO dto) {
        // Mesma mensagem para email inexistente e senha errada: não revela quais emails existem.
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .filter(encontrado -> passwordEncoder.matches(dto.senhaAtual(), encontrado.getSenha()))
                .orElseThrow(() -> new BadRequestException("Senha atual incorreta"));

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);

        return new MessageResponseDTO("Senha atualizada com sucesso");
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
