import React, { useState } from 'react';
import {
  Box,
  AppBar,
  Toolbar,
  Typography,
  Button,
  Container,
  Card,
  CardContent,
  Avatar,
  Tab,
  Tabs,
  Dialog,
  DialogContent,
  DialogTitle,
  Snackbar,
  Alert,
} from '@mui/material';
import { Logout as LogOutIcon, School as SchoolIcon, VpnKey as VpnKeyIcon } from '@mui/icons-material';
import { AlumnosPanel } from '../components/AlumnosPanel';
import { MiInformacion } from '../components/MiInformacion';
import { DocentesPanel } from '../components/DocentesPanel';
import { UsuariosPanel } from '../components/UsuariosPanel';
import { PermisosRoles } from '../components/PermisosRoles';
import { DatosMaestros } from '../components/DatosMaestros';
import { FormularioCambioPassword } from '../components/CambiarPassword';
import { rolDelToken } from '../utils/token';

interface DashboardProps {
  token: string;
  onLogout: () => void;
  onTokenActualizado: (nuevoToken: string) => void;
}

type Pestana = 'alumnos' | 'docentes' | 'usuarios' | 'permisos' | 'datos';

export const Dashboard: React.FC<DashboardProps> = ({ token, onLogout, onTokenActualizado }) => {
  const [pestana, setPestana] = useState<Pestana>('alumnos');
  const [cambioAbierto, setCambioAbierto] = useState(false);
  const [avisoCambio, setAvisoCambio] = useState(false);
  const esAdmin = rolDelToken(token) === 'ADMIN';
  const esFamilia = ['PADRE', 'ALUMNO'].includes(rolDelToken(token) ?? '');

  const alCambiarPassword = (nuevoToken: string) => {
    onTokenActualizado(nuevoToken);
    setCambioAbierto(false);
    setAvisoCambio(true);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', backgroundColor: '#F8F9FA' }}>
      {/* AppBar */}
      <AppBar
        position="sticky"
        sx={{
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
        }}
      >
        <Toolbar>
          <SchoolIcon sx={{ marginRight: 1.5, fontSize: 28 }} />
          <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 700 }}>
            Centro Educativo - Sistema de Gestión
          </Typography>
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
            sx={{
              fontWeight: 600,
              '&:hover': {
                backgroundColor: 'rgba(255,255,255,0.1)',
              },
            }}
          >
            Logout
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

      {/* Contenido Principal */}
      <Container maxWidth="md" sx={{ flex: 1, paddingY: 4 }}>
        {/* Tarjeta de Bienvenida */}
        <Card sx={{ marginBottom: 4, boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box>
                <Avatar
                  sx={{
                    width: 64,
                    height: 64,
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    fontSize: '1.8rem',
                  }}
                >
                  👨‍🎓
                </Avatar>
              </Box>
              <Box sx={{ flex: 1 }}>
                <Typography variant="h5" sx={{ fontWeight: 600 }}>
                  Bienvenido al Sistema
                </Typography>
                <Typography variant="body2" sx={{ color: '#666', marginTop: 0.5 }}>
                  Busca y consulta información de alumnos de forma rápida y segura.
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>

        {esAdmin && (
          <Tabs value={pestana} onChange={(_e, nueva: Pestana) => setPestana(nueva)} centered sx={{ mb: 3 }}>
            <Tab value="alumnos" label="Alumnos" />
            <Tab value="docentes" label="Docentes" />
            <Tab value="usuarios" label="Usuarios" />
            <Tab value="permisos" label="Permisos" />
            <Tab value="datos" label="Datos" />
          </Tabs>
        )}

        {esFamilia ? <MiInformacion token={token} /> : (!esAdmin || pestana === 'alumnos') ? <AlumnosPanel token={token} /> : null}
        {esAdmin && pestana === 'docentes' ? <DocentesPanel token={token} /> : null}
        {esAdmin && pestana === 'usuarios' ? <UsuariosPanel token={token} /> : null}
        {esAdmin && pestana === 'permisos' ? <PermisosRoles token={token} /> : null}
        {esAdmin && pestana === 'datos' ? <DatosMaestros token={token} /> : null}

        {/* Footer Info */}
        <Box sx={{ marginTop: 4, textAlign: 'center', color: '#999' }}>
          <Typography variant="caption">
            <br />
            Sistema seguro con autenticación JWT y auditoría automática
          </Typography>
        </Box>
      </Container>
    </Box>
  );
};
