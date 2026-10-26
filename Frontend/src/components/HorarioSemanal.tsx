import React, { useMemo } from 'react';
import { Box, Typography } from '@mui/material';
import { DIAS_NOMBRE, armarGrilla } from '../utils/horarios';
import type { HorarioGrilla } from '../utils/horarios';
import { PALETA } from '../institucion';

interface HorarioSemanalProps {
  horarios: HorarioGrilla[];
  /** Muestra el docente en cada bloque (útil cuando la grilla es de un curso). */
  mostrarDocente?: boolean;
}

const PX_POR_HORA = 56;
const ANCHO_COLUMNA_HORAS = 56;

/** Grilla semanal: una columna por día y un bloque por clase, ubicado según su hora. */
export const HorarioSemanal: React.FC<HorarioSemanalProps> = ({ horarios, mostrarDocente = false }) => {
  const grilla = useMemo(() => armarGrilla(horarios), [horarios]);
  const alto = (grilla.horaFin - grilla.horaInicio) * PX_POR_HORA;

  if (horarios.length === 0) {
    return <Typography variant="body2" sx={{ color: '#666' }}>No hay horarios cargados.</Typography>;
  }

  return (
    <Box sx={{ overflowX: 'auto', border: '1px solid #e4ecf4', borderRadius: 2, backgroundColor: '#fff' }}>
      <Box sx={{ minWidth: ANCHO_COLUMNA_HORAS + grilla.dias.length * 130 }}>
        <Box sx={{ display: 'grid', gridTemplateColumns: `${ANCHO_COLUMNA_HORAS}px repeat(${grilla.dias.length}, 1fr)`, borderBottom: '1px solid #e4ecf4' }}>
          <Box />
          {grilla.dias.map((dia) => (
            <Typography key={dia} variant="subtitle2" sx={{ textAlign: 'center', py: 1, fontWeight: 700, color: PALETA.azul }}>
              {DIAS_NOMBRE[dia - 1]}
            </Typography>
          ))}
        </Box>

        <Box sx={{ display: 'grid', gridTemplateColumns: `${ANCHO_COLUMNA_HORAS}px repeat(${grilla.dias.length}, 1fr)` }}>
          <Box sx={{ position: 'relative', height: alto }}>
            {Array.from({ length: grilla.horaFin - grilla.horaInicio }, (_, indice) => (
              <Typography
                key={indice}
                variant="caption"
                sx={{ position: 'absolute', top: indice * PX_POR_HORA - 8, right: 6, color: '#888' }}
              >
                {String(grilla.horaInicio + indice).padStart(2, '0')}:00
              </Typography>
            ))}
          </Box>

          {grilla.dias.map((dia) => (
            <Box
              key={dia}
              sx={{
                position: 'relative',
                height: alto,
                borderLeft: '1px solid #eef2f7',
                backgroundImage: `repeating-linear-gradient(to bottom, transparent 0, transparent ${PX_POR_HORA - 1}px, #eef2f7 ${PX_POR_HORA - 1}px, #eef2f7 ${PX_POR_HORA}px)`,
              }}
            >
              {grilla.bloques
                .filter((bloque) => bloque.dia === dia)
                .map((bloque) => {
                  const top = ((bloque.inicioMin - grilla.horaInicio * 60) / 60) * PX_POR_HORA;
                  const height = ((bloque.finMin - bloque.inicioMin) / 60) * PX_POR_HORA - 4;
                  return (
                    <Box
                      key={bloque.id}
                      sx={{
                        position: 'absolute',
                        top: top + 2,
                        left: 4,
                        right: 4,
                        height,
                        borderRadius: 1.5,
                        px: 1,
                        py: 0.5,
                        color: '#fff',
                        backgroundColor: bloque.color,
                        overflow: 'hidden',
                        boxShadow: '0 1px 3px rgba(0,0,0,0.15)',
                      }}
                    >
                      <Typography variant="caption" sx={{ display: 'block', fontWeight: 700, lineHeight: 1.2 }}>
                        {bloque.materia}
                      </Typography>
                      <Typography variant="caption" sx={{ display: 'block', opacity: 0.95, lineHeight: 1.2 }}>
                        {bloque.curso}
                      </Typography>
                      {mostrarDocente && (
                        <Typography variant="caption" sx={{ display: 'block', opacity: 0.9, lineHeight: 1.2 }}>
                          {bloque.docente}
                        </Typography>
                      )}
                    </Box>
                  );
                })}
            </Box>
          ))}
        </Box>
      </Box>
    </Box>
  );
};
