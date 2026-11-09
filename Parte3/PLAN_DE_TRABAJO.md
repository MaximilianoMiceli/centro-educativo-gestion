# Parte 3: Aplicación móvil integrada con IA — Plan de trabajo

Fechas clave: app terminada y lista para la actividad de testing el **11/11/2026**; informe y bitácora de IA el **17/11/2026**.
Última actualización: 09/11/2026.

## 1. Objetivo

Ampliar el sistema de gestión de "Educar Para Transformar" con una aplicación móvil para padres que centraliza
cuotas, comedor, deportes y transporte, y con las funciones web y de backend que la sostienen. La app reutiliza el
backend y la base de datos existentes: no se duplican datos.

Regla de trabajo: **solo se agrega lo de la Parte 3**. El código del sistema de gestión ya entregado no se modifica,
salvo los puntos de enganche indispensables (nuevos menús y rutas), que se listan en la sección 9.

## 2. Decisiones tomadas

| Tema | Decisión | Motivo |
|---|---|---|
| App móvil | React Native con Expo (TypeScript) | Mismo lenguaje que el frontend actual; llega al 11/11 |
| Demostración | Emulador Android | Elegido por el equipo |
| Correos | API HTTP de Resend, con modo demostración sin envío | Render gratis bloquea SMTP |
| Cuota base | Un valor único para todos los alumnos | Alcanza para el enunciado y es simple |
| Comprobantes de pago | Se guardan en PostgreSQL (bytea, hasta 5 MB) | El disco de Render gratis es efímero |
| Pagos | Solo transferencia bancaria: el padre carga el comprobante | Regla de negocio del enunciado |
| Contraseña olvidada | No disponible | El sistema actual no tiene recuperación por correo; se documenta |

Supuestos a confirmar con la cátedra:
- Las facturas del mes siguiente se emiten el último día hábil de cada mes, junto con el recordatorio.
- El vencimiento es el día 20 del mes de la cuota, y ese día se envía el aviso de deuda si el pago no está completo.
- Se emite una factura por alumno y período, y el correo al padre suma las de todos sus hijos.

## 3. Estructura del repositorio

```
centro-educativo-gestion/
├── Backend/            sistema existente + paquete nuevo de cuotas, pagos y correos (migraciones V18 en adelante)
├── Frontend/           sistema web existente + pantallas nuevas de finanzas
├── Movil/              aplicación móvil nueva (Expo)
└── Parte3/             documentación de esta parte
    ├── PLAN_DE_TRABAJO.md
    ├── bitacora-ia.md
    └── informe/INFORME.md
```

Ramas: `parte3/app-movil` es la rama de trabajo, creada desde `main`. Los cambios se hacen en commits chicos y por fase.

## 4. Modelo de datos nuevo

Todo entra como migraciones Flyway nuevas (V18 en adelante). No se altera ninguna tabla existente.

| Tabla | Para qué sirve |
|---|---|
| `precio` | Valor vigente de la cuota base, de cada deporte, de cada recorrido y del comedor |
| `factura` | Una por alumno y período (AAAA-MM): total, vencimiento, estado (pendiente, parcial, pagada) |
| `factura_item` | Detalle de la factura: concepto, descripción e importe. Es la composición del total |
| `pago` | Pago informado por el padre: importe, fecha, número de comprobante emitido y archivo de la transferencia |
| `pago_item` | Qué ítems de la factura cubre cada pago |
| `envio_correo` | Registro de cada correo (tipo, padre, período, estado). Evita enviar dos veces |

Los importes se copian a la factura al emitirla, así un cambio de precio no altera facturas ya emitidas.
La factura se arma con las inscripciones vigentes del alumno (deportes, recorrido, comedor), que ya existen.

## 5. API nueva (resumen)

Administración (permiso nuevo `FINANZAS_ADMINISTRAR`, rol Admin):
- `GET` y `PUT /api/v1/precios`: ver y cambiar los valores.
- `POST /api/v1/facturacion/generar?periodo=AAAA-MM`: genera las facturas del período, sin duplicar.
- `POST /api/v1/correos/ejecutar?tipo=RECORDATORIO|DEUDA`: dispara el envío a mano (para la demostración).

Consulta (permiso nuevo `FINANZAS_CONSULTAR`, roles Admin y Dirección), con filtro `desde` y `hasta`:
- `/api/v1/finanzas/ingresos`
- `/api/v1/finanzas/pagos-completos` y `/pagos-incompletos`, agrupados por año y alumno
- `/api/v1/finanzas/pagos-por-deporte`: por deporte, nivel, horario y profesor a cargo
- `/api/v1/finanzas/pagos-por-recorrido`

