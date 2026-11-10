# Parte 3: Informe de la aplicación móvil integrada con IA

Equipo: Miceli Maximiliano
Materia: Metodología de Sistemas II
Entrega: 17/11/2026

Este informe describe cómo se integró una aplicación móvil al sistema de gestión del Centro Educativo "Educar Para Transformar", y cómo se usó una IA (Claude Code) durante el desarrollo. Los datos salen del repositorio, de las pruebas y de la bitácora (`Parte3/bitacora-ia.md`, 14 entradas). Donde algo no se pudo comprobar, se dice.

## 1. Relación entre la aplicación web y la aplicación móvil

### 1.1 Funcionalidades que ya existían en la web

- Ingreso con usuario y contraseña, roles y permisos guardados en la base de datos, y cambio de contraseña.
- Gestión de alumnos, docentes, niveles, cursos, materias, horarios y calificaciones.
- Inscripciones a deportes, transporte (recorridos) y comedor.
- La pantalla "Mi información" para los padres, que muestra a sus hijos.
- Auditoría automática de cambios.

### 1.2 Funcionalidades incorporadas en la Parte 3

En la aplicación móvil (para padres):
- Ingreso, cambio de contraseña y perfil.
- Lista de cuotas por mes y por hijo, con su estado (pendiente, parcial, pagada).
- Detalle de cada factura y descarga del PDF.
- Informar un pago: se elige qué ítems se pagaron, se adjunta el comprobante y se registra.
- Historial de deuda y servicios contratados de cada hijo.

En la aplicación web (para administración):
- Pantalla de precios: cuota base, comedor, cada deporte y cada recorrido.
- Emisión de la facturación mensual.
- Cinco listados: ingresos, pagos completos, pagos incompletos, pagos por deporte y pagos por recorrido.
- Pantalla de correos a los padres, con el texto y las facturas adjuntas.

En el servidor, sin pantalla: correos mensuales programados (recordatorio el último día hábil del mes y aviso de deuda el día 20).

### 1.3 Información que comparten

Las dos aplicaciones usan la misma base de datos PostgreSQL y el mismo servidor. Comparten alumnos, padres, inscripciones a servicios, usuarios, roles y permisos. Lo nuevo (precios, facturas, ítems, pagos, comprobantes, correos) también vive ahí, así que una factura emitida desde la web aparece en el celular del padre sin ningún paso intermedio.

### 1.4 Cómo se comunican

La aplicación móvil (Expo con React Native y TypeScript) consume la misma API REST del servidor (`/api/v1`) por HTTPS, con el mismo esquema de autenticación que la web: un token JWT. El token se guarda en el almacenamiento seguro del teléfono y se envía en cada pedido. El servidor valida el permiso de cada operación igual que para la web, de modo que el móvil no tiene un camino aparte.

### 1.5 Modificaciones necesarias en el backend

Se hicieron agregados, sin cambiar el comportamiento anterior:
- Cuatro migraciones nuevas: V18 (precios y permisos de finanzas), V19 (facturas, ítems y pagos con comprobante), V20 (registro de correos) y V21 (referencia al deporte o recorrido en cada ítem).
- Servicios y controladores nuevos para precios, facturación, pagos, facturas de la familia, listados de finanzas y correos.
- Permisos nuevos (`FINANZAS_ADMINISTRAR`, `FINANZAS_CONSULTAR`) y reglas en la configuración de seguridad.
- Generación de PDF (factura y comprobante) y una tarea programada diaria.
- Ajustes de configuración: tamaño máximo de archivo subido y modo de correo.

Un padre solo puede ver y pagar facturas de sus propios hijos. Esa regla se controla en el servidor, no en la aplicación.

### 1.6 Información que se reutiliza

Los usuarios y sus roles, la relación padre-hijo, las inscripciones a deporte, transporte y comedor (de ahí salen los ítems de cada factura), los datos de alumnos y la auditoría. Los precios no se leen de las inscripciones al vuelo: se copian a cada factura al emitirla, por lo que cambiar un precio no altera facturas anteriores.

### 1.7 Cómo se mantienen sincronizados

Hay una sola fuente de verdad. La aplicación móvil no tiene base de datos propia ni copia de los datos; solo guarda el token de sesión. Cada pantalla pide los datos al servidor al abrirse. Por eso no hay sincronización que mantener: cualquier cambio hecho desde la web se ve en el móvil en la siguiente consulta. La contrapartida es que la app necesita conexión.

### 1.8 Alcance verificado

- Backend: 163 pruebas automáticas pasan (incluye las de la Parte 3).
- Aplicación móvil: 31 pruebas unitarias y 5 pruebas de integración contra el backend real.
- Frontend web: 59 pruebas.
- La aplicación móvil se vio en el navegador con la versión web de Expo. **No se probó en un emulador ni en un teléfono Android**, y el selector de archivos y la descarga de PDF no se pudieron automatizar.
- Los correos funcionan en modo "sin envío": se preparan y se guardan, pero no salen. El envío real (Resend) queda pendiente.
- Las migraciones V18 a V21 todavía no están desplegadas en producción.

