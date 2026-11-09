# Bitácora de uso de IA — Parte 3

Se registra cada interacción con IA durante el desarrollo, con lo que pasó realmente: prompt, respuesta, si funcionó,
qué hubo que cambiar y cómo terminó. La IA propone; el equipo analiza, verifica y decide.

Herramienta usada hasta ahora: Claude Code (Claude Sonnet 5.5), dentro del repositorio.
Esta planilla se presenta el 17/11/2026, en la 3.ª instancia de evaluación.

Valores de "¿Funcionó?": Sí, Parcialmente, No. Valores de "Resultado": Correcto, Corregido, Reemplazado.

| Nº | Problema | Prompt utilizado | Herramienta de IA | Respuesta de IA | ¿Funcionó? | Modificaciones realizadas | Resultado |
|---|---|---|---|---|---|---|---|
| 1 | Organizar la Parte 3 a partir del enunciado | "Vamos a pasar a la parte 3 del TP, organiza y estructura todo el trabajo, y luego iremos realizando paso a paso..." | Claude Code (Claude Sonnet 5.5) | Revisó el repositorio y planteó cuatro decisiones. Armó un plan por fases con modelo de datos, API, cronograma, riesgos y trazabilidad | Sí | El equipo eligió Expo, emulador Android, Resend y cuota única. Se corrigió una frase ambigua de los supuestos | Correcto |
| 2 | Leer el PDF del enunciado | El mismo prompt, con el PDF adjunto | Claude Code (Claude Sonnet 5.5) | La herramienta de lectura de PDF no funcionó en esta PC por falta de un conversor. Instaló una librería de extracción de texto y leyó el enunciado completo | Parcialmente | El primer intento falló y se rehízo en otra carpeta. No hubo que corregir el contenido leído | Reemplazado |
| 3 | Fase 1: modelo de precios, permisos y API para que el administrador fije valores | "Sí, empezá con la fase 1 y pusheá la rama" | Claude Code (Claude Sonnet 5.5) | Leyó el código existente para copiar sus convenciones. Creó la migración V18, entidad, servicio con validaciones y auditoría, controlador, regla de seguridad y 8 pruebas de API | Sí | Sin cambios al diseño. Las 8 pruebas pasaron | Correcto |
| 4 | Un comando con comillas anidadas falló y no creó ningún archivo | Parte del mismo pedido de la fase 1 | Claude Code (Claude Sonnet 5.5) | El comando era inválido para el intérprete de comandos. Se reescribió usando las herramientas de escritura de archivos | Parcialmente | Se rehicieron los archivos del servicio, el controlador y la regla de seguridad | Corregido |
| 5 | Una prueba anterior fallaba por una copia vieja de V17 en la carpeta de compilación | Parte del mismo pedido de la fase 1 | Claude Code (Claude Sonnet 5.5) | Diagnosticó que Maven conservaba V17 en target. Repitió con una compilación limpia y una base nueva para reproducir el entorno de producción | Sí | Se confirmó que las 4 fallas restantes ya existían antes de la Parte 3 y que la migración V18 se aplica sin V17 | Correcto |