Padres (permiso existente `MI_INFORMACION_CONSULTAR`, solo sus hijos):
- `GET /api/v1/mi-informacion/alumnos/{id}/facturas?desde&hasta`: facturas y comprobantes.
- `GET /api/v1/mi-informacion/alumnos/{id}/deuda`: historial de deuda por ítem.
- `GET /api/v1/mi-informacion/facturas/{id}/pdf`: factura en PDF.
- `POST /api/v1/mi-informacion/facturas/{id}/pagos`: el padre elige ítems y adjunta la transferencia. Responde con el comprobante emitido.
- Las inscripciones a deportes, transporte y comedor ya existen en `/api/v1/mi-informacion/servicios`.

## 6. Aplicación móvil

Pantallas: ingreso, selector de hijo, cuotas (pendientes y pagadas, vencimiento, importe, estado), detalle de factura con
sus comprobantes, carga de comprobante, historial de pagos, historial de deuda por ítem, servicios del hijo
(deportes, transporte, comedor), cambio de contraseña y cierre de sesión.

Comunicación: la app usa la misma API REST y el mismo token JWT que la web. Todo se sincroniza porque hay una sola base.

## 7. Cronograma y prioridades

| Fase | Contenido | Fecha |
|---|---|---|
| 0 | Plan, estructura, rama, bitácora | 09/11 |
| 1 | Backend: precios y modelo de datos | 09/11 |
| 2 | Backend: facturación, pagos, comprobantes y factura en PDF | 09 y 10/11 |
| 3 | App móvil: ingreso, cuotas, facturas, deuda, carga de comprobante, servicios | 10 y 11/11 |
| 4 | Correos programados (último día hábil y día 20) y envío manual | 10/11 |
| 5 | Listados web de finanzas y pantalla de precios | 10 y 11/11 |
| 6 | Pruebas, despliegue del backend y ensayo en el emulador | 11/11 |
| 7 | Informe e IA vs. conocimiento, comparación y reflexión | 12 al 16/11 |
| 8 | Entrega de informe y bitácora | 17/11 |

Prioridad si el tiempo no alcanza:
1. **Imprescindible:** precios, facturación, pagos con comprobante, y la app con ingreso, cuotas, facturas y deuda.
2. **Importante:** correos programados con factura adjunta, y los cinco listados web.
3. **Si hay tiempo:** exportar listados a PDF y Excel, y pruebas end to end de la web.

## 8. Pruebas

- Backend: pruebas de integración con MockMvc para cada endpoint nuevo, incluidos permisos y propiedad (un padre no ve hijos ajenos).
- Facturación y correos: pruebas con un reloj fijo, para verificar último día hábil, día 20 y que no se envíe dos veces.
- App móvil: pruebas de las funciones de cálculo y formato, y recorrido manual completo en el emulador.
- Todo corre en el pipeline de CI existente.

## 9. Puntos de enganche con lo ya hecho

Cambios mínimos sobre código existente, todos aditivos:
- `Frontend/src/utils/navegacion.ts` y `Frontend/src/App.tsx`: nuevos ítems de menú y rutas para finanzas.
- `Backend/.../config/SecurityConfig.java`: reglas de autorización para las rutas nuevas.
- Migración que crea los permisos `FINANZAS_ADMINISTRAR` y `FINANZAS_CONSULTAR` y los asigna a los roles.

## 10. Riesgos

| Riesgo | Mitigación |
|---|---|
| Render gratis duerme el servicio y puede no correr el programador de tareas | Disparo manual y recomendación de monitor para mantenerlo despierto |
| Resend gratis solo envía al correo del dueño de la cuenta | Alcanza para la demostración; el modo sin envío queda como respaldo |
| Instalar el emulador Android en la PC es pesado | Instalarlo cuanto antes y tener a Expo web como respaldo |
| Poco tiempo hasta el 11/11 | Prioridades de la sección 7 |
| La migración V17 (datos de demostración) está sin commitear | Las migraciones nuevas arrancan en V18 y se dejan sin usar el número 17 |

## 11. Trazabilidad con el enunciado

| Requisito | Dónde se resuelve |
|---|---|
| Correo del último día hábil con total, composición y factura | Fase 4, `envio_correo`, factura PDF |
| Correo del día 20 con la deuda | Fase 4 |
| App: facturas y comprobantes por período | Fase 3, `GET .../facturas` |
| App: historial de deuda por ítem | Fase 3, `GET .../deuda` |
| Web: ingresos, pagos completos e incompletos, por deporte y por recorrido | Fase 5, `/finanzas/*` |
| Módulo 1: autenticación | Fase 3, sobre `/auth/*` existente |
| Módulo 2: cuotas, vencimientos, importe, estado, historial | Fases 2 y 3 |
| Solo el admin fija valores | `FINANZAS_ADMINISTRAR` |
| El padre elige ítems y recibe comprobante | Fase 2, `pago` y `pago_item` |
| Una factura con varios comprobantes | Fase 2, `pago` con relación 1 a N |
| Transporte opcional mes a mes | La factura lee la inscripción vigente al emitirse |
| Bitácora de IA e informe | `bitacora-ia.md` e `informe/INFORME.md` |
