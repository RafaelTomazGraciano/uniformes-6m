package com.six_m.uniform.domain.usuario;

import com.six_m.uniform.domain.usuario.dto.RequestRegistrarUsuarioDTO;
import com.six_m.uniform.domain.usuario.dto.ResponseUsuarioDTO;
import com.six_m.uniform.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
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
        if(usuarioRepository.findByEmail(dto.email()).isPresent()){
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

}
