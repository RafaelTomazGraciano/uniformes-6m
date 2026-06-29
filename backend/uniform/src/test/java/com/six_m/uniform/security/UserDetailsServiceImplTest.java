package com.six_m.uniform.security;

import com.six_m.uniform.domain.usuario.Usuario;
import com.six_m.uniform.domain.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    void deveCarregarUsuarioPorEmail(){
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Teste")
                .email("teste@teste.com")
                .senha("senha-criptografada")
                .build();

        when(usuarioRepository.findByEmail("teste@teste.com"))
                .thenReturn(Optional.of(usuario));


        UserDetails result = userDetailsServiceImpl.loadUserByUsername("teste@teste.com");

        assertEquals("teste@teste.com", result.getUsername());
        assertEquals("senha-criptografada", result.getPassword());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findByEmail("naoexiste@teste.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsServiceImpl.loadUserByUsername("naoexiste@teste.com"));
    }

}
