import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Divider,
  List,
  ListItemButton,
  ListItemText,
  MenuItem,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import {
  datosMaestrosService,
  DIAS,
} from '../services/datosMaestrosService';
import type { CursoDTO, DictadoDTO, HorarioDTO, MateriaDTO, NivelDTO } from '../services/datosMaestrosService';
import { docenteService } from '../services/docenteService';
import type { DocenteDTO } from '../services/docenteService';

interface DatosMaestrosProps {
  token: string;
}

type Seccion = 'niveles' | 'cursos' | 'materias' | 'asignaciones';
type Errores = Record<string, string>;
type Aviso = { tipo: 'success' | 'error'; texto: string } | null;

function leerError(err: any): { mensaje: string; errores: Errores } {
  return {
    mensaje: err.response?.data?.message ?? 'No se pudo completar la operación',
    errores: err.response?.data?.errores ?? {},
  };
}

const TARJETA = { boxShadow: '0 2px 8px rgba(0,0,0,0.08)' };

function TarjetaSeccion({ children }: { children: React.ReactNode }) {
  return (
    <Card sx={{ ...TARJETA, mb: 3 }}>
      <CardContent>{children}</CardContent>
    </Card>
  );
}

function NivelesSeccion({ onAviso }: { onAviso: (a: Aviso) => void }) {
  const [niveles, setNiveles] = useState<NivelDTO[]>([]);
  const [nombre, setNombre] = useState('');
  const [errores, setErrores] = useState<Errores>({});

  const cargar = () => datosMaestrosService.listarNiveles().then(setNiveles);
  useEffect(() => {
    cargar();
  }, []);

  const crear = async () => {
    setErrores({});
    try {
      await datosMaestrosService.crearNivel(nombre);
      setNombre('');
      onAviso({ tipo: 'success', texto: 'Nivel creado' });
      cargar();
    } catch (err) {
      const { mensaje, errores: e } = leerError(err);
      setErrores(e);
      onAviso({ tipo: 'error', texto: mensaje });
    }
  };

  return (
    <TarjetaSeccion>
      <Typography variant="h6" sx={{ fontWeight: 600 }}>Niveles educativos</Typography>
      <List dense>
        {niveles.map((n) => (
          <ListItemText key={n.id} primary={n.nombre} sx={{ pl: 2 }} />
        ))}
      </List>
      <Divider sx={{ my: 2 }} />
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
        <TextField
          label="Nuevo nivel"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          error={!!errores.nombre}
          helperText={errores.nombre ?? ' '}
          size="small"
          fullWidth
        />
        <Button variant="contained" onClick={crear} disabled={!nombre.trim()} sx={{ mt: 0.5 }}>
          Crear
        </Button>
      </Box>
    </TarjetaSeccion>
  );
}

