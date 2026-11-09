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
