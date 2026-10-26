import React, { useEffect, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { Alert, Box, Button, Card, CardActionArea, CardContent, CircularProgress, LinearProgress, Stack, Typography } from '@mui/material';
import { Add as AddIcon, Apartment, Assessment, Badge, Groups, ManageAccounts } from '@mui/icons-material';
import { alumnosAdminService } from '../services/alumnosAdminService';
import type { AlumnoFicha } from '../services/alumnosAdminService';
import { adminService } from '../services/adminService';
import { docenteService } from '../services/docenteService';
import { instalacionesService } from '../services/instalacionesService';
import type { InstalacionDTO } from '../services/instalacionesService';
import { rolDelToken } from '../utils/token';
import { PALETA } from '../institucion';

interface InicioProps {
  token: string;
}

interface Barra {
  etiqueta: string;
  valor: number;
  color: string;
}

interface Indicadores {
  alumnos: AlumnoFicha[];
  docentesActivos: number;
  docentesTotal: number;
  instalaciones: InstalacionDTO[];
  usuarios: number;
}

interface Tarjeta {
  ruta: string;
  titulo: string;
  valor: number | string;
  detalle: string;
  icono: React.ReactNode;
  color: string;
}

const COLORES_NIVEL: Record<string, string> = {
  INICIAL: PALETA.naranja,
  PRIMARIO: PALETA.verde,
  SECUNDARIO: PALETA.magenta,
};

/** Barras horizontales: cada valor se compara con el máximo para que se vean las proporciones. */
const GraficoBarras: React.FC<{ barras: Barra[]; vacio: string }> = ({ barras, vacio }) => {
  const maximo = Math.max(1, ...barras.map((b) => b.valor));
  if (barras.length === 0) {
    return <Typography variant="body2" sx={{ color: '#666' }}>{vacio}</Typography>;
  }
  return (
    <Stack spacing={1.5}>
      {barras.map((barra) => (
        <Box key={barra.etiqueta}>
          <Stack direction="row" sx={{ justifyContent: 'space-between', mb: 0.5 }}>
            <Typography variant="body2" sx={{ fontWeight: 600, color: PALETA.grisTexto }}>{barra.etiqueta}</Typography>
            <Typography variant="body2" sx={{ color: '#666' }}>{barra.valor}</Typography>
          </Stack>
          <LinearProgress
            variant="determinate"
            value={(barra.valor / maximo) * 100}
            sx={{
              height: 10,
              borderRadius: 5,
              backgroundColor: '#eef2f7',
              '& .MuiLinearProgress-bar': { backgroundColor: barra.color, borderRadius: 5 },
            }}
          />
        </Box>
      ))}
    </Stack>
  );
};

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
          alumnos,
          docentesActivos: docentes.filter((d) => d.estado === 'ACTIVO').length,
          docentesTotal: docentes.length,
          instalaciones,
          usuarios: usuarios.length,
        });
      })
      .catch(() => setError('No se pudieron cargar los indicadores'));
  }, [token, esAdmin]);

  const alumnosActivos = indicadores?.alumnos.filter((a) => a.estado === 'ACTIVO').length ?? 0;
  const instalacionesActivas = indicadores?.instalaciones.filter((i) => i.estado === 'ACTIVA').length ?? 0;
  const instalacionesMantenimiento = indicadores?.instalaciones.filter((i) => i.estado === 'EN_MANTENIMIENTO').length ?? 0;

  const tarjetas: Tarjeta[] = indicadores
    ? [
        {
          ruta: '/alumnos',
          titulo: 'Alumnos activos',
          valor: alumnosActivos,
          detalle: `de ${indicadores.alumnos.length} registrados`,
          icono: <Groups />,
          color: PALETA.verde,
        },
        ...(esAdmin
          ? [
              {
                ruta: '/docentes',
                titulo: 'Docentes activos',
                valor: indicadores.docentesActivos,
                detalle: `de ${indicadores.docentesTotal} registrados`,
                icono: <Badge />,
                color: PALETA.magenta,
              },
              {
                ruta: '/usuarios',
                titulo: 'Cuentas de acceso',
                valor: indicadores.usuarios,
                detalle: 'usuarios del sistema',
                icono: <ManageAccounts />,
                color: PALETA.purpura,
              },
            ]
          : []),
        {
          ruta: '/datos/instalaciones',
          titulo: 'Instalaciones activas',
          valor: instalacionesActivas,
          detalle: `${instalacionesMantenimiento} en mantenimiento`,
          icono: <Apartment />,
          color: PALETA.naranja,
        },
      ]
    : [];

  const barrasNiveles: Barra[] = indicadores
    ? Object.entries(
        indicadores.alumnos
          .filter((a) => a.estado === 'ACTIVO')
          .reduce<Record<string, number>>((acumulado, a) => {
            acumulado[a.nivelNombre] = (acumulado[a.nivelNombre] ?? 0) + 1;
            return acumulado;
          }, {}),
      ).map(([nivel, cantidad]) => ({
        etiqueta: nivel.charAt(0) + nivel.slice(1).toLowerCase(),
        valor: cantidad,
        color: COLORES_NIVEL[nivel] ?? PALETA.azulClaro,
      }))
    : [];

  const barrasInstalaciones: Barra[] = [
    { etiqueta: 'Activas', valor: instalacionesActivas, color: PALETA.verde },
    { etiqueta: 'En mantenimiento', valor: instalacionesMantenimiento, color: PALETA.naranja },
    { etiqueta: 'Inactivas', valor: (indicadores?.instalaciones.length ?? 0) - instalacionesActivas - instalacionesMantenimiento, color: PALETA.azulClaro },
  ].filter((b) => b.valor > 0);

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 700, mb: 0.5 }}>Inicio</Typography>
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
        <>
          <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 2, mb: 3 }}>
            {tarjetas.map((t) => (
              <Card key={t.titulo} sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)', borderRadius: 3, borderTop: `4px solid ${t.color}` }}>
                <CardActionArea component={RouterLink} to={t.ruta}>
                  <CardContent>
                    <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', mb: 1.5, color: t.color }}>
                      {t.icono}
                      <Typography variant="subtitle2" sx={{ fontWeight: 700, color: PALETA.grisTexto }}>{t.titulo}</Typography>
                    </Stack>
                    <Typography variant="h4" sx={{ fontWeight: 700 }}>{t.valor}</Typography>
                    <Typography variant="body2" sx={{ color: '#666' }}>{t.detalle}</Typography>
                  </CardContent>
                </CardActionArea>
              </Card>
            ))}
          </Box>

          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2, mb: 3 }}>
            <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)', borderRadius: 3 }}>
              <CardContent>
                <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2, color: PALETA.azul }}>Alumnos activos por nivel</Typography>
                <GraficoBarras barras={barrasNiveles} vacio="Todavía no hay alumnos activos cargados." />
              </CardContent>
            </Card>
            <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)', borderRadius: 3 }}>
              <CardContent>
                <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2, color: PALETA.azul }}>Estado de las instalaciones</Typography>
                <GraficoBarras barras={barrasInstalaciones} vacio="No hay instalaciones cargadas." />
              </CardContent>
            </Card>
          </Box>
        </>
      )}

      <Typography variant="h6" sx={{ fontWeight: 700, mb: 1.5 }}>Accesos rápidos</Typography>
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
