package edu.unialfa.alberguepro.repository;

import edu.unialfa.alberguepro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring Data JPA criará automaticamente os métodos básicos de CRUD (Create, Read, Update, Delete)
}