function CursosSeccion({ onAviso }: { onAviso: (a: Aviso) => void }) {
  const [niveles, setNiveles] = useState<NivelDTO[]>([]);
  const [cursos, setCursos] = useState<CursoDTO[]>([]);
  const [filtroNivel, setFiltroNivel] = useState('');
  const [form, setForm] = useState({ nivelId: '', anio: '', seccion: '' });
  const [errores, setErrores] = useState<Errores>({});

  const cargar = () =>
    datosMaestrosService.listarCursos(filtroNivel ? Number(filtroNivel) : undefined).then(setCursos);

  useEffect(() => {
    datosMaestrosService.listarNiveles().then(setNiveles);
  }, []);

  useEffect(() => {
    cargar();
  }, [filtroNivel]);

  const crear = async () => {
    setErrores({});
    try {
      await datosMaestrosService.crearCurso({
        nivelId: Number(form.nivelId),
        anio: Number(form.anio),
        seccion: form.seccion,
      });
      setForm({ nivelId: form.nivelId, anio: '', seccion: '' });
      onAviso({ tipo: 'success', texto: 'Curso creado' });
      cargar();
    } catch (err) {
      const { mensaje, errores: e } = leerError(err);
      setErrores(e);
      onAviso({ tipo: 'error', texto: mensaje });
    }
  };

  return (
    <TarjetaSeccion>
      <Typography variant="h6" sx={{ fontWeight: 600 }}>Cursos</Typography>
      <TextField
        select
        label="Filtrar por nivel"
        value={filtroNivel}
        onChange={(e) => setFiltroNivel(e.target.value)}
        size="small"
        sx={{ mt: 1, mb: 1, minWidth: 220 }}
      >
        <MenuItem value="">Todos</MenuItem>
        {niveles.map((n) => (
          <MenuItem key={n.id} value={String(n.id)}>{n.nombre}</MenuItem>
        ))}
      </TextField>
      <List dense>
        {cursos.map((c) => (
          <ListItemText key={c.id} primary={c.descripcion} secondary={c.nivel} sx={{ pl: 2 }} />
        ))}
      </List>
      <Divider sx={{ my: 2 }} />
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr 1fr auto' }, gap: 2, alignItems: 'flex-start' }}>
        <TextField
          select
          label="Nivel"
          value={form.nivelId}
          onChange={(e) => setForm({ ...form, nivelId: e.target.value })}
          error={!!errores.nivelId}
          helperText={errores.nivelId ?? ' '}
          size="small"
        >
          {niveles.map((n) => (
            <MenuItem key={n.id} value={String(n.id)}>{n.nombre}</MenuItem>
          ))}
        </TextField>
        <TextField
          select
          label="Año"
          value={form.anio}
          onChange={(e) => setForm({ ...form, anio: e.target.value })}
          error={!!errores.anio}
          helperText={errores.anio ?? ' '}
          size="small"
        >
          {[1, 2, 3, 4, 5, 6, 7].map((a) => (
            <MenuItem key={a} value={String(a)}>{a}</MenuItem>
          ))}
        </TextField>
        <TextField
          label="Sección"
          value={form.seccion}
          onChange={(e) => setForm({ ...form, seccion: e.target.value })}
          error={!!errores.seccion}
          helperText={errores.seccion ?? ' '}
          size="small"
        />
        <Button
          variant="contained"
          onClick={crear}
          disabled={!form.nivelId || !form.anio || !form.seccion.trim()}
          sx={{ mt: 0.5 }}
        >
          Crear
        </Button>
      </Box>
    </TarjetaSeccion>
  );
}

function MateriasSeccion({ onAviso }: { onAviso: (a: Aviso) => void }) {
  const [materias, setMaterias] = useState<MateriaDTO[]>([]);
  const [nombre, setNombre] = useState('');
  const [errores, setErrores] = useState<Errores>({});

  const cargar = () => datosMaestrosService.listarMaterias().then(setMaterias);
  useEffect(() => {
    cargar();
  }, []);

  const crear = async () => {
    setErrores({});
    try {
      await datosMaestrosService.crearMateria(nombre);
      setNombre('');
      onAviso({ tipo: 'success', texto: 'Materia creada' });
      cargar();
    } catch (err) {
      const { mensaje, errores: e } = leerError(err);
      setErrores(e);
      onAviso({ tipo: 'error', texto: mensaje });
    }
  };

  return (
    <TarjetaSeccion>
      <Typography variant="h6" sx={{ fontWeight: 600 }}>Materias</Typography>
      <List dense>
        {materias.map((m) => (
          <ListItemText key={m.id} primary={m.nombre} sx={{ pl: 2 }} />
        ))}
      </List>
      <Divider sx={{ my: 2 }} />
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
        <TextField
          label="Nueva materia"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          error={!!errores.nombre}
          helperText={errores.nombre ?? ' '}
          size="small"
          fullWidth
        />
        <Button variant="contained" onClick={crear} disabled={!nombre.trim()} sx={{ mt: 0.5 }}>
          Crear
        </Button>
      </Box>
    </TarjetaSeccion>
  );
}

