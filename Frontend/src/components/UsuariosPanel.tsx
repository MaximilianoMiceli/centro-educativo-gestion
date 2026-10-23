import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Checkbox,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  MenuItem,
  Paper,
  Snackbar,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { adminService, ROLES, TIPOS_DOCUMENTO } from '../services/adminService';
import { alumnosAdminService } from '../services/alumnosAdminService';
import type { CrearUsuarioDatos, RolNombre, UsuarioLista } from '../services/adminService';

interface UsuariosPanelProps {
  token: string;
}

const FORMULARIO_VACIO: CrearUsuarioDatos = {
  username: '',
  email: '',
  tipoDocumento: 'DNI',
  numeroDocumento: '',
  rol: 'PROFESOR',
  password: '',
  debeCambiarPassword: true,
};

type Errores = Record<string, string>;

export const UsuariosPanel: React.FC<UsuariosPanelProps> = ({ token }) => {
  const [usuarios, setUsuarios] = useState<UsuarioLista[]>([]);
  const [form, setForm] = useState<CrearUsuarioDatos>(FORMULARIO_VACIO);
  const [repetir, setRepetir] = useState('');
  const [errores, setErrores] = useState<Errores>({});
  const [error, setError] = useState<string | null>(null);
  const [guardando, setGuardando] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [restablecer, setRestablecer] = useState<UsuarioLista | null>(null);
  const [nuevaPassword, setNuevaPassword] = useState('');
  const [nuevaRepetir, setNuevaRepetir] = useState('');
  const [nuevaDebeCambiar, setNuevaDebeCambiar] = useState(true);
  const [errorRestablecer, setErrorRestablecer] = useState<string | null>(null);
  const [hijosDe, setHijosDe] = useState<UsuarioLista | null>(null);
  const [alumnosTodos, setAlumnosTodos] = useState<{ id: number; legajo: string; alumno: string }[]>([]);
  const [seleccionHijos, setSeleccionHijos] = useState<number[]>([]);

  const cargar = () => adminService.listarUsuarios().then(setUsuarios);

  const abrirHijos = async (usuario: UsuarioLista) => {
    setHijosDe(usuario);
    const [hijos, alumnos] = await Promise.all([
      adminService.hijos(usuario.id),
      alumnosAdminService.listar(),
    ]);
    setSeleccionHijos(hijos.map((h) => h.id));
    setAlumnosTodos(alumnos.map((a) => ({ id: a.id, legajo: a.legajo, alumno: `${a.apellido}, ${a.nombre}` })));
  };

  const guardarHijos = async () => {
    if (!hijosDe) return;
    await adminService.vincularHijos(hijosDe.id, seleccionHijos);
    setAviso(`Hijos de ${hijosDe.username} actualizados`);
    setHijosDe(null);
  };

  useEffect(() => {
    adminService.setToken(token);
    alumnosAdminService.setToken(token);
    cargar();
  }, [token]);

  const cambiar = (campo: keyof CrearUsuarioDatos, valor: string | boolean) => {
    setForm((previo) => ({ ...previo, [campo]: valor }));
  };

  const crear = async () => {
    setErrores({});
    setError(null);
    if (form.password !== repetir) {
      setErrores({ password: 'Las contraseñas no coinciden' });
      return;
    }
    setGuardando(true);
    try {
      const creado = await adminService.crearUsuario(form);
      setAviso(`Usuario ${creado.username} creado con rol ${creado.rol}`);
      setForm(FORMULARIO_VACIO);
      setRepetir('');
      cargar();
    } catch (err: any) {
      const status = err.response?.status;
      if (status === 400) {
        setErrores(err.response.data?.errores ?? {});
        setError(err.response.data?.message ?? 'Revisá los datos ingresados');
      } else {
        setError(err.response?.data?.message ?? 'No se pudo crear el usuario');
      }
    } finally {
      setGuardando(false);
    }
  };

  const abrirRestablecer = (usuario: UsuarioLista) => {
    setRestablecer(usuario);
    setNuevaPassword('');
    setNuevaRepetir('');
    setNuevaDebeCambiar(true);
    setErrorRestablecer(null);
  };

  const confirmarRestablecer = async () => {
    if (!restablecer) return;
    if (nuevaPassword !== nuevaRepetir) {
      setErrorRestablecer('Las contraseñas no coinciden');
      return;
    }
    try {
      await adminService.restablecerPassword(restablecer.id, nuevaPassword, nuevaDebeCambiar);
      setAviso(`Contraseña de ${restablecer.username} restablecida`);
      setRestablecer(null);
    } catch (err: any) {
      setErrorRestablecer(err.response?.data?.message ?? 'No se pudo restablecer la contraseña');
    }
  };

  const campo = (
    nombre: keyof CrearUsuarioDatos,
    etiqueta: string,
    opciones: { tipo?: string; ayuda?: string } = {},
  ) => (
    <TextField
      label={etiqueta}
      type={opciones.tipo ?? 'text'}
      value={form[nombre] as string}
      onChange={(e) => cambiar(nombre, e.target.value)}
      error={!!errores[nombre]}
      helperText={errores[nombre] ?? opciones.ayuda ?? ' '}
      disabled={guardando}
      size="small"
      fullWidth
    />
  );

  return (
    <Box>
      <Card sx={{ mb: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="h5" sx={{ mb: 2, fontWeight: 600 }}>Crear usuario</Typography>

          {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
            {campo('username', 'Username', { ayuda: '5 a 20 caracteres, sin espacios' })}
            {campo('email', 'Correo electrónico')}
            <TextField
              select
              label="Tipo de documento"
              value={form.tipoDocumento}
              onChange={(e) => cambiar('tipoDocumento', e.target.value)}
              error={!!errores.tipoDocumento}
              helperText={errores.tipoDocumento ?? ' '}
              size="small"
              fullWidth
            >
              {TIPOS_DOCUMENTO.map((tipo) => (
                <MenuItem key={tipo} value={tipo}>{tipo}</MenuItem>
              ))}
            </TextField>
            {campo('numeroDocumento', 'Número de documento', {
              ayuda: 'Debe coincidir con un docente, alumno o padre si el rol lo requiere',
            })}
            <TextField
              select
              label="Rol"
              value={form.rol}
              onChange={(e) => cambiar('rol', e.target.value as RolNombre)}
              error={!!errores.rol}
              helperText={errores.rol ?? ' '}
              size="small"
              fullWidth
            >
              {ROLES.map((rol) => (
                <MenuItem key={rol} value={rol}>{rol}</MenuItem>
              ))}
            </TextField>
            <Box />
            <TextField
              label="Contraseña inicial"
              type="password"
              value={form.password}
              onChange={(e) => cambiar('password', e.target.value)}
              error={!!errores.password}
              helperText={errores.password ?? '8 a 72 caracteres'}
              size="small"
              fullWidth
            />
            <TextField
              label="Repetir contraseña"
              type="password"
              value={repetir}
              onChange={(e) => setRepetir(e.target.value)}
              size="small"
              fullWidth
            />
          </Box>

          <FormControlLabel
            sx={{ mt: 1 }}
            control={
              <Checkbox
                checked={form.debeCambiarPassword}
                onChange={(e) => cambiar('debeCambiarPassword', e.target.checked)}
              />
            }
            label="Debe cambiarla al ingresar"
          />

          <Button
            variant="contained"
            onClick={crear}
            disabled={guardando || !form.username || !form.password || !repetir}
            sx={{ mt: 1, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', fontWeight: 600 }}
          >
            Crear usuario
          </Button>
        </CardContent>
      </Card>

      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>Usuarios</Typography>
      <TableContainer component={Paper} sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <Table size="small">
          <TableHead>
            <TableRow sx={{ '& th': { fontWeight: 700, backgroundColor: '#F5F6FA' } }}>
              <TableCell>Username</TableCell>
              <TableCell>Rol</TableCell>
              <TableCell>Documento</TableCell>
              <TableCell>Contraseña</TableCell>
              <TableCell align="right">Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {usuarios.map((u) => (
              <TableRow key={u.id} hover>
                <TableCell>{u.username}</TableCell>
                <TableCell>{u.rol}</TableCell>
                <TableCell>{u.numeroDocumento ?? '—'}</TableCell>
                <TableCell>
                  {u.debeCambiarPassword
                    ? <Chip size="small" color="warning" variant="outlined" label="Debe cambiarla" />
                    : <Chip size="small" color="success" variant="outlined" label="Vigente" />}
                </TableCell>
                <TableCell align="right">
                  {u.rol === 'PADRE' && (
                    <Button size="small" onClick={() => abrirHijos(u)}>Hijos</Button>
                  )}
                  <Button size="small" onClick={() => abrirRestablecer(u)}>Restablecer contraseña</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={!!restablecer} onClose={() => setRestablecer(null)} fullWidth maxWidth="xs">
        <DialogTitle>Restablecer contraseña de {restablecer?.username}</DialogTitle>
        <DialogContent>
          {errorRestablecer && <Alert severity="error" sx={{ mb: 2 }}>{errorRestablecer}</Alert>}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Nueva contraseña"
              type="password"
              value={nuevaPassword}
              onChange={(e) => setNuevaPassword(e.target.value)}
              helperText="8 a 72 caracteres"
              size="small"
              fullWidth
            />
            <TextField
              label="Repetir nueva contraseña"
              type="password"
              value={nuevaRepetir}
              onChange={(e) => setNuevaRepetir(e.target.value)}
              size="small"
              fullWidth
            />
            <FormControlLabel
              control={
                <Checkbox checked={nuevaDebeCambiar} onChange={(e) => setNuevaDebeCambiar(e.target.checked)} />
              }
              label="Debe cambiarla al ingresar"
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setRestablecer(null)}>Cancelar</Button>
          <Button variant="contained" onClick={confirmarRestablecer} disabled={!nuevaPassword || !nuevaRepetir}>
            Restablecer
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!hijosDe} onClose={() => setHijosDe(null)} fullWidth maxWidth="xs">
        <DialogTitle>Hijos de {hijosDe?.username}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column' }}>
            {alumnosTodos.map((a) => (
              <FormControlLabel
                key={a.id}
                control={
                  <Checkbox
                    checked={seleccionHijos.includes(a.id)}
                    onChange={(e) =>
                      setSeleccionHijos((previo) =>
                        e.target.checked ? [...previo, a.id] : previo.filter((x) => x !== a.id))
                    }
                  />
                }
                label={`${a.legajo} · ${a.alumno}`}
              />
            ))}
          </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setHijosDe(null)}>Cancelar</Button>
          <Button variant="contained" onClick={guardarHijos}>Guardar</Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={!!aviso}
        autoHideDuration={4000}
        onClose={() => setAviso(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        {aviso ? (
          <Alert severity="success" variant="filled" onClose={() => setAviso(null)}>{aviso}</Alert>
        ) : <span />}
      </Snackbar>
    </Box>
  );
};
