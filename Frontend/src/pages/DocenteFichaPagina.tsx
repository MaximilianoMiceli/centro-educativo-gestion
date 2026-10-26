import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Avatar, Box, Button, Chip, CircularProgress, Paper, Stack, Typography } from '@mui/material';
import { ArrowBack as ArrowBackIcon, PersonOff as PersonOffIcon } from '@mui/icons-material';
import { docenteService } from '../services/docenteService';
import type { DocenteDTO } from '../services/docenteService';
import { DocenteFichaContenido } from '../components/DocenteFichaContenido';
import { EstadoVacio } from '../components/EstadoVacio';
import { GRADIENTE_PRINCIPAL, PALETA } from '../institucion';

const COLORES_ESTADO: Record<DocenteDTO['estado'], 'success' | 'default' | 'warning'> = {
  ACTIVO: 'success',
  INACTIVO: 'default',
  LICENCIA: 'warning',
};

interface DocenteFichaPaginaProps {
  token: string;
}

/** Ficha completa de un docente: encabezado con su identidad, datos y agenda. */
export const DocenteFichaPagina: React.FC<DocenteFichaPaginaProps> = ({ token }) => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [docente, setDocente] = useState<DocenteDTO | null>(null);
  const [cargando, setCargando] = useState(true);
  const [noEncontrado, setNoEncontrado] = useState(false);

  useEffect(() => {
    docenteService.setToken(token);
    setCargando(true);
    setNoEncontrado(false);
    docenteService
      .obtener(Number(id))
      .then(setDocente)
      .catch(() => setNoEncontrado(true))
      .finally(() => setCargando(false));
  }, [id, token]);

  const volver = (
    <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/docentes')} sx={{ mb: 2 }}>
      Volver a docentes
    </Button>
  );

  if (cargando) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  }

  if (noEncontrado || !docente) {
    return (
      <Box>
        {volver}
        <EstadoVacio
          icono={<PersonOffIcon sx={{ fontSize: 48 }} />}
          titulo="No encontramos ese docente"
          descripcion="Puede que el enlace sea viejo o que el docente ya no esté disponible para tu rol."
        />
      </Box>
    );
  }

  const iniciales = `${docente.apellido.charAt(0)}${docente.nombre.charAt(0)}`.toUpperCase();

  return (
    <Box>
      {volver}

      <Paper sx={{ p: { xs: 2, sm: 3 }, mb: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)', borderRadius: 3 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: { xs: 'flex-start', sm: 'center' } }}>
          <Avatar sx={{ width: 64, height: 64, background: GRADIENTE_PRINCIPAL, fontWeight: 700, fontSize: '1.4rem' }}>
            {iniciales}
          </Avatar>
          <Box sx={{ flex: 1 }}>
            <Typography variant="h5" sx={{ fontWeight: 700, color: PALETA.grisTexto }}>
              {docente.apellido}, {docente.nombre}
            </Typography>
            <Typography variant="body2" sx={{ color: '#666' }}>
              Legajo {docente.legajo} · {docente.especialidad}
            </Typography>
          </Box>
          <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
            <Chip label={docente.estado} color={COLORES_ESTADO[docente.estado]} />
            {docente.cursos.length > 0 && <Chip label={`${docente.cursos.length} cursos`} variant="outlined" />}
          </Stack>
        </Stack>
      </Paper>

      <Paper sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)', borderRadius: 3, overflow: 'hidden' }}>
        <DocenteFichaContenido
          key={docente.id}
          docente={docente}
          onGuardado={(guardado) => setDocente(guardado)}
        />
      </Paper>
    </Box>
  );
};
