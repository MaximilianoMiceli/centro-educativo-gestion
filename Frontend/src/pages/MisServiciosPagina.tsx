import React, { useEffect, useState } from 'react';
import { Alert, Box, Card, CardContent, Chip, CircularProgress, Divider, List, ListItemText, Stack, Tab, Tabs, Typography } from '@mui/material';
import { miInformacionService } from '../services/miInformacionService';
import type { AlumnoResumen, MiInformacion } from '../services/miInformacionService';
import { ServiciosAlumno } from '../components/ServiciosAlumno';
import { EstadoVacio } from '../components/EstadoVacio';
import { rolDelToken } from '../utils/token';
import { PALETA } from '../institucion';

interface MisServiciosPaginaProps {
  token: string;
}

/**
 * Servicios de la cuenta. El alumno los gestiona desde acá; el padre los consulta de cada hijo
 * (solo lectura, porque la inscripción la hace el alumno).
 */
export const MisServiciosPagina: React.FC<MisServiciosPaginaProps> = ({ token }) => {
  const esAlumno = rolDelToken(token) === 'ALUMNO';
  const [alumnos, setAlumnos] = useState<AlumnoResumen[] | null>(null);
  const [seleccionado, setSeleccionado] = useState<number | null>(null);
  const [detalle, setDetalle] = useState<MiInformacion | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    miInformacionService.setToken(token);
    miInformacionService
      .alumnos()
      .then((lista) => {
        setAlumnos(lista);
        setSeleccionado(lista[0]?.id ?? null);
      })
      .catch(() => setError('No se pudieron cargar los servicios'));
  }, [token]);

  useEffect(() => {
    if (seleccionado === null || esAlumno) return;
    setDetalle(null);
    miInformacionService.detalle(seleccionado).then(setDetalle).catch(() => setError('No se pudieron cargar los servicios'));
  }, [seleccionado, esAlumno]);

  if (error) return <Alert severity="error">{error}</Alert>;
  if (alumnos === null) return <CircularProgress />;
  if (alumnos.length === 0) {
    return <EstadoVacio titulo="No tenés alumnos vinculados" descripcion="Consultá con la administración del centro." />;
  }

  const alumnoElegido = alumnos.find((a) => a.id === seleccionado);

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 700 }}>Mis servicios</Typography>
      <Typography variant="body2" sx={{ color: '#666', mb: 2 }}>
        {esAlumno
          ? 'Deportes, transporte, comedor e idiomas. Podés inscribirte o darte de baja en cada uno.'
          : 'Deportes, transporte, comedor e idiomas de tus hijos.'}
      </Typography>

      {!esAlumno && alumnos.length > 1 && (
        <Tabs value={seleccionado} onChange={(_e, id: number) => setSeleccionado(id)} variant="scrollable" sx={{ mb: 2, borderBottom: '1px solid #e4ecf4' }}>
          {alumnos.map((a) => <Tab key={a.id} value={a.id} label={a.alumno} />)}
        </Tabs>
      )}

      {esAlumno && seleccionado !== null && (
        <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
          <CardContent>
            <ServiciosAlumno alumnoId={seleccionado} token={token} editable propio />
          </CardContent>
        </Card>
      )}

      {!esAlumno && !detalle && <CircularProgress />}

      {!esAlumno && detalle && (
        <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
          <CardContent>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: PALETA.azul }}>{alumnoElegido?.alumno}</Typography>
            <Divider sx={{ my: 1.5 }} />
            <Typography variant="subtitle2">Deportes</Typography>
            <List dense>
              {detalle.servicios.deportes.map((d) => <ListItemText key={d.id} primary={`${d.deporte} · ${d.horario}`} />)}
              {detalle.servicios.deportes.length === 0 && <Typography variant="body2" sx={{ color: '#666' }}>Sin deportes inscriptos.</Typography>}
            </List>
            <Stack direction="row" spacing={2} sx={{ alignItems: 'center', mt: 1 }}>
              <Typography variant="subtitle2">Transporte</Typography>
              {detalle.servicios.transporte
                ? <Chip size="small" label={detalle.servicios.transporte.nombre} color="primary" variant="outlined" />
                : <Typography variant="body2" sx={{ color: '#666' }}>Sin transporte asignado.</Typography>}
            </Stack>
            <Typography variant="subtitle2" sx={{ mt: 1 }}>Comedor</Typography>
            <Typography variant="body2" sx={{ color: '#666' }}>
              {detalle.servicios.comedor ? 'Inscripto en el comedor' : 'No inscripto'}
            </Typography>
            <Typography variant="subtitle2" sx={{ mt: 1 }}>Idiomas</Typography>
            <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap', mt: 0.5 }}>
              {detalle.servicios.idiomas.map((i) => <Chip key={i.id} size="small" label={i.idioma} color="secondary" variant="outlined" />)}
              {detalle.servicios.idiomas.length === 0 && <Typography variant="body2" sx={{ color: '#666' }}>Sin idiomas inscriptos.</Typography>}
            </Stack>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};
