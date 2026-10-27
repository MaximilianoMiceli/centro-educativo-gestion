import React, { useState } from 'react';
import { Link as RouterLink, Outlet, useLocation } from 'react-router-dom';
import {
  Alert,
  AppBar,
  Box,
  Button,
  Dialog,
  DialogContent,
  DialogTitle,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  ListSubheader,
  Snackbar,
  Toolbar,
  Typography,
} from '@mui/material';
import {
  AdminPanelSettings,
  CalendarMonth,
  Favorite,
  Apartment,
  Assessment,
  Assignment,
  Badge,
  Class,
  Schedule,
  DirectionsBus,
  FamilyRestroom,
  Grade,
  Groups,
  Home,
  Layers,
  Logout as LogOutIcon,
  ManageAccounts,
  Menu as MenuIcon,
  Sports,
  Subject,
  VpnKey as VpnKeyIcon,
} from '@mui/icons-material';
import { LogoInstitucional } from '../components/LogoInstitucional';
import { FormularioCambioPassword } from '../components/CambiarPassword';
import { rolDelToken } from '../utils/token';
import { GRADIENTE_PRINCIPAL, INSTITUCION, PALETA } from '../institucion';
import { agruparMenu, itemsPermitidos } from '../utils/navegacion';
import type { ItemMenu } from '../utils/navegacion';

interface DashboardProps {
  token: string;
  onLogout: () => void;
  onTokenActualizado: (nuevoToken: string) => void;
}

const ANCHO_MENU = 250;

const ICONOS: Record<string, React.ReactNode> = {
  inicio: <Home fontSize="small" />,
  agenda: <CalendarMonth fontSize="small" />,
  horarios: <Schedule fontSize="small" />,
  inscripciones: <Favorite fontSize="small" />,
  misServicios: <Favorite fontSize="small" />,
  calificaciones: <Grade fontSize="small" />,
  alumnos: <Groups fontSize="small" />,
  docentes: <Badge fontSize="small" />,
  niveles: <Layers fontSize="small" />,
  cursos: <Class fontSize="small" />,
  materias: <Subject fontSize="small" />,
  asignaciones: <Assignment fontSize="small" />,
  deportes: <Sports fontSize="small" />,
  instalaciones: <Apartment fontSize="small" />,
  transporte: <DirectionsBus fontSize="small" />,
  reportes: <Assessment fontSize="small" />,
  usuarios: <ManageAccounts fontSize="small" />,
  permisos: <AdminPanelSettings fontSize="small" />,
  familia: <FamilyRestroom fontSize="small" />,
};

