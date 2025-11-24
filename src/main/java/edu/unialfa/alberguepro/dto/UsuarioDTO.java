package edu.unialfa.alberguepro.dto;

import edu.unialfa.alberguepro.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class UsuarioDTO {

    private Long id;
    
    @NotBlank(message = "O nome de usuário é obrigatório.")
    @Size(min = 4, max = 50, message = "O nome de usuário deve ter entre 4 e 50 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9_.]*$", message = "O nome de usuário só pode conter letras, números, '_' e '.'.")
    private String username;
    
    private String password;
    
    @NotBlank(message = "A função (role) do usuário é obrigatória.")
    private String role;
    
    private boolean ativo;
    private Integer failedLoginAttempts;
    private LocalDateTime accountLockedUntil;
    private LocalDateTime dataCriacao;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.role = usuario.getRole();
        this.ativo = usuario.isAtivo();
        this.failedLoginAttempts = usuario.getFailedLoginAttempts();
        this.accountLockedUntil = usuario.getAccountLockedUntil();
        this.dataCriacao = usuario.getDataCriacao();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getAccountLockedUntil() {
        return accountLockedUntil;
    }

    public void setAccountLockedUntil(LocalDateTime accountLockedUntil) {
        this.accountLockedUntil = accountLockedUntil;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
