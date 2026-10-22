package ar.edu.utn.centroeducativo.gestion.service;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.utn.centroeducativo.gestion.web.dto.DocenteUpdateRequest;
import org.junit.jupiter.api.Test;

class DocenteValidadorTest {

    @Test
    void datosValidosNoGeneranErrores() {
        assertThat(DocenteValidador.validar(valido())).isEmpty();
    }

    @Test
    void emailMalFormateadoDaMensajeEspecifico() {
        var errores = DocenteValidador.validar(conCambio(r -> new DocenteUpdateRequest(
                r.dni(), r.nombre(), r.apellido(), r.especialidad(), "juan-sin-arroba", r.telefono(), r.estado())));

        assertThat(errores).containsEntry("email", "Email inválido. Use formato: ejemplo@dominio.com");
    }

    @Test
    void telefonoDebeTenerDiezDigitos() {
        var errores = DocenteValidador.validar(conCambio(r -> new DocenteUpdateRequest(
                r.dni(), r.nombre(), r.apellido(), r.especialidad(), r.email(), "362412", r.estado())));

        assertThat(errores).containsKey("telefono");
    }

    @Test
    void nombreNoAdmiteCaracteresEspeciales() {
        var errores = DocenteValidador.validar(conCambio(r -> new DocenteUpdateRequest(
                r.dni(), "Juan#1", r.apellido(), r.especialidad(), r.email(), r.telefono(), r.estado())));

        assertThat(errores).containsKey("nombre");
    }

    @Test
    void especialidadFueraDeListaEsInvalida() {
        var errores = DocenteValidador.validar(conCambio(r -> new DocenteUpdateRequest(
                r.dni(), r.nombre(), r.apellido(), "Filosofía", r.email(), r.telefono(), r.estado())));

        assertThat(errores).containsKey("especialidad");
    }

    @Test
    void estadoDebeSerActivoInactivoOLicencia() {
        var errores = DocenteValidador.validar(conCambio(r -> new DocenteUpdateRequest(
                r.dni(), r.nombre(), r.apellido(), r.especialidad(), r.email(), r.telefono(), "JUBILADO")));

        assertThat(errores).containsKey("estado");
    }

    @Test
    void camposVaciosSeInformanTodos() {
        var errores = DocenteValidador.validar(new DocenteUpdateRequest(
                "", " ", null, null, "", "", "NADA"));

        assertThat(errores).containsKeys("dni", "nombre", "apellido", "especialidad", "email", "telefono", "estado");
    }

    private DocenteUpdateRequest valido() {
        return new DocenteUpdateRequest("12345678", "Juan", "Rodríguez", "Matemática",
                "juan@centro.local", "3624123456", "ACTIVO");
    }

    private DocenteUpdateRequest conCambio(java.util.function.UnaryOperator<DocenteUpdateRequest> cambio) {
        return cambio.apply(valido());
    }
}
