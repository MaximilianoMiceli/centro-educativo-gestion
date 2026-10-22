import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  List,
  ListItemText,
  MenuItem,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import { alumnosAdminService } from '../services/alumnosAdminService';
import type { AlumnoFicha, AlumnoMateria, DatosAlumno } from '../services/alumnosAdminService';
import type { CursoDTO } from '../services/datosMaestrosService';

interface AlumnoDialogProps {
  open: boolean;
  alumno: AlumnoFicha | null;
  cursos: CursoDTO[];
  editable: boolean;
  onClose: () => void;
  onGuardado: (alumno: AlumnoFicha, esNuevo: boolean) => void;
}

type Errores = Record<string, string>;

const FORMULARIO_VACIO: DatosAlumno = {
  legajo: '',
  dni: '',
  nombre: '',
  apellido: '',
  fechaNacimiento: '',
  domicilio: '',
  telefono: '',
  email: '',
  cursoId: 0,
  estado: 'ACTIVO',
};

function aFormulario(alumno: AlumnoFicha): DatosAlumno {
  return {
    legajo: alumno.legajo,
    dni: alumno.dni,
    nombre: alumno.nombre,
    apellido: alumno.apellido,
    fechaNacimiento: alumno.fechaNacimiento,
    domicilio: alumno.domicilio ?? '',
    telefono: alumno.telefono ?? '',
    email: alumno.email ?? '',
    cursoId: alumno.cursoId,
    estado: alumno.estado,
  };
}

export const AlumnoDialog: React.FC<AlumnoDialogProps> = ({ open, alumno, cursos, editable, onClose, onGuardado }) => {
  const [pestana, setPestana] = useState<'datos' | 'materias'>('datos');
  const [form, setForm] = useState<DatosAlumno>(FORMULARIO_VACIO);
  const [materias, setMaterias] = useState<AlumnoMateria[]>([]);
  const [errores, setErrores] = useState<Errores>({});
  const [error, setError] = useState<string | null>(null);
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    if (!open) return;
    setPestana('datos');
    setErrores({});
    setError(null);
    setForm(alumno ? aFormulario(alumno) : FORMULARIO_VACIO);
    setMaterias([]);
    if (alumno) {
      alumnosAdminService.materias(alumno.id).then(setMaterias).catch(() => setMaterias([]));
    }
  }, [open, alumno]);

  const cambiar = (campo: keyof DatosAlumno, valor: string | number) => {
    setForm((previo) => ({ ...previo, [campo]: valor }));
  };

  const guardar = async () => {
    setGuardando(true);
    setErrores({});
    setError(null);
    try {
      const guardado = alumno
        ? await alumnosAdminService.modificar(alumno.id, form)
        : await alumnosAdminService.crear(form);
      onGuardado(guardado, !alumno);
    } catch (err: any) {
      const status = err.response?.status;
      if (status === 400) {
        setErrores(err.response.data?.errores ?? {});
        setError(err.response.data?.message ?? 'Revisá los datos ingresados');
      } else {
        setError(err.response?.data?.message ?? 'No se pudo guardar el alumno');
      }
    } finally {
      setGuardando(false);
    }
  };

  const soloLectura = !editable;
  const campo = (nombre: keyof DatosAlumno, etiqueta: string, tipo = 'text', deshabilitado = false) => (
    <TextField
      label={etiqueta}
      type={tipo}
      value={form[nombre]}
      onChange={(e) => cambiar(nombre, e.target.value)}
      disabled={soloLectura || deshabilitado || guardando}
      error={!!errores[nombre]}
      helperText={errores[nombre] ?? ' '}
      size="small"
      fullWidth
      slotProps={tipo === 'date' ? { inputLabel: { shrink: true } } : undefined}
    />
  );

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle>
        {alumno ? `${alumno.apellido}, ${alumno.nombre}` : 'Nuevo alumno'}
        {alumno && (
          <Typography variant="body2" sx={{ color: '#666' }}>
            Legajo {alumno.legajo} · {alumno.cursoDescripcion} · {alumno.estado}
          </Typography>
        )}
      </DialogTitle>

      {alumno && (
        <Tabs value={pestana} onChange={(_e, nueva: 'datos' | 'materias') => setPestana(nueva)} sx={{ px: 3 }}>
          <Tab value="datos" label="Datos" />
          <Tab value="materias" label="Materias cursadas" />
        </Tabs>
      )}

      <DialogContent dividers>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        {pestana === 'datos' && (
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, pt: 1 }}>
            {campo('legajo', 'Legajo', 'text', !!alumno)}
            {campo('dni', 'DNI')}
            {campo('nombre', 'Nombre')}
            {campo('apellido', 'Apellido')}
            {campo('fechaNacimiento', 'Fecha de nacimiento', 'date')}
            <TextField
              select
              label="Curso"
              value={form.cursoId || ''}
              onChange={(e) => cambiar('cursoId', Number(e.target.value))}
              disabled={soloLectura || guardando}
              error={!!errores.cursoId}
              helperText={errores.cursoId ?? ' '}
              size="small"
              fullWidth
            >
              {cursos.map((c) => (
                <MenuItem key={c.id} value={c.id}>{c.descripcion}</MenuItem>
              ))}
            </TextField>
            {campo('domicilio', 'Domicilio')}
            {campo('telefono', 'Teléfono')}
            {campo('email', 'Email')}
            <TextField
              select
              label="Estado"
              value={form.estado}
              onChange={(e) => cambiar('estado', e.target.value)}
              disabled={soloLectura || guardando}
              error={!!errores.estado}
              helperText={errores.estado ?? ' '}
              size="small"
              fullWidth
            >
              <MenuItem value="ACTIVO">Activo</MenuItem>
              <MenuItem value="INACTIVO">Inactivo</MenuItem>
              <MenuItem value="EGRESADO">Egresado</MenuItem>
            </TextField>
          </Box>
        )}

        {pestana === 'materias' && (
          <List dense>
            {materias.length === 0 && (
              <Typography variant="body2" sx={{ color: '#666', p: 1 }}>
                Su curso todavía no tiene materias asignadas.
              </Typography>
            )}
            {materias.map((m) => (
              <ListItemText
                key={m.materia}
                sx={{ px: 1, py: 0.5 }}
                primary={`${m.materia} · ${m.docente}`}
                secondary={m.horarios.length ? m.horarios.join(' · ') : 'Sin horarios cargados'}
              />
            ))}
          </List>
        )}
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button onClick={onClose} disabled={guardando}>Cerrar</Button>
        {editable && pestana === 'datos' && (
          <Button variant="contained" onClick={guardar} disabled={guardando}>
            {alumno ? 'Guardar cambios' : 'Dar de alta'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};
