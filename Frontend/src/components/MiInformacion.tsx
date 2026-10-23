import React, { useEffect, useState } from 'react';
import { Alert, Box, Card, CardContent, Chip, Divider, Tab, Tabs, Typography, List, ListItemText } from '@mui/material';
import { miInformacionService } from '../services/miInformacionService';
import type { AlumnoResumen, MiInformacion as Detalle } from '../services/miInformacionService';

interface MiInformacionProps {
  token: string;
}

export const MiInformacion: React.FC<MiInformacionProps> = ({ token }) => {
  const [alumnos, setAlumnos] = useState<AlumnoResumen[]>([]);
  const [seleccionado, setSeleccionado] = useState<number | null>(null);
  const [detalle, setDetalle] = useState<Detalle | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    miInformacionService.setToken(token);
    miInformacionService
      .alumnos()
      .then((lista) => {
        setAlumnos(lista);
        setSeleccionado(lista[0]?.id ?? null);
      })
      .catch(() => setError('No se pudo cargar la información'));
  }, [token]);

  useEffect(() => {
    if (seleccionado === null) return;
    setDetalle(null);
    miInformacionService.detalle(seleccionado).then(setDetalle).catch(() => setError('No se pudo cargar el alumno'));
  }, [seleccionado]);

  if (error) return <Alert severity="error">{error}</Alert>;
  if (alumnos.length === 0) {
    return <Alert severity="info">No tenés alumnos vinculados a tu cuenta. Consultá con la administración.</Alert>;
  }

  return (
    <Box>
      {alumnos.length > 1 && (
        <Tabs value={seleccionado} onChange={(_e, id: number) => setSeleccionado(id)} variant="scrollable" sx={{ mb: 2 }}>
          {alumnos.map((a) => (
            <Tab key={a.id} value={a.id} label={a.alumno} />
          ))}
        </Tabs>
      )}

      {detalle && (
        <>
          <Card sx={{ mb: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
            <CardContent>
              <Typography variant="h5" sx={{ fontWeight: 600 }}>
                {detalle.ficha.apellido}, {detalle.ficha.nombre}
              </Typography>
              <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>
                Legajo {detalle.ficha.legajo} · {detalle.ficha.cursoDescripcion} · {detalle.ficha.estado}
              </Typography>
              <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 1 }}>
                <Typography variant="body2">Fecha de nacimiento: {detalle.ficha.fechaNacimiento}</Typography>
                <Typography variant="body2">Domicilio: {detalle.ficha.domicilio ?? '—'}</Typography>
                <Typography variant="body2">Teléfono: {detalle.ficha.telefono ?? '—'}</Typography>
                <Typography variant="body2">Email: {detalle.ficha.email ?? '—'}</Typography>
              </Box>
            </CardContent>
          </Card>

          <Card sx={{ mb: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>Materias</Typography>
              <Divider sx={{ my: 1 }} />
              <List dense>
                {detalle.materias.map((m) => (
                  <ListItemText
                    key={m.materia}
                    sx={{ px: 1 }}
                    primary={`${m.materia} · ${m.docente}`}
                    secondary={m.horarios.length ? m.horarios.join(' · ') : 'Sin horarios cargados'}
                  />
                ))}
                {detalle.materias.length === 0 && (
                  <Typography variant="body2" sx={{ color: '#666' }}>Todavía no tiene materias asignadas.</Typography>
                )}
              </List>
            </CardContent>
          </Card>

          <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>Servicios</Typography>
              <Divider sx={{ my: 1 }} />
              <Typography variant="subtitle2" sx={{ mt: 1 }}>Deportes</Typography>
              <List dense>
                {detalle.servicios.deportes.map((d) => (
                  <ListItemText key={d.id} sx={{ px: 1 }} primary={`${d.deporte} · ${d.horario}`} />
                ))}
                {detalle.servicios.deportes.length === 0 && (
                  <Typography variant="body2" sx={{ px: 1, color: '#666' }}>Sin deportes inscriptos.</Typography>
                )}
              </List>
              <Typography variant="subtitle2" sx={{ mt: 1 }}>Transporte</Typography>
              <Box sx={{ px: 1, mb: 1 }}>
                {detalle.servicios.transporte
                  ? <Chip size="small" label={detalle.servicios.transporte.nombre} color="primary" variant="outlined" />
                  : <Typography variant="body2" sx={{ color: '#666' }}>Sin transporte asignado.</Typography>}
              </Box>
              <Typography variant="subtitle2">Comedor</Typography>
              <Typography variant="body2" sx={{ px: 1, color: '#666' }}>
                {detalle.servicios.comedor ? 'Inscripto en el comedor' : 'No inscripto'}
              </Typography>
            </CardContent>
          </Card>
        </>
      )}
    </Box>
  );
};
