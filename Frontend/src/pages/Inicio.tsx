import React, { useEffect, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { Alert, Box, Button, Card, CardActionArea, CardContent, CircularProgress, Stack, Typography } from '@mui/material';
import { Add as AddIcon, Apartment, Badge, Groups, ManageAccounts, Assessment } from '@mui/icons-material';
import { alumnosAdminService } from '../services/alumnosAdminService';
import { adminService } from '../services/adminService';
import { docenteService } from '../services/docenteService';
import { instalacionesService } from '../services/instalacionesService';
import { rolDelToken } from '../utils/token';

interface InicioProps {
  token: string;
}

interface Indicadores {
  alumnosActivos: number;
  alumnosTotal: number;
  docentesActivos: number;
  docentesTotal: number;
  instalacionesActivas: number;
  instalacionesMantenimiento: number;
  usuarios: number;
}

interface Tarjeta {
  ruta: string;
  titulo: string;
  valor: number | string;
  detalle: string;
  icono: React.ReactNode;
}

export const Inicio: React.FC<InicioProps> = ({ token }) => {
  const [indicadores, setIndicadores] = useState<Indicadores | null>(null);
  const [error, setError] = useState('');
  const esAdmin = rolDelToken(token) === 'ADMIN';

  useEffect(() => {
    alumnosAdminService.setToken(token);
    adminService.setToken(token);
    docenteService.setToken(token);
    instalacionesService.setToken(token);

    Promise.all([
      alumnosAdminService.listar(),
      instalacionesService.listar(),
      esAdmin ? docenteService.listar() : Promise.resolve([]),
      esAdmin ? adminService.listarUsuarios() : Promise.resolve([]),
    ])
      .then(([alumnos, instalaciones, docentes, usuarios]) => {
        setIndicadores({
          alumnosActivos: alumnos.filter((a) => a.estado === 'ACTIVO').length,
          alumnosTotal: alumnos.length,
          docentesActivos: docentes.filter((d) => d.estado === 'ACTIVO').length,
          docentesTotal: docentes.length,
          instalacionesActivas: instalaciones.filter((i) => i.estado === 'ACTIVA').length,
          instalacionesMantenimiento: instalaciones.filter((i) => i.estado === 'EN_MANTENIMIENTO').length,
          usuarios: usuarios.length,
        });
      })
      .catch(() => setError('No se pudieron cargar los indicadores'));
  }, [token, esAdmin]);

  const tarjetas: Tarjeta[] = indicadores
    ? [
        {
          ruta: '/alumnos',
          titulo: 'Alumnos activos',
          valor: indicadores.alumnosActivos,
          detalle: `de ${indicadores.alumnosTotal} registrados`,
          icono: <Groups />,
        },
        ...(esAdmin
          ? [
              {
                ruta: '/docentes',
                titulo: 'Docentes activos',
                valor: indicadores.docentesActivos,
                detalle: `de ${indicadores.docentesTotal} registrados`,
                icono: <Badge />,
              },
              {
                ruta: '/usuarios',
                titulo: 'Cuentas de acceso',
                valor: indicadores.usuarios,
                detalle: 'usuarios del sistema',
                icono: <ManageAccounts />,
              },
            ]
          : []),
        {
          ruta: '/datos/instalaciones',
          titulo: 'Instalaciones activas',
          valor: indicadores.instalacionesActivas,
          detalle: `${indicadores.instalacionesMantenimiento} en mantenimiento`,
          icono: <Apartment />,
        },
      ]
    : [];

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 600, mb: 0.5 }}>Inicio</Typography>
      <Typography variant="body2" sx={{ color: '#666', mb: 3 }}>
        Resumen del centro y accesos rápidos a las tareas más frecuentes.
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {!indicadores && !error && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress />
        </Box>
      )}

      {indicadores && (
        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 2, mb: 4 }}>
          {tarjetas.map((t) => (
            <Card key={t.titulo} sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)', borderRadius: 2 }}>
              <CardActionArea component={RouterLink} to={t.ruta}>
                <CardContent>
                  <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', mb: 1.5, color: '#1a5fa8' }}>
                    {t.icono}
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#333' }}>{t.titulo}</Typography>
                  </Stack>
                  <Typography variant="h4" sx={{ fontWeight: 700 }}>{t.valor}</Typography>
                  <Typography variant="body2" sx={{ color: '#666' }}>{t.detalle}</Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          ))}
        </Box>
      )}

      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1.5 }}>Accesos rápidos</Typography>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
        <Button variant="contained" startIcon={<AddIcon />} component={RouterLink} to="/alumnos">
          Nuevo alumno
        </Button>
        <Button variant="outlined" startIcon={<Assessment />} component={RouterLink} to="/reportes">
          Generar reporte
        </Button>
        {esAdmin && (
          <Button variant="outlined" startIcon={<AddIcon />} component={RouterLink} to="/datos/instalaciones">
            Nueva instalación
          </Button>
        )}
      </Stack>
    </Box>
  );
};
