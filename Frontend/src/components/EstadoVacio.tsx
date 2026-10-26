import React from 'react';
import { Box, Typography } from '@mui/material';
import { Inbox as InboxIcon } from '@mui/icons-material';
import { PALETA } from '../institucion';

interface EstadoVacioProps {
  titulo: string;
  descripcion?: string;
  icono?: React.ReactNode;
  accion?: React.ReactNode;
}

/** Mensaje para listas sin datos: explica por qué está vacía y, si se puede, ofrece la acción siguiente. */
export const EstadoVacio: React.FC<EstadoVacioProps> = ({ titulo, descripcion, icono, accion }) => (
  <Box sx={{ textAlign: 'center', py: 5, px: 2 }}>
    <Box sx={{ color: PALETA.azulClaro, mb: 1 }}>
      {icono ?? <InboxIcon sx={{ fontSize: 48 }} />}
    </Box>
    <Typography variant="subtitle1" sx={{ fontWeight: 700, color: PALETA.grisTexto }}>{titulo}</Typography>
    {descripcion && (
      <Typography variant="body2" sx={{ color: '#666', mt: 0.5, maxWidth: 480, mx: 'auto' }}>{descripcion}</Typography>
    )}
    {accion && <Box sx={{ mt: 2 }}>{accion}</Box>}
  </Box>
);
