import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  MenuItem,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import { docenteService } from '../services/docenteService';
import type { DocenteAgenda, DocenteDTO, DocenteFormulario } from '../services/docenteService';

const ESPECIALIDADES = [
  'Matemática',
  'Lengua',
  'Ciencias Sociales',
  'Ciencias Naturales',
  'Inglés',
  'Educación Física',
  'Artes',
];

const ESTADOS: { valor: DocenteDTO['estado']; etiqueta: string }[] = [
  { valor: 'ACTIVO', etiqueta: 'Activo' },
  { valor: 'INACTIVO', etiqueta: 'Inactivo' },
  { valor: 'LICENCIA', etiqueta: 'Licencia' },
];

const FORMULARIO_VACIO: DocenteFormulario = {
  legajo: '',
  dni: '',
  nombre: '',
  apellido: '',
  especialidad: '',
  email: '',
  telefono: '',
  estado: 'ACTIVO',
};

interface DocenteDialogProps {
  open: boolean;
  docente: DocenteDTO | null;
  onClose: () => void;
  onGuardado: (docente: DocenteDTO, esNuevo: boolean) => void;
}

export const DocenteDialog: React.FC<DocenteDialogProps> = ({ open, docente, onClose, onGuardado }) => {
  const [form, setForm] = useState<DocenteFormulario>(FORMULARIO_VACIO);
  const [errores, setErrores] = useState<Record<string, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [guardando, setGuardando] = useState(false);
  const [pestana, setPestana] = useState<'datos' | 'agenda'>('datos');
  const [agenda, setAgenda] = useState<DocenteAgenda | null>(null);

  useEffect(() => {
    if (!open) return;
    setPestana('datos');
    setAgenda(null);
    if (docente) {
      docenteService.agenda(docente.id).then(setAgenda).catch(() => setAgenda(null));
    }
    setErrores({});
    setError(null);
    setForm(docente
      ? {
          legajo: docente.legajo,
          dni: docente.dni,
          nombre: docente.nombre,
          apellido: docente.apellido,
          especialidad: docente.especialidad ?? '',
          email: docente.email ?? '',
          telefono: docente.telefono ?? '',
          estado: docente.estado,
        }
      : FORMULARIO_VACIO);
  }, [open, docente]);

  const cambiar = (campo: keyof DocenteFormulario, valor: string) => {
    setForm((previo) => ({ ...previo, [campo]: valor }));
  };

  const guardar = async () => {
    setGuardando(true);
    setErrores({});
    setError(null);
    try {
      const guardado = docente
        ? await docenteService.actualizar(docente.id, form)
        : await docenteService.crear(form);
      onGuardado(guardado, !docente);
    } catch (err: any) {
      const status = err.response?.status;
      if (status === 400) {
        setErrores(err.response.data?.errores ?? {});
        setError(err.response.data?.message ?? 'Revisá los datos ingresados');
      } else if (status === 423) {
        setError(err.response.data?.message ?? 'La edición está bloqueada temporalmente');
      } else {
        setError(err.response?.data?.message ?? 'No se pudo guardar el docente');
      }
    } finally {
      setGuardando(false);
    }
  };

  const campo = (nombre: keyof DocenteFormulario, etiqueta: string, deshabilitado = false, ayuda?: string) => (
    <TextField
      label={etiqueta}
      value={form[nombre]}
      onChange={(e) => cambiar(nombre, e.target.value)}
      disabled={deshabilitado || guardando}
      error={!!errores[nombre]}
      helperText={errores[nombre] ?? ayuda ?? ' '}
      size="small"
      fullWidth
    />
  );

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle>
        {docente ? `${docente.apellido}, ${docente.nombre}` : 'Nuevo docente'}
        {docente && (
          <Typography variant="body2" sx={{ color: '#666' }}>
            Legajo {docente.legajo} · {docente.estado}
          </Typography>
        )}
      </DialogTitle>

      {docente && (
        <Tabs value={pestana} onChange={(_e, nueva: 'datos' | 'agenda') => setPestana(nueva)} sx={{ px: 3 }}>
          <Tab value="datos" label="Datos" />
          <Tab value="agenda" label="Agenda" />
        </Tabs>
      )}

      <DialogContent dividers>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        {pestana === 'agenda' && (
          <Box>
            {!agenda && <Typography variant="body2" sx={{ color: '#666' }}>Cargando agenda…</Typography>}
            {agenda && agenda.cursos.length === 0 && (
              <Typography variant="body2" sx={{ color: '#666' }}>Este docente todavía no tiene cursos asignados.</Typography>
            )}
            {agenda?.cursos.map((curso, indice) => (
              <Box key={curso.cursoId} sx={{ mb: 2 }}>
                {indice > 0 && <Divider sx={{ mb: 2 }} />}
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>{curso.curso}</Typography>
                  <Chip size="small" label={curso.nivel} variant="outlined" />
                </Box>
                {curso.materias.map((materia) => (
                  <Box key={materia.materia} sx={{ pl: 2, mb: 1 }}>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>{materia.materia}</Typography>
                    <Typography variant="body2" sx={{ color: '#555' }}>
                      {materia.horarios.length ? materia.horarios.join(' · ') : 'Sin horarios cargados'}
                    </Typography>
                  </Box>
                ))}
              </Box>
            ))}
          </Box>
        )}

        {pestana === 'datos' && <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, pt: 1 }}>
          {campo('legajo', 'Legajo', !!docente)}
          {campo('dni', 'DNI', !!docente && !docente.dniEditable,
            docente && !docente.dniEditable ? 'No editable: el docente tiene usuario asociado' : undefined)}
          {campo('nombre', 'Nombre')}
          {campo('apellido', 'Apellido')}
          <TextField
            select
            label="Especialidad"
            value={form.especialidad}
            onChange={(e) => cambiar('especialidad', e.target.value)}
            disabled={guardando}
            error={!!errores.especialidad}
            helperText={errores.especialidad ?? ' '}
            size="small"
            fullWidth
          >
            {ESPECIALIDADES.map((esp) => (
              <MenuItem key={esp} value={esp}>{esp}</MenuItem>
            ))}
          </TextField>
          {campo('email', 'Correo electrónico')}
          {campo('telefono', 'Teléfono')}
          <TextField
            select
            label="Estado"
            value={form.estado}
            onChange={(e) => cambiar('estado', e.target.value)}
            disabled={guardando}
            error={!!errores.estado}
            helperText={errores.estado ?? ' '}
            size="small"
            fullWidth
          >
            {ESTADOS.map((estado) => (
              <MenuItem key={estado.valor} value={estado.valor}>{estado.etiqueta}</MenuItem>
            ))}
          </TextField>
        </Box>}
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button onClick={onClose} disabled={guardando}>Cerrar</Button>
        {pestana === 'datos' && (
          <Button variant="contained" onClick={guardar} disabled={guardando}>
            {docente ? 'Guardar cambios' : 'Dar de alta'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};