function AsignacionesSeccion({ onAviso }: { onAviso: (a: Aviso) => void }) {
  const [docentes, setDocentes] = useState<DocenteDTO[]>([]);
  const [materias, setMaterias] = useState<MateriaDTO[]>([]);
  const [cursos, setCursos] = useState<CursoDTO[]>([]);
  const [asignaciones, setAsignaciones] = useState<DictadoDTO[]>([]);
  const [form, setForm] = useState({ docenteId: '', materiaId: '', cursoId: '' });
  const [errores, setErrores] = useState<Errores>({});
  const [seleccionada, setSeleccionada] = useState<DictadoDTO | null>(null);
  const [horarios, setHorarios] = useState<HorarioDTO[]>([]);
  const [horario, setHorario] = useState({ dia: '1', inicio: '', fin: '' });
  const [erroresHorario, setErroresHorario] = useState<Errores>({});

  const cargarAsignaciones = () => datosMaestrosService.listarAsignaciones().then(setAsignaciones);

  useEffect(() => {
    docenteService.listar().then(setDocentes);
    datosMaestrosService.listarMaterias().then(setMaterias);
    datosMaestrosService.listarCursos().then(setCursos);
    cargarAsignaciones();
  }, []);

  useEffect(() => {
    if (seleccionada) {
      datosMaestrosService.listarHorarios(seleccionada.id).then(setHorarios);
    } else {
      setHorarios([]);
    }
  }, [seleccionada]);

  const asignar = async () => {
    setErrores({});
    try {
      await datosMaestrosService.asignar({
        docenteId: Number(form.docenteId),
        materiaId: Number(form.materiaId),
        cursoId: Number(form.cursoId),
      });
      setForm({ docenteId: '', materiaId: '', cursoId: '' });
      onAviso({ tipo: 'success', texto: 'Materia asignada al docente' });
      cargarAsignaciones();
    } catch (err) {
      const { mensaje, errores: e } = leerError(err);
      setErrores(e);
      onAviso({ tipo: 'error', texto: mensaje });
    }
  };

  const crearHorario = async () => {
    if (!seleccionada) return;
    setErroresHorario({});
    try {
      await datosMaestrosService.crearHorario({
        dictadoId: seleccionada.id,
        diaSemana: Number(horario.dia),
        horaInicio: horario.inicio,
        horaFin: horario.fin,
      });
      setHorario({ dia: horario.dia, inicio: '', fin: '' });
      onAviso({ tipo: 'success', texto: 'Horario registrado' });
      datosMaestrosService.listarHorarios(seleccionada.id).then(setHorarios);
    } catch (err) {
      const { mensaje, errores: e } = leerError(err);
      setErroresHorario(e);
      onAviso({ tipo: 'error', texto: mensaje });
    }
  };

  return (
    <>
      <TarjetaSeccion>
        <Typography variant="h6" sx={{ fontWeight: 600 }}>Asignar materia a docente</Typography>
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr 1fr auto' }, gap: 2, mt: 1, alignItems: 'flex-start' }}>
          <TextField
            select
            label="Docente"
            value={form.docenteId}
            onChange={(e) => setForm({ ...form, docenteId: e.target.value })}
            error={!!errores.docenteId}
            helperText={errores.docenteId ?? ' '}
            size="small"
          >
            {docentes.map((d) => (
              <MenuItem key={d.id} value={String(d.id)}>{d.apellido}, {d.nombre}</MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Materia"
            value={form.materiaId}
            onChange={(e) => setForm({ ...form, materiaId: e.target.value })}
            error={!!errores.materiaId}
            helperText={errores.materiaId ?? ' '}
            size="small"
          >
            {materias.map((m) => (
              <MenuItem key={m.id} value={String(m.id)}>{m.nombre}</MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Curso"
            value={form.cursoId}
            onChange={(e) => setForm({ ...form, cursoId: e.target.value })}
            error={!!errores.cursoId}
            helperText={errores.cursoId ?? ' '}
            size="small"
          >
            {cursos.map((c) => (
              <MenuItem key={c.id} value={String(c.id)}>{c.descripcion}</MenuItem>
            ))}
          </TextField>
          <Button
            variant="contained"
            onClick={asignar}
            disabled={!form.docenteId || !form.materiaId || !form.cursoId}
            sx={{ mt: 0.5 }}
          >
            Asignar
          </Button>
        </Box>
      </TarjetaSeccion>

      <TarjetaSeccion>
        <Typography variant="h6" sx={{ fontWeight: 600 }}>Asignaciones y horarios</Typography>
        <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>
          Elegí una asignación para cargarle sus horarios.
        </Typography>
        <List dense sx={{ maxHeight: 260, overflow: 'auto' }}>
          {asignaciones.map((a) => (
            <ListItemButton
              key={a.id}
              selected={seleccionada?.id === a.id}
              onClick={() => setSeleccionada(a)}
            >
              <ListItemText primary={`${a.materia} · ${a.curso}`} secondary={a.docente} />
            </ListItemButton>
          ))}
        </List>

        {seleccionada && (
          <>
            <Divider sx={{ my: 2 }} />
            <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
              Horarios de {seleccionada.materia} en {seleccionada.curso}
            </Typography>
            <List dense>
              {horarios.map((h) => (
                <ListItemText
                  key={h.id}
                  sx={{ pl: 2 }}
                  primary={`${DIAS[h.diaSemana - 1]} ${h.horaInicio.slice(0, 5)} a ${h.horaFin.slice(0, 5)}`}
                />
              ))}
              {horarios.length === 0 && (
                <Typography variant="body2" sx={{ pl: 2, color: '#666' }}>Sin horarios cargados.</Typography>
              )}
            </List>
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr 1fr auto' }, gap: 2, mt: 2, alignItems: 'flex-start' }}>
              <TextField
                select
                label="Día"
                value={horario.dia}
                onChange={(e) => setHorario({ ...horario, dia: e.target.value })}
                error={!!erroresHorario.diaSemana}
                helperText={erroresHorario.diaSemana ?? ' '}
                size="small"
              >
                {DIAS.map((d, i) => (
                  <MenuItem key={d} value={String(i + 1)}>{d}</MenuItem>
                ))}
              </TextField>
              <TextField
                type="time"
                label="Inicio"
                value={horario.inicio}
                onChange={(e) => setHorario({ ...horario, inicio: e.target.value })}
                error={!!erroresHorario.horaInicio}
                helperText={erroresHorario.horaInicio ?? ' '}
                size="small"
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <TextField
                type="time"
                label="Fin"
                value={horario.fin}
                onChange={(e) => setHorario({ ...horario, fin: e.target.value })}
                error={!!erroresHorario.horaFin}
                helperText={erroresHorario.horaFin ?? ' '}
                size="small"
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <Button
                variant="contained"
                onClick={crearHorario}
                disabled={!horario.inicio || !horario.fin}
                sx={{ mt: 0.5 }}
              >
                Agregar
              </Button>
            </Box>
          </>
        )}
      </TarjetaSeccion>
    </>
  );
}

export const DatosMaestros: React.FC<DatosMaestrosProps> = ({ token }) => {
  const [seccion, setSeccion] = useState<Seccion>('niveles');
  const [aviso, setAviso] = useState<Aviso>(null);

  useEffect(() => {
    datosMaestrosService.setToken(token);
    docenteService.setToken(token);
  }, [token]);

  return (
    <Box>
      <Tabs
        value={seccion}
        onChange={(_e, nueva: Seccion) => {
          setAviso(null);
          setSeccion(nueva);
        }}
        variant="scrollable"
        scrollButtons="auto"
        sx={{ mb: 3 }}
      >
        <Tab value="niveles" label="Niveles" />
        <Tab value="cursos" label="Cursos" />
        <Tab value="materias" label="Materias" />
        <Tab value="asignaciones" label="Asignaciones" />
      </Tabs>

      {aviso && (
        <Alert severity={aviso.tipo} sx={{ mb: 2 }} onClose={() => setAviso(null)}>
          {aviso.texto}
        </Alert>
      )}

      {seccion === 'niveles' && <NivelesSeccion onAviso={setAviso} />}
      {seccion === 'cursos' && <CursosSeccion onAviso={setAviso} />}
      {seccion === 'materias' && <MateriasSeccion onAviso={setAviso} />}
      {seccion === 'asignaciones' && <AsignacionesSeccion onAviso={setAviso} />}
    </Box>
  );
};
