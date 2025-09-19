package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.time.LocalDateTime;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    @Modifying
    @Query("update Usuario u set u.failedLoginAttempts = ?1 where u.username = ?2")
    void updateFailedLoginAttempts(int failedLoginAttempts, String username);

    @Modifying
    @Query("update Usuario u set u.accountLockedUntil = ?1 where u.username = ?2")
    void updateAccountLockedUntil(LocalDateTime accountLockedUntil, String username);
}