## 2. Registro de uso de la IA

La bitácora completa está en `Parte3/bitacora-ia.md`, con 14 registros: problema, prompt, respuesta, si funcionó, modificaciones y resultado. Se usó una sola herramienta, Claude Code (Claude Sonnet 5.5), dentro del repositorio.

Resumen de los 14 registros:

| Resultado | Cantidad |
|---|---|
| Correcto | 5 |
| Corregido | 6 |
| Reemplazado | 3 |

Los registros 4, 5, 6, 10 y 11 documentan errores o fallos reales. Se dejaron en la bitácora a propósito.

## 3. IA vs. conocimiento del sistema

Una IA puede dar una solución que compila y funciona en general, pero que no sirve para este sistema. La razón es que no conoce lo que no está escrito en el código ni en lo que se le dice: las decisiones del equipo, las reglas de la institución y el estado real del entorno. Ejemplos de este trabajo:

- **Contexto y decisiones del equipo (registro 6).** El archivo `V17__datos_demo_navegables.sql` estaba sin versionar a propósito. La IA usó `git add` sobre una carpeta entera y lo incluyó en un commit. Técnicamente era un commit válido, pero contradecía una decisión del equipo que no estaba en ningún documento. Se corrigió con un commit nuevo y desde entonces se agregan solo archivos con ruta explícita.
- **Estado del entorno (registro 5).** Una prueba fallaba porque Maven conservaba una copia vieja de V17 en `target`. El código era correcto; el entorno no. Hizo falta entender cómo se construye el proyecto para diagnosticarlo.
- **Base de datos y arquitectura (registro 8).** El plan inicial preveía una tabla `pago_item` para relacionar pagos e ítems. Al implementar, se vio que era mejor que cada ítem guarde el pago que lo cubrió, porque un ítem se paga una sola vez. Además, antes de probar se corrigieron dos riesgos propios del sistema: el período guardado como CHAR y la carga de dos listas a la vez en JPA, que la configuración con `ddl validate` y `open-in-view` desactivado volvía problemática.
- **Reglas de negocio (registros 13 y 14).** La IA propuso un calendario con solo feriados de fecha fija, envío a las 9:00 y facturas del mes siguiente emitidas con el recordatorio. Son supuestos razonables, pero no son reglas confirmadas por la institución, y quedaron anotados como pendientes de validar. En la fase 5, la relación entre un ítem y su deporte o recorrido se resolvía al principio comparando texto; se cambió por una referencia real (V21), porque comparar texto se rompe si alguien renombra un deporte.
- **Tecnologías y permisos.** La IA asumió que `@expo/vector-icons` venía incluido en Expo (registro 11) y que el entorno de pruebas de Expo permitía llamar al servidor real (registro 10). Ninguna de las dos cosas era cierta. Las detectó el verificador de tipos y la ejecución real contra el backend, no la propia IA.
- **Límites de lo que puede hacer (registro 7).** Se pidió mergear un PR; la IA no tiene credenciales ni puede saltear la protección de la rama `main`. Lo resolvió con un merge local y dejó claro que el merge real lo hace una persona.

En todos los casos, lo que permitió detectar el problema fue conocer el sistema: sus convenciones, su base de datos, su proceso de trabajo y sus reglas. El código generado fue una propuesta que había que verificar.

## 4. Comparación: desarrollo tradicional vs. desarrollo asistido por IA

Es una comparación cualitativa. No se midieron tiempos de un desarrollo sin IA, así que no se afirman cifras de ahorro.

| Aspecto | Sin IA | Con IA |
|---|---|---|
| Generación de código | Se escribe todo a mano, consultando documentación. Mucho código repetitivo (DTO, repositorios, controladores). | Genera rápido estructuras completas siguiendo las convenciones del repositorio que lee. Hay que revisar cada parte. |
| Tiempo de desarrollo | Mayor en tareas repetitivas y en aprender una tecnología nueva (Expo). | En esta parte se completaron cinco fases con backend, web y móvil. Parece menor, aunque no se cuantificó. Parte del tiempo se va en revisar y corregir. |
| Comprensión del código | Se entiende lo que se escribe porque se escribe paso a paso. | Hay que leer código que no se escribió. Entenderlo exige un esfuerzo extra y no se puede omitir. |
| Detección de errores | Se detectan al escribir y probar. | La IA también produce errores propios (registros 6, 10, 11), que se detectan con compilación, pruebas y ejecución real. Sin pruebas, varios habrían pasado. |
| Documentación | Suele postergarse. | Se produjo junto con el código: plan, README y bitácora. Hay que corroborar que lo documentado coincida con lo hecho. |
| Aprendizaje | Se aprende investigando y equivocándose. | Se aprende leyendo y cuestionando las propuestas. Hay riesgo de aprender menos si se acepta sin entender. |
| Integración | Depende de que quien programa conozca todo el sistema. | La IA leyó el código existente y copió convenciones, pero no conocía decisiones del equipo ni el entorno (registros 5 y 6). |
| Seguridad | Depende de la experiencia de cada persona. | Aplicó controles de permisos y de pertenencia (un padre solo ve a sus hijos), pero cada regla se verificó con pruebas. No se debe suponer que sea segura por defecto. |
| Mantenimiento | Código con el estilo de quien lo escribió. | Código consistente y con pruebas, pero la facilidad de generarlo puede llevar a más código del necesario si no se controla. |
| Calidad del resultado | Variable. | Buena cuando hubo revisión y pruebas: 163 pruebas en el backend. Peor sin ellas. |

