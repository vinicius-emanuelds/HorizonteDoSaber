package com.poo.siga.model;

import com.poo.siga.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @NotBlank(message = "O nome completo é obrigatório")
    @Column(nullable = false)
    private String nomeCompleto;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "O login é obrigatório")
    @Column(unique = true, nullable = false)
    private String login;

    @NotBlank(message = "A senha é obrigatória")
    @Column(nullable = false)
    private String senha;

    @NotNull(message = "O perfil de acesso é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    private LocalDate dataExpiracaoSenha;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private int tentativasLogin = 0;

    @Column(nullable = false)
    private boolean bloqueado = false;

    @Column(nullable = false)
    private boolean primeiroAcesso = true;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @PrePersist
    public void prePersist() {
        if (this.codigo == null) {
            this.codigo = "USR" + String.format("%05d", SEQ.getAndIncrement());
        }
        if (this.dataCadastro == null) {
            this.dataCadastro = LocalDateTime.now();
        }
    }
}
