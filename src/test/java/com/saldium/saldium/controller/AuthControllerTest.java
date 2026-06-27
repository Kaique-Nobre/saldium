package com.saldium.saldium.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saldium.saldium.dto.email.ResendVerificationEmailRequestDTO;
import com.saldium.saldium.exceptions.BadRequestException;
import com.saldium.saldium.exceptions.auth.BadCredentialsException;
import com.saldium.saldium.exceptions.auth.EmailJaRegistradoException;
import com.saldium.saldium.exceptions.auth.TokenInvalidoException;
import com.saldium.saldium.security.auth.AuthController;
import com.saldium.saldium.security.auth.AuthService;
import com.saldium.saldium.security.auth.dto.AlterarSenhaRequestDTO;
import com.saldium.saldium.security.auth.dto.CadastroDTO;
import com.saldium.saldium.security.auth.dto.LoginRequestDTO;
import com.saldium.saldium.security.auth.dto.LoginResponseDTO;
import com.saldium.saldium.security.jwt.JwtAuthenticationFilter;
import com.saldium.saldium.security.jwt.JwtService;
import com.saldium.saldium.security.passwordResetToken.ForgotPasswordRequestDTO;
import com.saldium.saldium.security.passwordResetToken.ResetPasswordRequestDTO;
import com.saldium.saldium.security.refreshToken.RefreshTokenRequestDTO;
import com.saldium.saldium.security.refreshToken.RefreshTokenResponseDTO;
import com.saldium.saldium.security.verificationToken.VerificationTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.saldium.saldium.util.auth.CadastroCreator.criarCadastroDTO;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private VerificationTokenService verificationTokenService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void cadastrar_ShouldSaveUser_WhenSuccessfully()  throws Exception {
        CadastroDTO request = criarCadastroDTO();

        doNothing().when(authService).cadastrar(request);

        mockMvc.perform(post("/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void cadastrar_ShouldReturnConflict_WhenEmailAlreadyRegistered() throws Exception {
        CadastroDTO request = criarCadastroDTO();

        doThrow(new EmailJaRegistradoException("Email ja cadastrado")).when(authService).cadastrar(request);

        mockMvc.perform(post("/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email ja cadastrado"));;
    }

    @Test
    void cadastrar_ShouldReturnBadRequest_WhenCadastroIsInvalid() throws Exception {
        CadastroDTO request = new CadastroDTO("", "", "");

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(authService, never()).cadastrar(any(CadastroDTO.class));
    }

    @Test
    void login_ShouldReturnToken_WhenSuccessfully()  throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("usuario@email.com", "senha123");
        LoginResponseDTO response = new LoginResponseDTO("jwt-accessToken", "jwt-refreshToken");

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("jwt-refreshToken"));
        verify(authService).login(any(LoginRequestDTO.class));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("wrong@email.com", "wrong-password123");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Email ou senha estão incorretos"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou senha estão incorretos"));
    }

    @Test
    void login_ShouldReturnBadRequest_WhenCredentialsBlank() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
           verify(authService, never()).login(any(LoginRequestDTO.class));
    }

    @Test
    void refreshToken_ShouldReturnToken_WhenSuccessfully()  throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("jwt-refreshToken");
        RefreshTokenResponseDTO response = new RefreshTokenResponseDTO("new-accessToken");

        when(authService.refreshToken(any(RefreshTokenRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-accessToken"));
        verify(authService).refreshToken(any(RefreshTokenRequestDTO.class));
    }

    @Test
    void refreshToken_ShouldReturnUnauthorized_WhenUserNotFound() throws Exception {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("jwt-refreshToken");

        when(authService.refreshToken(any(RefreshTokenRequestDTO.class)))
                .thenThrow(new TokenInvalidoException("Token invalido ou expirado"));

        mockMvc.perform(post("/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token invalido ou expirado"));
    }

    @Test
    void alterarSenha_ShouldReturnOk_WhenSuccessfully()  throws Exception {
        AlterarSenhaRequestDTO request =
                new AlterarSenhaRequestDTO("senha123", "nova-senha", "nova-senha");

        doNothing().when(authService).alterarSenha(any(AlterarSenhaRequestDTO.class));

        mockMvc.perform(patch("/auth/alterar-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void alterarSenha_ShouldReturnUnauthorized_WhenSenhaIsIncorrect() throws Exception {
        AlterarSenhaRequestDTO request =
                new AlterarSenhaRequestDTO( "wrong-password", "nova-senha", "nova-senha");

        doThrow(new BadCredentialsException("Senha incorreta")).when(authService).alterarSenha(any(AlterarSenhaRequestDTO.class));

        mockMvc.perform(patch("/auth/alterar-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Senha incorreta"));
    }

    @Test
    void verifyEmail_ShouldReturnOk_WhenSuccessfully()  throws Exception {
        doNothing().when(verificationTokenService).verifyEmail(anyString());

        mockMvc.perform(get("/auth/verify-email?token=validateToken"))
                .andExpect(status().isFound());

        verify(verificationTokenService).verifyEmail(anyString());
    }

    @Test
    void verifyEmail_ShouldReturnUnauthorized_WhenTokenIsInvalidOrExpired()  throws Exception {
        doThrow(new TokenInvalidoException("Token invalido ou expirado")).when(verificationTokenService).verifyEmail(anyString());

        mockMvc.perform(get("/auth/verify-email?token=invalideToken"))
                .andExpect(status().isUnauthorized());

        verify(verificationTokenService).verifyEmail(anyString());
    }

    @Test
    void resendVerificationEmail_ShouldReturnNoContent_WhenSuccessfully()  throws Exception {
        ResendVerificationEmailRequestDTO request =  new ResendVerificationEmailRequestDTO("user@email.com");

        doNothing().when(authService).resendVerificationEmail(anyString());

        mockMvc.perform(post("/auth/resend-verification-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).resendVerificationEmail(anyString());
    }

    @Test
    void resendVerificationEmail_ShouldBadRequest_WhenEmailAlreadyVerified()  throws Exception {
        ResendVerificationEmailRequestDTO request =  new ResendVerificationEmailRequestDTO("user@email.com");

        doThrow(new BadRequestException("Bad Request")).when(authService).resendVerificationEmail(anyString());

        mockMvc.perform(post("/auth/resend-verification-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void forgotPassword_ShouldReturnNoContent_WhenSuccessfully()  throws Exception {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO("user@email.com");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void resetPassword_ShouldReturnNoContent_WhenSuccessfully() throws Exception {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("novaSenha", "novaSenha");

        mockMvc.perform(post("/auth/reset-password?token=reset-password-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void resetPassword_ShouldReturnUnauthorized_WhenTokenIsInvalidOrExpired()  throws Exception {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("novaSenha", "novaSenha");

        doThrow(new TokenInvalidoException("Token inválido")).when(authService).resetPassword("invalidToken", request);

        mockMvc.perform(post("/auth/reset-password?token=invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Token Inválido"));
    }

    @Test
    void resetPassword_ShouldReturnUnauthorized_WhenNovaSenhaIsWrong()  throws Exception {
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO("novaSenha", "1234");

        doThrow(new BadCredentialsException("Bad Credentials")).when(authService).resetPassword("reset-password-token", request);

        mockMvc.perform(post("/auth/reset-password?token=reset-password-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Bad Credentials"));
    }
}
