import React, { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Divider,
  List,
  ListItemButton,
  ListItemText,
  MenuItem,
  TextField,
  Typography,
} from '@mui/material';
import { serviciosService } from '../services/serviciosService';
import type { Deporte } from '../services/serviciosService';
import { datosMaestrosService, DIAS } from '../services/datosMaestrosService';
import type { NivelDTO } from '../services/datosMaestrosService';
import { docenteService } from '../services/docenteService';
import type { DocenteDTO } from '../services/docenteService';

type Errores = Record<string, string>;
type Aviso = { tipo: 'success' | 'error'; texto: string } | null;

interface DeportesSeccionProps {
  onAviso: (aviso: Aviso) => void;
}

function leerError(err: any): { mensaje: string; errores: Errores } {
  return {
    mensaje: err.response?.data?.message ?? 'No se pudo completar la operación',
    errores: err.response?.data?.errores ?? {},
  };
}

export const DeportesSeccion: React.FC<DeportesSeccionProps> = ({ onAviso }) => {
  const [deportes, setDeportes] = useState<Deporte[]>([]);
  const [docentes, setDocentes] = useState<DocenteDTO[]>([]);
  const [niveles, setNiveles] = useState<NivelDTO[]>([]);
  const [seleccionado, setSeleccionado] = useState<Deporte | null>(null);
  const [nombre, setNombre] = useState('');
  const [responsableNuevo, setResponsableNuevo] = useState('');
  const [errores, setErrores] = useState<Errores>({});
  const [grupo, setGrupo] = useState({ nivelId: '', dia: '1', inicio: '', fin: '' });
  const [erroresGrupo, setErroresGrupo] = useState<Errores>({});
  const [responsableSel, setResponsableSel] = useState('');

  const cargar = async () => {
    const lista = await serviciosService.listarDeportes();
    setDeportes(lista);
    setSeleccionado((actual) => (actual ? lista.find((d) => d.id === actual.id) ?? null : null));
  };

  useEffect(() => {
    cargar();
    docenteService.listar().then(setDocentes);
    datosMaestrosService.listarNiveles().then(setNiveles);
  }, []);

  useEffect(() => {
    setResponsableSel(seleccionado?.docenteResponsableId ? String(seleccionado.docenteResponsableId) : '');
    setErroresGrupo({});
  }, [seleccionado]);

  const crear = async () => {
    setErrores({});
    try {
      await serviciosService.crearDeporte(nombre, responsableNuevo ? Number(responsableNuevo) : null);
      setNombre('');
      setResponsableNuevo('');
      onAviso({ tipo: 'success', texto: 'Deporte creado' });
      cargar();
    } catch (err) {
      const { mensaje, errores: e } = leerError(err);
      setErrores(e);
      onAviso({ tipo: 'error', texto: mensaje });
    }
  };

  const guardarResponsable = async () => {
    if (!seleccionado) return;
    try {
      const actualizado = await serviciosService.asignarResponsable(
        seleccionado.id,
        responsableSel ? Number(responsableSel) : null,
      );
      setSeleccionado(actualizado);
      onAviso({ tipo: 'success', texto: 'Responsable actualizado' });
      cargar();
    } catch (err) {
      const { mensaje, errores: e } = leerError(err);
      setErroresGrupo(e);
      onAviso({ tipo: 'error', texto: mensaje });
    }
  };

  const agregarGrupo = async () => {
    if (!seleccionado) return;
    setErroresGrupo({});
    try {
      const actualizado = await serviciosService.crearGrupo(seleccionado.id, {
        nivelId: Number(grupo.nivelId),
        diaSemana: Number(grupo.dia),
        horaInicio: grupo.inicio,
        horaFin: grupo.fin,
      });
      setSeleccionado(actualizado);
      setGrupo({ nivelId: grupo.nivelId, dia: grupo.dia, inicio: '', fin: '' });
      onAviso({ tipo: 'success', texto: 'Grupo agregado' });
      cargar();
    } catch (err) {
      const { mensaje, errores: e } = leerError(err);
      setErroresGrupo(e);
      onAviso({ tipo: 'error', texto: mensaje });
    }
  };

  return (
    <Box>
      <Card sx={{ mb: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="h6" sx={{ fontWeight: 600 }}>Deportes</Typography>
          <List dense sx={{ maxHeight: 260, overflow: 'auto' }}>
            {deportes.map((d) => (
              <ListItemButton key={d.id} selected={seleccionado?.id === d.id} onClick={() => setSeleccionado(d)}>
                <ListItemText
                  primary={d.nombre}
                  secondary={`Responsable: ${d.docenteResponsable ?? 'sin asignar'} · ${d.grupos.length} grupos`}
                />
              </ListItemButton>
            ))}
            {deportes.length === 0 && (
              <Typography variant="body2" sx={{ p: 1, color: '#666' }}>Todavía no hay deportes cargados.</Typography>
            )}
          </List>
          <Divider sx={{ my: 2 }} />
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr auto' }, gap: 2, alignItems: 'flex-start' }}>
            <TextField
              label="Nuevo deporte"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              error={!!errores.nombre}
              helperText={errores.nombre ?? ' '}
              size="small"
            />
            <TextField
              select
              label="Responsable"
              required
              value={responsableNuevo}
              onChange={(e) => setResponsableNuevo(e.target.value)}
              error={!!errores.docenteResponsableId}
              helperText={errores.docenteResponsableId ?? 'Todo deporte tiene un profesor responsable'}
              size="small"
            >
              {docentes.map((d) => (
                <MenuItem key={d.id} value={String(d.id)}>{d.apellido}, {d.nombre}</MenuItem>
              ))}
            </TextField>
            <Button
              variant="contained"
              onClick={crear}
              disabled={!nombre.trim() || !responsableNuevo}
              sx={{ mt: 0.5 }}
            >
              Crear
            </Button>
          </Box>
        </CardContent>
      </Card>

      {seleccionado && (
        <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
          <CardContent>
            <Typography variant="h6" sx={{ fontWeight: 600 }}>{seleccionado.nombre}</Typography>

            <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start', my: 2 }}>
              <TextField
                select
                label="Profesor responsable"
                required
                value={responsableSel}
                onChange={(e) => setResponsableSel(e.target.value)}
                size="small"
                sx={{ minWidth: 280 }}
              >
                {docentes.map((d) => (
                  <MenuItem key={d.id} value={String(d.id)}>{d.apellido}, {d.nombre}</MenuItem>
                ))}
              </TextField>
              <Button variant="outlined" onClick={guardarResponsable} disabled={!responsableSel}>
                Guardar responsable
              </Button>
            </Box>

            <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Grupos</Typography>
            <List dense>
              {seleccionado.grupos.map((g) => (
                <ListItemText key={g.id} sx={{ pl: 1 }} primary={`${g.nivel} · ${g.horario}`} />
              ))}
              {seleccionado.grupos.length === 0 && (
                <Typography variant="body2" sx={{ pl: 1, color: '#666' }}>Sin grupos todavía.</Typography>
              )}
            </List>

            <Divider sx={{ my: 2 }} />
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr 1fr 1fr auto' }, gap: 2, alignItems: 'flex-start' }}>
              <TextField
                select
                label="Nivel"
                value={grupo.nivelId}
                onChange={(e) => setGrupo({ ...grupo, nivelId: e.target.value })}
                error={!!erroresGrupo.nivelId}
                helperText={erroresGrupo.nivelId ?? ' '}
                size="small"
              >
                {niveles.map((n) => (
                  <MenuItem key={n.id} value={String(n.id)}>{n.nombre}</MenuItem>
                ))}
              </TextField>
              <TextField
                select
                label="Día"
                value={grupo.dia}
                onChange={(e) => setGrupo({ ...grupo, dia: e.target.value })}
                error={!!erroresGrupo.diaSemana}
                helperText={erroresGrupo.diaSemana ?? ' '}
                size="small"
              >
                {DIAS.map((d, i) => (
                  <MenuItem key={d} value={String(i + 1)}>{d}</MenuItem>
                ))}
              </TextField>
              <TextField
                type="time"
                label="Inicio"
                value={grupo.inicio}
                onChange={(e) => setGrupo({ ...grupo, inicio: e.target.value })}
                error={!!erroresGrupo.horaInicio}
                helperText={erroresGrupo.horaInicio ?? ' '}
                size="small"
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <TextField
                type="time"
                label="Fin"
                value={grupo.fin}
                onChange={(e) => setGrupo({ ...grupo, fin: e.target.value })}
                error={!!erroresGrupo.horaFin}
                helperText={erroresGrupo.horaFin ?? ' '}
                size="small"
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <Button
                variant="contained"
                onClick={agregarGrupo}
                disabled={!grupo.nivelId || !grupo.inicio || !grupo.fin}
                sx={{ mt: 0.5 }}
              >
                Agregar grupo
              </Button>
            </Box>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};
