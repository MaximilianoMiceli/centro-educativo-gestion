import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  MenuItem,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material';
import { docenteService } from '../services/docenteService';
import type { DocenteDTO, DocenteEdicion as DocenteForm } from '../services/docenteService';

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

interface DocenteEdicionProps {
  token: string;
}

type Busqueda = 'legajo' | 'dni';

type Mensaje = { tipo: 'success' | 'error'; texto: string };

function aFormulario(docente: DocenteDTO): DocenteForm {
  return {
    dni: docente.dni,
    nombre: docente.nombre,
    apellido: docente.apellido,
    especialidad: docente.especialidad ?? '',
    email: docente.email ?? '',
    telefono: docente.telefono ?? '',
    estado: docente.estado,
  };
}

export const DocenteEdicion: React.FC<DocenteEdicionProps> = ({ token }) => {
  const [busqueda, setBusqueda] = useState<Busqueda>('legajo');
  const [valor, setValor] = useState('');
  const [docente, setDocente] = useState<DocenteDTO | null>(null);
  const [form, setForm] = useState<DocenteForm | null>(null);
  const [errores, setErrores] = useState<Record<string, string>>({});
  const [mensaje, setMensaje] = useState<Mensaje | null>(null);
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    docenteService.setToken(token);
  }, [token]);

  const seleccionar = (encontrado: DocenteDTO) => {
    setDocente(encontrado);
    setForm(aFormulario(encontrado));
    setErrores({});
  };

  const buscar = async () => {
    setCargando(true);
    setMensaje(null);
    try {
      const encontrado = busqueda === 'legajo'
        ? await docenteService.buscarPorLegajo(valor.trim())
        : await docenteService.buscarPorDni(valor.trim());
      seleccionar(encontrado);
    } catch (err: any) {
      setDocente(null);
      setForm(null);
      setMensaje({ tipo: 'error', texto: err.response?.data?.message ?? 'No se encontró el docente' });
    } finally {
      setCargando(false);
    }
  };

  const cambiarCampo = (campo: keyof DocenteForm, nuevoValor: string) => {
    setForm((previo) => (previo ? { ...previo, [campo]: nuevoValor } : previo));
  };

  const guardar = async () => {
    if (!docente || !form) return;
    setCargando(true);
    setMensaje(null);
    try {
      const actualizado = await docenteService.actualizar(docente.id, form);
      seleccionar(actualizado);
      setMensaje({ tipo: 'success', texto: 'Datos del docente actualizados exitosamente' });
    } catch (err: any) {
      const status = err.response?.status;
      const data = err.response?.data;
      if (status === 400) {
        setErrores(data?.errores ?? {});
        setMensaje({ tipo: 'error', texto: data?.message ?? 'Revisá los datos ingresados' });
      } else {
        setMensaje({ tipo: 'error', texto: data?.message ?? 'No se pudieron guardar los cambios' });
      }
    } finally {
      setCargando(false);
    }
  };

  const campoTexto = (campo: keyof DocenteForm, etiqueta: string, opciones: { disabled?: boolean; ayuda?: string } = {}) => (
    <TextField
      label={etiqueta}
      value={form?.[campo] ?? ''}
      onChange={(e) => cambiarCampo(campo, e.target.value)}
      disabled={cargando || opciones.disabled}
      error={!!errores[campo]}
      helperText={errores[campo] ?? opciones.ayuda ?? ' '}
      fullWidth
      size="small"
    />
  );

  return (
    <Box>
      <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="h5" sx={{ mb: 2, fontWeight: 600 }}>
            ✏️ Editar docente
          </Typography>

          <ToggleButtonGroup
            value={busqueda}
            exclusive
            fullWidth
            onChange={(_e, nuevo: Busqueda | null) => nuevo && setBusqueda(nuevo)}
            sx={{ mb: 2 }}
          >
            <ToggleButton value="legajo">Legajo</ToggleButton>
            <ToggleButton value="dni">DNI</ToggleButton>
          </ToggleButtonGroup>

          <TextField
            fullWidth
            size="small"
            placeholder={busqueda === 'legajo' ? 'Ej: DOC001' : 'Ej: 12345678'}
            value={valor}
            onChange={(e) => setValor(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && valor.trim()) buscar();
            }}
            disabled={cargando}
          />
          <Button fullWidth variant="contained" onClick={buscar} disabled={cargando || !valor.trim()} sx={{ mt: 2 }}>
            Buscar
          </Button>
        </CardContent>
      </Card>

      {mensaje && (
        <Alert severity={mensaje.tipo} sx={{ mt: 2 }} onClose={() => setMensaje(null)}>
          {mensaje.texto}
        </Alert>
      )}

      {docente && form && (
        <Card sx={{ mt: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
          <CardContent>
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
              <TextField label="Legajo" value={docente.legajo} disabled fullWidth size="small" helperText=" " />
              {campoTexto('dni', 'DNI', {
                disabled: !docente.dniEditable,
                ayuda: docente.dniEditable ? undefined : 'No editable: el docente tiene usuario asociado',
              })}
              {campoTexto('nombre', 'Nombre')}
              {campoTexto('apellido', 'Apellido')}
              <TextField
                select
                label="Especialidad"
                value={form.especialidad}
                onChange={(e) => cambiarCampo('especialidad', e.target.value)}
                disabled={cargando}
                error={!!errores.especialidad}
                helperText={errores.especialidad ?? ' '}
                fullWidth
                size="small"
              >
                {ESPECIALIDADES.map((esp) => (
                  <MenuItem key={esp} value={esp}>
                    {esp}
                  </MenuItem>
                ))}
              </TextField>
              {campoTexto('email', 'Correo electrónico')}
              {campoTexto('telefono', 'Teléfono')}
              <TextField
                select
                label="Estado"
                value={form.estado}
                onChange={(e) => cambiarCampo('estado', e.target.value)}
                disabled={cargando}
                error={!!errores.estado}
                helperText={errores.estado ?? ' '}
                fullWidth
                size="small"
              >
                {ESTADOS.map((estado) => (
                  <MenuItem key={estado.valor} value={estado.valor}>
                    {estado.etiqueta}
                  </MenuItem>
                ))}
              </TextField>
            </Box>

            <Typography variant="body2" sx={{ mt: 2, color: '#555' }}>
              Cursos a cargo: {docente.cursos.length ? docente.cursos.join(', ') : 'ninguno'}
            </Typography>

            <Button
              fullWidth
              variant="contained"
              onClick={guardar}
              disabled={cargando}
              sx={{ mt: 3, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', fontWeight: 600 }}
            >
              {cargando ? <CircularProgress size={24} color="inherit" /> : 'Guardar cambios'}
            </Button>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};
