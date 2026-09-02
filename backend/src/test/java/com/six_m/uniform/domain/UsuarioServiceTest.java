package com.six_m.uniform.domain;

import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.domain.usuario.UsuarioRepository;
import com.six_m.uniform.domain.usuario.UsuarioService;
import com.six_m.uniform.domain.usuario.dto.RequestTrocarSenhaDTO;
import com.six_m.uniform.exception.NotFoundException;
import com.six_m.uniform.shared.dto.MessageResponseDTO;
import com.six_m.uniform.domain.usuario.dto.RequestAtualizarUsuarioDTO;
import com.six_m.uniform.domain.usuario.dto.RequestRegistrarUsuarioDTO;
import com.six_m.uniform.domain.usuario.dto.ResponseUsuarioDTO;
import com.six_m.uniform.exception.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

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
        assertEquals("Rafael", response.nome());
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

    @Test
    void deveAtualizarUsuarioComSucesso() {
        RequestAtualizarUsuarioDTO dto = new RequestAtualizarUsuarioDTO("Rafael Atualizado", "novo@teste.com");

        UUID id = UUID.randomUUID();
        Usuario usuarioExistente = Usuario.builder()
                .id(id)
                .nome("Rafael")
                .email("rafael@teste.com")
                .build();

        autenticarComo("rafael@teste.com");

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("rafael@teste.com")).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseUsuarioDTO response = usuarioService.atualizarUsuario(dto);

        assertEquals(id, response.idUsuario());
        assertEquals("Rafael Atualizado", response.nome());
        assertEquals("novo@teste.com", response.email());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExisteAoAtualizar() {
        RequestAtualizarUsuarioDTO dto = new RequestAtualizarUsuarioDTO("Rafael", "existente@teste.com");

        Usuario outroUsuario = Usuario.builder()
                .id(UUID.randomUUID())
                .email("existente@teste.com")
                .build();

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(outroUsuario));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> usuarioService.atualizarUsuario(dto));

        assertEquals("Este email já está em uso", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioAutenticadoNaoExisteAoAtualizar() {
        RequestAtualizarUsuarioDTO dto = new RequestAtualizarUsuarioDTO("Rafael", "novo@teste.com");

        autenticarComo("fantasma@teste.com");

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("fantasma@teste.com")).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> usuarioService.atualizarUsuario(dto));

        assertEquals("Este email não existe", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveTrocarSenhaComSucesso() {
        RequestTrocarSenhaDTO dto = new RequestTrocarSenhaDTO("rafael@teste.com", "novaSenha123");

        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Rafael")
                .email("rafael@teste.com")
                .senha("senha-antiga-criptografada")
                .build();

        when(usuarioRepository.findByEmail("rafael@teste.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("senha-nova-criptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponseDTO resultado = usuarioService.trocarSenha(dto);

        assertEquals("Senha atualizada com sucesso", resultado.message());
    }

    @Test
    void deveCriptografarNovaSenhaAntesDeSalvarAoTrocarSenha() {
        RequestTrocarSenhaDTO dto = new RequestTrocarSenhaDTO("rafael@teste.com", "senha-pura-nova");

        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Rafael")
                .email("rafael@teste.com")
                .senha("senha-antiga-criptografada")
                .build();

        when(usuarioRepository.findByEmail("rafael@teste.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("senha-pura-nova")).thenReturn("senha-nova-encriptada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.trocarSenha(dto);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        assertEquals("senha-nova-encriptada", captor.getValue().getSenha());
        verify(passwordEncoder).encode("senha-pura-nova");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExisteAoTrocarSenha() {
        RequestTrocarSenhaDTO dto = new RequestTrocarSenhaDTO("naoexiste@teste.com", "novaSenha123");

        when(usuarioRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> usuarioService.trocarSenha(dto));

        assertEquals("Usuário não encontrado com o email informado", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deveDeletarUsuarioComSucesso() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .id(id)
                .nome("Rafael")
                .email("rafael@teste.com")
                .build();

        autenticarComo("rafael@teste.com");

        when(usuarioRepository.findByEmail("rafael@teste.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponseDTO resultado = usuarioService.deletarUsuario();

        assertEquals("Usuário deletado com sucesso", resultado.message());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(captor.capture());
        assertTrue(captor.getValue().getEmail().contains("|_deleted_|" + id));

        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioAutenticadoNaoExisteAoDeletar() {
        autenticarComo("fantasma@teste.com");

        when(usuarioRepository.findByEmail("fantasma@teste.com")).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> usuarioService.deletarUsuario());

        assertEquals("Este email não existe", exception.getMessage());
        verify(usuarioRepository, never()).saveAndFlush(any());
        verify(usuarioRepository, never()).delete(any());
    }

    private void autenticarComo(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }
}