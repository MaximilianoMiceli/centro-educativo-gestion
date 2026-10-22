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
  Typography,
} from '@mui/material';
import { adminService, ROLES, TIPOS_DOCUMENTO } from '../services/adminService';
import type { CrearUsuarioDatos, RolNombre, UsuarioCreado } from '../services/adminService';

interface UsuarioAltaProps {
  token: string;
}

const FORMULARIO_VACIO: CrearUsuarioDatos = {
  username: '',
  email: '',
  tipoDocumento: 'DNI',
  numeroDocumento: '',
  rol: 'PROFESOR',
};

export const UsuarioAlta: React.FC<UsuarioAltaProps> = ({ token }) => {
  const [form, setForm] = useState<CrearUsuarioDatos>(FORMULARIO_VACIO);
  const [errores, setErrores] = useState<Record<string, string>>({});
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [creado, setCreado] = useState<UsuarioCreado | null>(null);
  const [copiada, setCopiada] = useState(false);
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    adminService.setToken(token);
  }, [token]);

  const cambiar = (campo: keyof CrearUsuarioDatos, valor: string) => {
    setForm((previo) => ({ ...previo, [campo]: valor }));
  };

  const crear = async () => {
    setCargando(true);
    setMensaje(null);
    setErrores({});
    try {
      const nuevo = await adminService.crearUsuario(form);
      setCreado(nuevo);
      setCopiada(false);
      setForm(FORMULARIO_VACIO);
    } catch (err: any) {
      const status = err.response?.status;
      const data = err.response?.data;
      if (status === 400) {
        setErrores(data?.errores ?? {});
        setMensaje(data?.message ?? 'Revisá los datos ingresados');
      } else {
        setMensaje(data?.message ?? 'No se pudo crear el usuario');
      }
    } finally {
      setCargando(false);
    }
  };

  const copiarPassword = async () => {
    if (!creado) return;
    try {
      await navigator.clipboard.writeText(creado.passwordTemporal);
      setCopiada(true);
    } catch {
      setCopiada(false);
    }
  };

  const campo = (nombre: keyof CrearUsuarioDatos, etiqueta: string, ayuda?: string) => (
    <TextField
      label={etiqueta}
      value={form[nombre]}
      onChange={(e) => cambiar(nombre, e.target.value)}
      error={!!errores[nombre]}
      helperText={errores[nombre] ?? ayuda ?? ' '}
      disabled={cargando}
      fullWidth
      size="small"
    />
  );

  return (
    <Box>
      <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="h5" sx={{ mb: 2, fontWeight: 600 }}>
            👤 Crear usuario
          </Typography>

          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
            {campo('username', 'Username', '5 a 20 caracteres, sin espacios')}
            {campo('email', 'Correo electrónico')}
            <TextField
              select
              label="Tipo de documento"
              value={form.tipoDocumento}
              onChange={(e) => cambiar('tipoDocumento', e.target.value)}
              error={!!errores.tipoDocumento}
              helperText={errores.tipoDocumento ?? ' '}
              disabled={cargando}
              fullWidth
              size="small"
            >
              {TIPOS_DOCUMENTO.map((tipo) => (
                <MenuItem key={tipo} value={tipo}>
                  {tipo}
                </MenuItem>
              ))}
            </TextField>
            {campo('numeroDocumento', 'Número de documento')}
            <TextField
              select
              label="Rol"
              value={form.rol}
              onChange={(e) => cambiar('rol', e.target.value as RolNombre)}
              error={!!errores.rol}
              helperText={errores.rol ?? ' '}
              disabled={cargando}
              fullWidth
              size="small"
            >
              {ROLES.map((rol) => (
                <MenuItem key={rol} value={rol}>
                  {rol}
                </MenuItem>
              ))}
            </TextField>
          </Box>

          <Button
            fullWidth
            variant="contained"
            onClick={crear}
            disabled={cargando}
            sx={{ mt: 3, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', fontWeight: 600 }}
          >
            {cargando ? <CircularProgress size={24} color="inherit" /> : 'Crear usuario'}
          </Button>
        </CardContent>
      </Card>

      {mensaje && (
        <Alert severity="error" sx={{ mt: 2 }} onClose={() => setMensaje(null)}>
          {mensaje}
        </Alert>
      )}

      {creado && (
        <Alert severity="success" sx={{ mt: 2 }} onClose={() => setCreado(null)}>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            Usuario {creado.username} creado con rol {creado.rol}.
          </Typography>
          <Typography variant="body2" sx={{ mt: 1 }}>
            Contraseña temporal (se muestra una sola vez; el usuario deberá cambiarla al ingresar):
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mt: 1 }}>
            <Typography sx={{ fontFamily: 'monospace', fontSize: '1.1rem', fontWeight: 700 }}>
              {creado.passwordTemporal}
            </Typography>
            <Button size="small" variant="outlined" onClick={copiarPassword}>
              {copiada ? 'Copiada' : 'Copiar'}
            </Button>
          </Box>
        </Alert>
      )}
    </Box>
  );
};
