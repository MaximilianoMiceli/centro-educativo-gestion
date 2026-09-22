package ar.edu.utn.centroeducativo.gestion.repository;

import ar.edu.utn.centroeducativo.gestion.domain.Alumno;
import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;

public final class AlumnoSpecs {

    private AlumnoSpecs() {
    }

    public static Specification<Alumno> legajoIgual(String legajo) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("legajo")), legajo.toLowerCase());
    }

    public static Specification<Alumno> dniIgual(String dni) {
        return (root, query, cb) -> cb.equal(root.get("dni"), dni);
    }

    public static Specification<Alumno> nombreContiene(String texto) {
        return (root, query, cb) -> {
            String patron = "%" + texto.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nombre")), patron),
                    cb.like(cb.lower(root.get("apellido")), patron),
                    cb.like(cb.lower(cb.concat(cb.concat(root.get("nombre"), " "), root.get("apellido"))), patron));
        };
    }

    public static Specification<Alumno> cursoEn(Collection<Long> cursoIds) {
        return (root, query, cb) -> cursoIds.isEmpty()
                ? cb.disjunction()
                : root.get("curso").get("id").in(cursoIds);
    }
}
