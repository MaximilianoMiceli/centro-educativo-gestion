import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Avatar, Box, Button, Chip, CircularProgress, Paper, Stack, Typography } from '@mui/material';
import { ArrowBack as ArrowBackIcon, PersonOff as PersonOffIcon } from '@mui/icons-material';
import { alumnosAdminService } from '../services/alumnosAdminService';
import type { AlumnoFicha } from '../services/alumnosAdminService';
import { datosMaestrosService } from '../services/datosMaestrosService';
import type { CursoDTO } from '../services/datosMaestrosService';
import { AlumnoFichaContenido } from '../components/AlumnoFichaContenido';
import { EstadoVacio } from '../components/EstadoVacio';
import { rolDelToken } from '../utils/token';
import { GRADIENTE_PRINCIPAL, PALETA } from '../institucion';

const COLORES_ESTADO: Record<AlumnoFicha['estado'], 'success' | 'default' | 'info'> = {
  ACTIVO: 'success',
  INACTIVO: 'default',
  EGRESADO: 'info',
};

interface AlumnoFichaPaginaProps {
  token: string;
}

/** Ficha completa de un alumno: encabezado con su identidad y el contenido de la ficha. */
export const AlumnoFichaPagina: React.FC<AlumnoFichaPaginaProps> = ({ token }) => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [alumno, setAlumno] = useState<AlumnoFicha | null>(null);
  const [cursos, setCursos] = useState<CursoDTO[]>([]);
  const [cargando, setCargando] = useState(true);
  const [noEncontrado, setNoEncontrado] = useState(false);
  const editable = rolDelToken(token) === 'ADMIN';

  useEffect(() => {
    alumnosAdminService.setToken(token);
    datosMaestrosService.setToken(token);
    datosMaestrosService.listarCursos().then(setCursos).catch(() => setCursos([]));
  }, [token]);

  useEffect(() => {
    setCargando(true);
    setNoEncontrado(false);
    alumnosAdminService
      .ficha(Number(id))
      .then((ficha) => setAlumno(ficha))
      .catch(() => setNoEncontrado(true))
      .finally(() => setCargando(false));
  }, [id, token]);

  const volver = (
    <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/alumnos')} sx={{ mb: 2 }}>
      Volver a alumnos
    </Button>
  );

  if (cargando) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  }

  if (noEncontrado || !alumno) {
    return (
      <Box>
        {volver}
        <EstadoVacio
          icono={<PersonOffIcon sx={{ fontSize: 48 }} />}
          titulo="No encontramos ese alumno"
          descripcion="Puede que el enlace sea viejo o que el alumno ya no esté disponible para tu rol."
        />
      </Box>
    );
  }

  const iniciales = `${alumno.apellido.charAt(0)}${alumno.nombre.charAt(0)}`.toUpperCase();

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
              {alumno.apellido}, {alumno.nombre}
            </Typography>
            <Typography variant="body2" sx={{ color: '#666' }}>
              Legajo {alumno.legajo} · DNI {alumno.dni}
            </Typography>
          </Box>
          <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
            <Chip label={alumno.cursoDescripcion} variant="outlined" color="primary" />
            <Chip label={alumno.nivelNombre} variant="outlined" />
            <Chip label={alumno.estado} color={COLORES_ESTADO[alumno.estado]} />
          </Stack>
        </Stack>
      </Paper>

      <Paper sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)', borderRadius: 3, overflow: 'hidden' }}>
        <AlumnoFichaContenido
          key={alumno.id}
          alumno={alumno}
          cursos={cursos}
          editable={editable}
          onGuardado={(guardado) => setAlumno(guardado)}
        />
      </Paper>
    </Box>
  );
};
