import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  Divider,
  FormControlLabel,
  List,
  ListItemText,
  MenuItem,
  TextField,
  Typography,
} from '@mui/material';
import { serviciosService } from '../services/serviciosService';
import type { Deporte, Recorrido, ServiciosAlumno as Servicios } from '../services/serviciosService';

interface ServiciosAlumnoProps {
  alumnoId: number;
  editable: boolean;
}

export const ServiciosAlumno: React.FC<ServiciosAlumnoProps> = ({ alumnoId, editable }) => {
  const [servicios, setServicios] = useState<Servicios | null>(null);
  const [deportes, setDeportes] = useState<Deporte[]>([]);
  const [recorridos, setRecorridos] = useState<Recorrido[]>([]);
  const [deporteSel, setDeporteSel] = useState('');
  const [grupoSel, setGrupoSel] = useState('');
  const [recorridoSel, setRecorridoSel] = useState('');
  const [error, setError] = useState<string | null>(null);

  const cargar = async () => {
    setServicios(await serviciosService.servicios(alumnoId));
  };

  useEffect(() => {
    setError(null);
    setDeporteSel('');
    setGrupoSel('');
    cargar().catch(() => setError('No se pudieron cargar los servicios del alumno'));
    serviciosService.listarDeportes().then(setDeportes);
    serviciosService.listarRecorridos().then(setRecorridos);
  }, [alumnoId]);

  const ejecutar = async (operacion: () => Promise<Servicios>) => {
    setError(null);
    try {
      setServicios(await operacion());
    } catch (err: any) {
      const data = err.response?.data;
      const detalle = data?.errores ? Object.values(data.errores).join(' · ') : null;
      setError(detalle ?? data?.message ?? 'No se pudo completar la operación');
    }
  };

  if (!servicios) {
    return <Typography variant="body2" sx={{ color: '#666' }}>Cargando servicios…</Typography>;
  }

  const gruposDelDeporte = deportes
    .find((d) => String(d.id) === deporteSel)
    ?.grupos.filter((g) => g.nivel === servicios.nivel) ?? [];
  const deportesDisponibles = deportes.filter((d) =>
    d.grupos.some((g) => g.nivel === servicios.nivel)
      && !servicios.deportes.some((i) => i.deporteId === d.id));

  return (
    <Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Deportes</Typography>
      <List dense>
        {servicios.deportes.map((i) => (
          <ListItemText
            key={i.id}
            sx={{ pl: 1 }}
            primary={`${i.deporte} · ${i.horario}`}
            secondary={editable ? (
              <Button size="small" color="error" onClick={() => ejecutar(() => serviciosService.quitarDeporte(alumnoId, i.id))}>
                Dar de baja
              </Button>
            ) : undefined}
          />
        ))}
        {servicios.deportes.length === 0 && (
          <Typography variant="body2" sx={{ pl: 1, color: '#666' }}>Sin deportes inscriptos (máximo 2).</Typography>
        )}
      </List>

      {editable && servicios.deportes.length < 2 && (
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr auto' }, gap: 2, mt: 1, alignItems: 'flex-start' }}>
          <TextField
            select
            label="Deporte"
            value={deporteSel}
            onChange={(e) => {
              setDeporteSel(e.target.value);
              setGrupoSel('');
            }}
            size="small"
          >
            {deportesDisponibles.map((d) => (
              <MenuItem key={d.id} value={String(d.id)}>{d.nombre}</MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Grupo (horario)"
            value={grupoSel}
            onChange={(e) => setGrupoSel(e.target.value)}
            size="small"
            disabled={!deporteSel}
          >
            {gruposDelDeporte.map((g) => (
              <MenuItem key={g.id} value={String(g.id)}>{g.horario}</MenuItem>
            ))}
          </TextField>
          <Button
            variant="contained"
            disabled={!grupoSel}
            onClick={() => ejecutar(() => serviciosService.inscribirDeporte(alumnoId, Number(grupoSel)))}
          >
            Inscribir
          </Button>
        </Box>
      )}

      <Divider sx={{ my: 2 }} />

      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Transporte</Typography>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap', mt: 1 }}>
        {servicios.transporte
          ? <Chip label={servicios.transporte.nombre} color="primary" variant="outlined" />
          : <Typography variant="body2" sx={{ color: '#666' }}>Sin transporte asignado</Typography>}
        {editable && (
          <>
            <TextField
              select
              label="Recorrido"
              value={recorridoSel}
              onChange={(e) => setRecorridoSel(e.target.value)}
              size="small"
              sx={{ minWidth: 220 }}
            >
              {recorridos.map((r) => (
                <MenuItem key={r.id} value={String(r.id)}>{r.nombre}</MenuItem>
              ))}
            </TextField>
            <Button
              variant="outlined"
              disabled={!recorridoSel}
              onClick={() => ejecutar(() => serviciosService.asignarTransporte(alumnoId, Number(recorridoSel)))}
            >
              Asignar
            </Button>
            {servicios.transporte && (
              <Button color="error" onClick={() => ejecutar(() => serviciosService.quitarTransporte(alumnoId))}>
                Dar de baja
              </Button>
            )}
          </>
        )}
      </Box>

      <Divider sx={{ my: 2 }} />

      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Comedor</Typography>
      <FormControlLabel
        control={
          <Checkbox
            checked={servicios.comedor}
            disabled={!editable}
            onChange={(e) => ejecutar(() => serviciosService.asignarComedor(alumnoId, e.target.checked))}
          />
        }
        label={servicios.comedor ? 'Inscripto en el comedor' : 'No inscripto'}
      />
    </Box>
  );
};