## 5. Reflexión crítica

1. **¿La utilización de IA redujo realmente el tiempo de desarrollo?**
   Creemos que sí, sobre todo en tareas repetitivas y en una tecnología nueva como Expo, pero no lo medimos. Parte del ahorro se consumió en revisar, probar y corregir. Una afirmación con números exigiría haber hecho el mismo trabajo sin IA.

2. **¿En qué tareas resultó más útil?**
   En estructuras repetitivas (entidades, repositorios, controladores, DTO), en pruebas, en la documentación y en el diagnóstico de fallos concretos (el problema de V17 en `target`, el `fetch` simulado de Jest).

3. **¿En qué tareas produjo errores?**
   En el manejo de Git (registro 6), en comandos de consola con comillas anidadas (registro 4), en dependencias de la aplicación móvil (registros 10 y 11) y en el cuidado del entorno de pruebas (registro 5). También en un primer diseño de datos que luego se cambió (registros 8 y 14).

4. **¿Qué tipo de errores fueron más frecuentes?**
   Suposiciones sobre el entorno y sobre el contexto del equipo: algo que "debería" estar instalado o funcionar de cierta manera, o una decisión que no estaba escrita. Hubo pocos errores de lógica pura. Un caso aparte: una expectativa de prueba equivocada (se esperaban 3 pagos y eran 2) que hubo que corregir en la prueba y no en el código.

5. **¿Fue necesario modificar código generado por IA?**
   Sí. De 14 registros, 6 terminaron "Corregido" y 3 "Reemplazado"; los 5 "Correcto" también se revisaron y probaron antes de aceptarse.

6. **¿La IA comprendió correctamente la base de datos?**
   En gran parte, porque leyó las migraciones y las entidades existentes y siguió sus convenciones. Pero cometió errores de diseño que se vieron al implementar o al probar con datos (el período CHAR, la carga de dos listas a la vez, comparar texto en lugar de una referencia). La validación con Flyway, `ddl validate` y las pruebas contra PostgreSQL fue lo que los detectó.

7. **¿La IA comprendió correctamente las reglas de negocio?**
   Solo las que estaban escritas en el enunciado o en lo que se le dijo. Donde hacía falta criterio de la institución (feriados, hora de envío, cuándo se emiten las facturas, día de vencimiento), tomó supuestos razonables que hay que validar con la cátedra. Esa validación todavía no se hizo.

8. **¿Qué riesgos identificaron?**
   - Aceptar código que se entiende a medias.
   - Cambios que contradicen decisiones no escritas del equipo.
   - Operaciones de Git o de base de datos con efectos no deseados.
   - Seguridad: permisos mal aplicados o datos de otra familia expuestos si no se prueban.
   - Documentación que dice más de lo que se verificó (por ejemplo, afirmar que la app funciona en Android sin haberla probado ahí).
   - Dependencia de un servicio externo (envío de correos, hosting gratuito que se duerme).

9. **¿Qué conocimientos necesitó el equipo para validar las respuestas de IA?**
   Conocer el sistema (arquitectura, base de datos, reglas y permisos), Git, Java y Spring, SQL y migraciones, React y TypeScript, cómo se prueba una API y qué significa cada fallo. Sin eso no habríamos podido distinguir una solución correcta de una que solo lo parece.

10. **¿Consideran que utilizar IA permite reemplazar el conocimiento del programador?**
    No. La IA acelera la escritura, pero no reemplaza el criterio. En este trabajo cada error importante lo detectó una persona o una verificación diseñada por una persona (pruebas, compilación, ejecución real). Además, las decisiones que definen el sistema (qué hacer cuando faltan reglas, qué se versiona, qué se despliega, qué es aceptable) son del equipo. Quien no sabe programar no puede saber si la respuesta es buena, y quien sí sabe la aprovecha mejor.

---

Pendientes antes de entregar: confirmar con la cátedra los supuestos de correos, probar la app en un emulador o teléfono Android, desplegar las migraciones V18 a V21 y completar en la bitácora lo que ocurra hasta la entrega.
