package com.saldium.saldium.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saldium.saldium.dto.user.UserResponseDTO;
import com.saldium.saldium.exceptions.BadRequestException;
import com.saldium.saldium.security.auth.dto.DeletarContaRequestDTO;
import com.saldium.saldium.security.jwt.JwtAuthenticationFilter;
import com.saldium.saldium.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void deletarUsuario_ShouldReturnNoContent_WhenSuccessfully() throws Exception {
        DeletarContaRequestDTO request = new DeletarContaRequestDTO("senhaUsuario");
        doNothing().when(userService).deletarUsuario(request);

        mockMvc.perform(delete("/user")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletarUsuario_ShouldReturnBadRequest_WhenPasswordIsWrong() throws Exception {
        DeletarContaRequestDTO request = new DeletarContaRequestDTO("senhaErrada");
        doThrow(new BadRequestException("Senha incorreta")).when(userService).deletarUsuario(request);

        mockMvc.perform(delete("/user")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void getUserInfo_ShouldReturnUserInfo_WhenSuccessfully() throws Exception {
        UserResponseDTO response = new UserResponseDTO("usuario", "usuario@email.com", LocalDate.now());

        when(userService.getUserInfo()).thenReturn(response);

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk());
    }
}
