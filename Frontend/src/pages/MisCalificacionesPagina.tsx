import React, { useEffect, useState } from 'react';
import { Alert, Box, Card, CardContent, Chip, CircularProgress, Stack, Tab, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tabs, Typography } from '@mui/material';
import { calificacionesService, ETIQUETA_TIPO } from '../services/calificacionesService';
import type { MateriaCalificaciones } from '../services/calificacionesService';
import { miInformacionService } from '../services/miInformacionService';
import type { AlumnoResumen } from '../services/miInformacionService';
import { EstadoVacio } from '../components/EstadoVacio';
import { PALETA } from '../institucion';

interface MisCalificacionesPaginaProps {
  token: string;
}

const fecha = (valor: string) => new Date(`${valor}T00:00:00`).toLocaleDateString('es-AR');
const numero = (valor: number) => valor.toFixed(2).replace('.', ',');

/** Calificaciones por materia del alumno, o de cada hijo si la cuenta es de padre. */
export const MisCalificacionesPagina: React.FC<MisCalificacionesPaginaProps> = ({ token }) => {
  const [alumnos, setAlumnos] = useState<AlumnoResumen[] | null>(null);
  const [seleccionado, setSeleccionado] = useState<number | null>(null);
  const [materias, setMaterias] = useState<MateriaCalificaciones[] | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    calificacionesService.setToken(token);
    miInformacionService.setToken(token);
    miInformacionService
      .alumnos()
      .then((lista) => {
        setAlumnos(lista);
        setSeleccionado(lista[0]?.id ?? null);
      })
      .catch(() => setError('No se pudieron cargar los alumnos'));
  }, [token]);

  useEffect(() => {
    if (seleccionado === null) return;
    setMaterias(null);
    setError('');
    calificacionesService.deAlumno(seleccionado).then(setMaterias).catch(() => setError('No se pudieron cargar las calificaciones'));
  }, [seleccionado]);

  if (error) return <Alert severity="error">{error}</Alert>;
  if (alumnos === null) return <CircularProgress />;
  if (alumnos.length === 0) {
    return <EstadoVacio titulo="No tenés alumnos vinculados" descripcion="Consultá con la administración del centro." />;
  }

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 700 }}>Calificaciones</Typography>
      <Typography variant="body2" sx={{ color: '#666', mb: 2 }}>
        Notas de cada evaluación y promedio por materia.
      </Typography>

      {alumnos.length > 1 && (
        <Tabs value={seleccionado} onChange={(_e, id: number) => setSeleccionado(id)} variant="scrollable" sx={{ mb: 2, borderBottom: '1px solid #e4ecf4' }}>
          {alumnos.map((a) => <Tab key={a.id} value={a.id} label={a.alumno} />)}
        </Tabs>
      )}

      {materias === null && <CircularProgress />}
      {materias?.length === 0 && (
        <EstadoVacio titulo="Todavía no hay calificaciones" descripcion="Cuando los docentes carguen notas, las vas a ver acá." />
      )}

      {materias?.map((m) => (
        <Card key={m.materia} sx={{ mb: 2, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
          <CardContent>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ justifyContent: 'space-between', mb: 1.5 }}>
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 700, color: PALETA.azul }}>{m.materia}</Typography>
                <Typography variant="body2" sx={{ color: '#666' }}>{m.docente}</Typography>
              </Box>
              <Chip
                label={m.promedio === null ? 'Sin notas' : `Promedio ${numero(m.promedio)}`}
                color={m.promedio === null ? 'default' : m.promedio >= 6 ? 'success' : 'warning'}
                variant="outlined"
              />
            </Stack>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ '& th': { fontWeight: 700, backgroundColor: '#F5F6FA' } }}>
                    <TableCell>Evaluación</TableCell>
                    <TableCell>Tipo</TableCell>
                    <TableCell>Período</TableCell>
                    <TableCell>Fecha</TableCell>
                    <TableCell align="right">Nota</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {m.evaluaciones.map((e) => (
                    <TableRow key={`${e.titulo}-${e.fecha}`}>
                      <TableCell>{e.titulo}</TableCell>
                      <TableCell>{ETIQUETA_TIPO[e.tipo]}</TableCell>
                      <TableCell>{e.periodo}°</TableCell>
                      <TableCell>{fecha(e.fecha)}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 700, color: e.nota === null ? '#999' : undefined }}>
                        {e.nota === null ? 'Sin nota' : numero(e.nota)}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      ))}
    </Box>
  );
};
