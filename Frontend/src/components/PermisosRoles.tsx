import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Checkbox,
  CircularProgress,
  FormControlLabel,
  Typography,
} from '@mui/material';
import { adminService } from '../services/adminService';
import type { PermisoDTO, RolNombre, RolPermisos } from '../services/adminService';

interface PermisosRolesProps {
  token: string;
}

const PERMISO_ADMIN = 'PERMISOS_ADMINISTRAR';

function iguales(a: string[], b: string[]): boolean {
  if (a.length !== b.length) return false;
  const ordenA = [...a].sort();
  const ordenB = [...b].sort();
  return ordenA.every((valor, indice) => valor === ordenB[indice]);
}

export const PermisosRoles: React.FC<PermisosRolesProps> = ({ token }) => {
  const [catalogo, setCatalogo] = useState<PermisoDTO[]>([]);
  const [roles, setRoles] = useState<RolPermisos[]>([]);
  const [seleccion, setSeleccion] = useState<Record<string, string[]>>({});
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState<RolNombre | null>(null);
  const [mensaje, setMensaje] = useState<{ tipo: 'success' | 'error'; texto: string } | null>(null);

  useEffect(() => {
    adminService.setToken(token);
    Promise.all([adminService.listarPermisos(), adminService.listarRoles()])
      .then(([permisos, rolesCargados]) => {
        setCatalogo(permisos);
        setRoles(rolesCargados);
        setSeleccion(Object.fromEntries(rolesCargados.map((r) => [r.nombre, r.permisos])));
      })
      .catch(() => setMensaje({ tipo: 'error', texto: 'No se pudieron cargar los permisos' }))
      .finally(() => setCargando(false));
  }, [token]);

  const alternar = (rol: string, codigo: string, marcado: boolean) => {
    setSeleccion((previo) => {
      const actuales = previo[rol] ?? [];
      return {
        ...previo,
        [rol]: marcado ? [...actuales, codigo] : actuales.filter((c) => c !== codigo),
      };
    });
  };

  const guardar = async (rol: RolNombre) => {
    setGuardando(rol);
    setMensaje(null);
    try {
      const actualizado = await adminService.actualizarPermisos(rol, seleccion[rol] ?? []);
      setRoles((previo) => previo.map((r) => (r.nombre === rol ? actualizado : r)));
      setSeleccion((previo) => ({ ...previo, [rol]: actualizado.permisos }));
      setMensaje({ tipo: 'success', texto: `Permisos del rol ${rol} actualizados. Aplican desde el próximo request.` });
    } catch (err: any) {
      setMensaje({ tipo: 'error', texto: err.response?.data?.message ?? 'No se pudieron guardar los permisos' });
    } finally {
      setGuardando(null);
    }
  };

  if (cargando) {
    return <CircularProgress />;
  }

  return (
    <Box>
      {mensaje && (
        <Alert severity={mensaje.tipo} sx={{ mb: 2 }} onClose={() => setMensaje(null)}>
          {mensaje.texto}
        </Alert>
      )}

      {roles.map((rol) => {
        const seleccionados = seleccion[rol.nombre] ?? [];
        const cambio = !iguales(seleccionados, rol.permisos);
        return (
          <Card key={rol.nombre} sx={{ mb: 2, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                {rol.nombre}
              </Typography>

              <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, mt: 1 }}>
                {catalogo.map((permiso) => {
                  const bloqueado = rol.nombre === 'ADMIN' && permiso.codigo === PERMISO_ADMIN;
                  return (
                    <FormControlLabel
                      key={permiso.codigo}
                      disabled={bloqueado || guardando !== null}
                      control={
                        <Checkbox
                          size="small"
                          checked={seleccionados.includes(permiso.codigo)}
                          onChange={(e) => alternar(rol.nombre, permiso.codigo, e.target.checked)}
                        />
                      }
                      label={
                        <Box>
                          <Typography variant="body2" sx={{ fontWeight: 600 }}>
                            {permiso.codigo}
                          </Typography>
                          <Typography variant="caption" sx={{ color: '#666' }}>
                            {permiso.descripcion}
                          </Typography>
                        </Box>
                      }
                    />
                  );
                })}
              </Box>

              <Button
                variant="contained"
                size="small"
                onClick={() => guardar(rol.nombre)}
                disabled={!cambio || guardando !== null}
                sx={{ mt: 2 }}
              >
                {guardando === rol.nombre ? <CircularProgress size={20} color="inherit" /> : 'Guardar permisos'}
              </Button>
            </CardContent>
          </Card>
        );
      })}
    </Box>
  );
};
