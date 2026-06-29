package com.six_m.uniform.domain;

import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.domain.usuario.UsuarioRepository;
import com.six_m.uniform.domain.usuario.UsuarioService;
import com.six_m.uniform.domain.usuario.dto.RequestRegistrarUsuarioDTO;
import com.six_m.uniform.domain.usuario.dto.ResponseUsuarioDTO;
import com.six_m.uniform.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveRegistrarUsuarioComSucesso() {
        RequestRegistrarUsuarioDTO dto = new RequestRegistrarUsuarioDTO(
                "Rafael", "rafael@teste.com", "123456"
        );

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.senha())).thenReturn("senha-criptografada");

        UUID idGerado = UUID.randomUUID();
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioSalvo = invocation.getArgument(0);
            usuarioSalvo.setId(idGerado);
            return usuarioSalvo;
        });

        ResponseUsuarioDTO response = usuarioService.registrarUsuario(dto);

        assertEquals(idGerado, response.idUsuario());
        assertEquals("Rafael", response.name());
        assertEquals("rafael@teste.com", response.email());
    }

    @Test
    void deveCriptografarSenhaAntesDeSalvar() {
        RequestRegistrarUsuarioDTO dto = new RequestRegistrarUsuarioDTO(
                "Rafael", "rafael@teste.com", "senha-pura"
        );

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha-pura")).thenReturn("senha-encriptada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.registrarUsuario(dto);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        assertEquals("senha-encriptada", captor.getValue().getSenha());
        verify(passwordEncoder).encode("senha-pura");
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExiste() {
        RequestRegistrarUsuarioDTO dto = new RequestRegistrarUsuarioDTO(
                "Rafael", "rafael@teste.com", "123456"
        );

        Usuario usuarioExistente = Usuario.builder()
                .id(UUID.randomUUID())
                .email("rafael@teste.com")
                .build();

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(usuarioExistente));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> usuarioService.registrarUsuario(dto));

        assertEquals("Este email já está em uso", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }
}