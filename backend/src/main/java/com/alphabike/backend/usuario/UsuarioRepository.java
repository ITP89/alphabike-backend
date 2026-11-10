package com.alphabike.backend.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u where u.email = :email")
    Optional<Usuario> findByEmailForUpdate(@org.springframework.data.repository.query.Param("email") String email);
    boolean existsByEmail(String email);
    Optional<Usuario> findByTokenVerificacionEmail(String tokenVerificacionEmail);
    Optional<Usuario> findByTokenRecuperacionPassword(String tokenRecuperacionPassword);
}