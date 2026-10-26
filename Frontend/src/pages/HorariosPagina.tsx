import React, { useEffect, useState } from 'react';
import { Alert, Box, Card, CardContent, MenuItem, Stack, TextField, ToggleButton, ToggleButtonGroup, Typography } from '@mui/material';
import { CalendarMonth as CalendarIcon } from '@mui/icons-material';
import { datosMaestrosService } from '../services/datosMaestrosService';
import type { CursoDTO } from '../services/datosMaestrosService';
import { docenteService } from '../services/docenteService';
import type { DocenteDTO } from '../services/docenteService';
import { horariosService } from '../services/horariosService';
import type { HorarioDTO } from '../services/horariosService';
import { HorarioSemanal } from '../components/HorarioSemanal';
import { EstadoVacio } from '../components/EstadoVacio';

interface HorariosPaginaProps {
  token: string;
}

type Vista = 'curso' | 'docente';

/** Horario semanal por curso o por docente, en grilla. */
export const HorariosPagina: React.FC<HorariosPaginaProps> = ({ token }) => {
  const [vista, setVista] = useState<Vista>('curso');
  const [cursos, setCursos] = useState<CursoDTO[]>([]);
  const [docentes, setDocentes] = useState<DocenteDTO[]>([]);
  const [seleccion, setSeleccion] = useState<number | ''>('');
  const [horarios, setHorarios] = useState<HorarioDTO[] | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    datosMaestrosService.setToken(token);
    docenteService.setToken(token);
    horariosService.setToken(token);
    datosMaestrosService.listarCursos().then(setCursos).catch(() => setError('No se pudieron cargar los cursos'));
    docenteService.listar().then(setDocentes).catch(() => setError('No se pudieron cargar los docentes'));
  }, [token]);

  useEffect(() => {
    if (seleccion === '') {
      return;
    }
    setError('');
    horariosService
      .listar(vista === 'curso' ? { cursoId: seleccion } : { docenteId: seleccion })
      .then(setHorarios)
      .catch(() => setError('No se pudo cargar el horario'));
  }, [vista, seleccion]);

  const cambiarVista = (nueva: Vista | null) => {
    if (!nueva) return;
    setVista(nueva);
    setSeleccion('');
    setHorarios(null);
  };

  const docenteElegido = docentes.find((d) => d.id === seleccion);
  const nombreSeleccionado = vista === 'curso'
    ? cursos.find((c) => c.id === seleccion)?.descripcion
    : docenteElegido && `${docenteElegido.apellido}, ${docenteElegido.nombre}`;

  return (
    <Box>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ mb: 3, alignItems: { md: 'center' }, justifyContent: 'space-between' }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>Horarios</Typography>
          <Typography variant="body2" sx={{ color: '#666' }}>
            Elegí un curso o un docente para ver su horario de la semana.
          </Typography>
        </Box>
        <ToggleButtonGroup value={vista} exclusive onChange={(_e, nueva: Vista | null) => cambiarVista(nueva)} size="small">
          <ToggleButton value="curso">Por curso</ToggleButton>
          <ToggleButton value="docente">Por docente</ToggleButton>
        </ToggleButtonGroup>
      </Stack>

      <Card sx={{ mb: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <TextField
            select
            label={vista === 'curso' ? 'Curso' : 'Docente'}
            value={seleccion}
            onChange={(e) => setSeleccion(e.target.value === '' ? '' : Number(e.target.value))}
            size="small"
            sx={{ minWidth: 300 }}
          >
            {vista === 'curso'
              ? cursos.map((c) => <MenuItem key={c.id} value={c.id}>{c.descripcion}</MenuItem>)
              : docentes.map((d) => <MenuItem key={d.id} value={d.id}>{d.apellido}, {d.nombre}</MenuItem>)}
          </TextField>
        </CardContent>
      </Card>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {seleccion === '' && (
        <EstadoVacio
          icono={<CalendarIcon sx={{ fontSize: 48 }} />}
          titulo="Todavía no elegiste nada"
          descripcion={vista === 'curso' ? 'Seleccioná un curso para ver sus materias por día y hora.' : 'Seleccioná un docente para ver sus clases de la semana.'}
        />
      )}

      {seleccion !== '' && horarios && (
        <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
          <CardContent>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2 }}>{nombreSeleccionado}</Typography>
            <HorarioSemanal horarios={horarios} mostrarDocente={vista === 'curso'} />
          </CardContent>
        </Card>
      )}
    </Box>
  );
};