export const Dashboard: React.FC<DashboardProps> = ({ token, onLogout, onTokenActualizado }) => {
  const [cambioAbierto, setCambioAbierto] = useState(false);
  const [avisoCambio, setAvisoCambio] = useState(false);
  const [menuAbierto, setMenuAbierto] = useState(false);
  const { pathname } = useLocation();
  const grupos = agruparMenu(itemsPermitidos(rolDelToken(token)));

  const alCambiarPassword = (nuevoToken: string) => {
    onTokenActualizado(nuevoToken);
    setCambioAbierto(false);
    setAvisoCambio(true);
  };

  const elementoMenu = (item: ItemMenu) => {
    const activo = pathname === item.ruta || pathname.startsWith(`${item.ruta}/`);
    return (
      <ListItemButton
        key={item.ruta}
        component={RouterLink}
        to={item.ruta}
        selected={activo}
        onClick={() => setMenuAbierto(false)}
        sx={{ borderRadius: 2, mx: 1, mb: 0.5, '&.Mui-selected': { backgroundColor: PALETA.azulSuave, color: PALETA.azul } }}
      >
        <ListItemIcon sx={{ minWidth: 36, color: 'inherit' }}>{ICONOS[item.icono]}</ListItemIcon>
        <ListItemText primary={item.etiqueta} slotProps={{ primary: { sx: { fontSize: 14, fontWeight: activo ? 700 : 500 } } }} />
      </ListItemButton>
    );
  };

  const menu = (
    <Box sx={{ width: ANCHO_MENU, pt: 2 }}>
      {grupos.map((grupo) => (
        <List
          key={grupo.titulo || 'general'}
          dense
          subheader={
            grupo.titulo ? (
              <ListSubheader sx={{ backgroundColor: 'transparent', lineHeight: '32px', fontWeight: 700, color: '#667' }}>
                {grupo.titulo}
              </ListSubheader>
            ) : undefined
          }
        >
          {grupo.items.map(elementoMenu)}
        </List>
      ))}
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', backgroundColor: PALETA.grisSuave }}>
      {/* Menú fijo en escritorio */}
      <Box
        component="aside"
        sx={{
          display: { xs: 'none', md: 'block' },
          width: ANCHO_MENU,
          flexShrink: 0,
          position: 'sticky',
          top: 0,
          height: '100vh',
          overflowY: 'auto',
          backgroundColor: '#fff',
          borderRight: '1px solid #e4ecf4',
        }}
      >
        {menu}
      </Box>

      {/* Menú deslizable en celular */}
      <Drawer open={menuAbierto} onClose={() => setMenuAbierto(false)} sx={{ display: { md: 'none' } }}>
        {menu}
      </Drawer>

      <Box sx={{ flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column' }}>
        <AppBar
          position="sticky"
          sx={{
            background: GRADIENTE_PRINCIPAL,
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
          }}
        >
          <Toolbar>
            <IconButton
              color="inherit"
              onClick={() => setMenuAbierto(true)}
              aria-label="Abrir menú"
              sx={{ mr: 1, display: { md: 'none' } }}
            >
              <MenuIcon />
            </IconButton>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: 40, height: 40, mr: 1.5, borderRadius: '50%', backgroundColor: 'rgba(255,255,255,0.95)' }}>
              <LogoInstitucional size={28} />
            </Box>
            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="h6" sx={{ fontWeight: 700, lineHeight: 1.2 }}>
                {INSTITUCION.nombre}
              </Typography>
              <Typography variant="caption" sx={{ opacity: 0.85 }}>
                Sistema de Gestión
              </Typography>
            </Box>
            <Button
              color="inherit"
              startIcon={<VpnKeyIcon />}
              onClick={() => setCambioAbierto(true)}
              sx={{ fontWeight: 600, mr: 1, '&:hover': { backgroundColor: 'rgba(255,255,255,0.1)' } }}
            >
              Mi contraseña
            </Button>
            <Button
              color="inherit"
              startIcon={<LogOutIcon />}
              onClick={onLogout}
              sx={{ fontWeight: 600, '&:hover': { backgroundColor: 'rgba(255,255,255,0.1)' } }}
            >
              Salir
            </Button>
          </Toolbar>
        </AppBar>

        <Dialog open={cambioAbierto} onClose={() => setCambioAbierto(false)} fullWidth maxWidth="xs">
          <DialogTitle>Cambiar mi contraseña</DialogTitle>
          <DialogContent>
            <FormularioCambioPassword
              token={token}
              onCambiado={alCambiarPassword}
              onCancelar={() => setCambioAbierto(false)}
            />
          </DialogContent>
        </Dialog>

        <Snackbar
          open={avisoCambio}
          autoHideDuration={4000}
          onClose={() => setAvisoCambio(false)}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        >
          <Alert severity="success" variant="filled" onClose={() => setAvisoCambio(false)}>
            Contraseña actualizada
          </Alert>
        </Snackbar>

        <Box component="main" sx={{ flex: 1, p: { xs: 2, md: 3 }, maxWidth: 1200, width: '100%', boxSizing: 'border-box' }}>
          <Outlet />
        </Box>

        <Box sx={{ py: 2, textAlign: 'center', color: '#999' }}>
          <Typography variant="caption">
            © 2026 {INSTITUCION.nombre} · {INSTITUCION.localidad} · Sistema seguro con autenticación JWT y auditoría automática
          </Typography>
        </Box>
      </Box>
    </Box>
  );
};
