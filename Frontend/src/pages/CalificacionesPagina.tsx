import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { calificacionesService, ETIQUETA_TIPO } from '../services/calificacionesService';
import type { DictadoCalificable, Evaluacion, Planilla, TipoEvaluacion } from '../services/calificacionesService';
import { EstadoVacio } from '../components/EstadoVacio';
import { FilasCargando } from '../components/FilasCargando';
import { PALETA } from '../institucion';

interface CalificacionesPaginaProps {
  token: string;
}

const TIPOS: TipoEvaluacion[] = ['EXAMEN', 'TRABAJO_PRACTICO', 'ORAL', 'OTRA'];
const fecha = (valor: string) => new Date(`${valor}T00:00:00`).toLocaleDateString('es-AR');

/** Carga de calificaciones del profesor (sus materias) y del administrador (todas). */
export const CalificacionesPagina: React.FC<CalificacionesPaginaProps> = ({ token }) => {
  const [dictados, setDictados] = useState<DictadoCalificable[] | null>(null);
  const [dictadoId, setDictadoId] = useState<number | ''>('');
  const [evaluaciones, setEvaluaciones] = useState<Evaluacion[] | null>(null);
  const [evaluacionId, setEvaluacionId] = useState<number | null>(null);
  const [planilla, setPlanilla] = useState<Planilla | null>(null);
  const [notas, setNotas] = useState<Record<number, string>>({});
  const [nueva, setNueva] = useState(false);
  const [formulario, setFormulario] = useState({ titulo: '', tipo: 'EXAMEN' as TipoEvaluacion, periodo: 1, fecha: '' });
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState('');
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    calificacionesService.setToken(token);
    calificacionesService.dictados().then(setDictados).catch(() => setDictados([]));
  }, [token]);

  useEffect(() => {
    if (dictadoId === '') return;
    setEvaluaciones(null);
    setEvaluacionId(null);
    setPlanilla(null);
    calificacionesService.evaluaciones(dictadoId).then(setEvaluaciones).catch(() => setEvaluaciones([]));
  }, [dictadoId]);

  const abrirPlanilla = (id: number) => {
    setEvaluacionId(id);
    setError('');
    setAviso('');
    calificacionesService.planilla(id).then((p) => {
      setPlanilla(p);
      setNotas(Object.fromEntries(p.alumnos.map((a) => [a.alumnoId, a.nota === null ? '' : String(a.nota)])));
    }).catch(() => setError('No se pudo cargar la planilla'));
  };

  const crearEvaluacion = async () => {
    if (dictadoId === '') return;
    setError('');
    try {
      const creada = await calificacionesService.crearEvaluacion(dictadoId, formulario);
      setEvaluaciones((previas) => [...(previas ?? []), creada]);
      setNueva(false);
      setFormulario({ titulo: '', tipo: 'EXAMEN', periodo: 1, fecha: '' });
      setAviso(`Evaluación "${creada.titulo}" creada`);
    } catch (err: any) {
      const errores = err.response?.data?.errores;
      setError(errores ? Object.values(errores).join(' · ') : (err.response?.data?.message ?? 'No se pudo crear la evaluación'));
    }
  };

  const guardar = async () => {
    if (!planilla) return;
    const invalidas = Object.values(notas).filter((v) => v.trim() !== '' && (Number(v) < 1 || Number(v) > 10 || Number.isNaN(Number(v))));
    if (invalidas.length > 0) {
      setError('Las notas deben estar entre 1 y 10');
      return;
    }
    setGuardando(true);
    setError('');
    try {
      const datos = planilla.alumnos.map((a) => ({
        alumnoId: a.alumnoId,
        nota: notas[a.alumnoId]?.trim() ? Number(notas[a.alumnoId]) : null,
      }));
      const actualizada = await calificacionesService.guardarNotas(planilla.evaluacionId, datos);
      setPlanilla(actualizada);
      setAviso('Notas guardadas');
    } catch (err: any) {
      const errores = err.response?.data?.errores;
      setError(errores ? Object.values(errores).join(' · ') : (err.response?.data?.message ?? 'No se pudieron guardar las notas'));
    } finally {
      setGuardando(false);
    }
  };

  if (dictados === null) {
    return <Typography variant="body2" sx={{ color: '#666' }}>Cargando materias…</Typography>;
  }

  if (dictados.length === 0) {
    return (
      <EstadoVacio
        titulo="No tenés materias a cargo"
        descripcion="Cuando te asignen una materia con su curso, vas a poder crear evaluaciones y cargar las notas desde acá."
      />
    );
  }

  const evaluacionElegida = evaluaciones?.find((e) => e.id === evaluacionId);

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 700 }}>Calificaciones</Typography>
      <Typography variant="body2" sx={{ color: '#666', mb: 3 }}>
        Creá las evaluaciones de cada materia y cargá las notas de tus alumnos.
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {aviso && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setAviso('')}>{aviso}</Alert>}

      <Card sx={{ mb: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <TextField
            select
            label="Materia y curso"
            value={dictadoId}
            onChange={(e) => setDictadoId(e.target.value === '' ? '' : Number(e.target.value))}
            size="small"
            fullWidth
            sx={{ maxWidth: 480 }}
          >
            {dictados.map((d) => (
              <MenuItem key={d.id} value={d.id}>{d.materia} · {d.curso}</MenuItem>
            ))}
          </TextField>
        </CardContent>
      </Card>

      {dictadoId !== '' && (
        <Card sx={{ mb: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
          <CardContent>
            <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 700, color: PALETA.azul }}>Evaluaciones</Typography>
              <Button variant="contained" startIcon={<AddIcon />} onClick={() => setNueva(true)}>Nueva evaluación</Button>
            </Stack>

            {evaluaciones === null && <Typography variant="body2" sx={{ color: '#666' }}>Cargando evaluaciones…</Typography>}
            {evaluaciones?.length === 0 && (
              <EstadoVacio
                titulo="Todavía no hay evaluaciones en esta materia"
                descripcion="Creá la primera con el botón de arriba. Después vas a poder cargar sus notas."
              />
            )}
            {evaluaciones && evaluaciones.length > 0 && (
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow sx={{ '& th': { fontWeight: 700, backgroundColor: '#F5F6FA' } }}>
                      <TableCell>Evaluación</TableCell>
                      <TableCell>Tipo</TableCell>
                      <TableCell>Período</TableCell>
                      <TableCell>Fecha</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {evaluaciones.map((e) => (
                      <TableRow key={e.id} hover selected={e.id === evaluacionId} onClick={() => abrirPlanilla(e.id)} sx={{ cursor: 'pointer' }}>
                        <TableCell>{e.titulo}</TableCell>
                        <TableCell>{ETIQUETA_TIPO[e.tipo]}</TableCell>
                        <TableCell>{e.periodo}°</TableCell>
                        <TableCell>{fecha(e.fecha)}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </CardContent>
        </Card>
      )}

      {evaluacionElegida && planilla && (
        <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
          <CardContent>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ justifyContent: 'space-between', mb: 2 }}>
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>{planilla.titulo}</Typography>
                <Typography variant="body2" sx={{ color: '#666' }}>{planilla.materia} · {planilla.curso} · {fecha(planilla.fecha)}</Typography>
              </Box>
              <Chip size="small" label="Notas de 1 a 10. Dejá vacío para alumnos sin nota." variant="outlined" />
            </Stack>

            <Paper variant="outlined" sx={{ borderRadius: 2, overflow: 'hidden' }}>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow sx={{ '& th': { fontWeight: 700, backgroundColor: '#F5F6FA' } }}>
                      <TableCell>Legajo</TableCell>
                      <TableCell>Alumno</TableCell>
                      <TableCell sx={{ width: 140 }}>Nota</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {planilla.alumnos.length === 0 && <FilasCargando columnas={3} filas={1} />}
                    {planilla.alumnos.map((a) => (
                      <TableRow key={a.alumnoId} hover>
                        <TableCell>{a.legajo}</TableCell>
                        <TableCell>{a.alumno}</TableCell>
                        <TableCell>
                          <TextField
                            size="small"
                            type="number"
                            value={notas[a.alumnoId] ?? ''}
                            onChange={(e) => setNotas((previas) => ({ ...previas, [a.alumnoId]: e.target.value }))}
                            slotProps={{ htmlInput: { min: 1, max: 10, step: 0.5 } }}
                            sx={{ width: 110 }}
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Paper>

            <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
              <Button variant="contained" onClick={guardar} disabled={guardando}>
                {guardando ? 'Guardando…' : 'Guardar notas'}
              </Button>
            </Box>
          </CardContent>
        </Card>
      )}

      <Dialog open={nueva} onClose={() => setNueva(false)} fullWidth maxWidth="xs">
        <DialogTitle>Nueva evaluación</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            <TextField
              label="Nombre"
              value={formulario.titulo}
              onChange={(e) => setFormulario({ ...formulario, titulo: e.target.value })}
              size="small"
              helperText="Por ejemplo: Examen parcial o Trabajo práctico 2"
              fullWidth
            />
            <TextField select label="Tipo" value={formulario.tipo} size="small" fullWidth
              onChange={(e) => setFormulario({ ...formulario, tipo: e.target.value as TipoEvaluacion })}>
              {TIPOS.map((t) => <MenuItem key={t} value={t}>{ETIQUETA_TIPO[t]}</MenuItem>)}
            </TextField>
            <TextField select label="Período" value={formulario.periodo} size="small" fullWidth
              onChange={(e) => setFormulario({ ...formulario, periodo: Number(e.target.value) })}>
              {[1, 2, 3].map((p) => <MenuItem key={p} value={p}>{p}° período</MenuItem>)}
            </TextField>
            <TextField label="Fecha" type="date" value={formulario.fecha} size="small" fullWidth
              slotProps={{ inputLabel: { shrink: true } }}
              onChange={(e) => setFormulario({ ...formulario, fecha: e.target.value })} />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setNueva(false)}>Cancelar</Button>
          <Button
            variant="contained"
            onClick={crearEvaluacion}
            disabled={formulario.titulo.trim().length < 3 || !formulario.fecha}
          >
            Crear
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
