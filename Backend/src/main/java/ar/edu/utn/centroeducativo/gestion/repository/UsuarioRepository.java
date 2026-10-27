package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Usuario;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByDocente_Id(Long docenteId);

    @Query("select u.docente.id from Usuario u where u.docente is not null")
    Set<Long> findDocenteIdsConUsuario();

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNumeroDocumento(String numeroDocumento);

    long countByRol_NombreAndActivoTrue(String nombre);
}
