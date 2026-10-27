import { useState, useEffect } from 'react';
import { BrowserRouter, Navigate, Route, Routes, useParams } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { Inicio } from './pages/Inicio';
import { CambiarPassword } from './components/CambiarPassword';
import { AlumnosPanel } from './components/AlumnosPanel';
import { DocentesPanel } from './components/DocentesPanel';
import { UsuariosPanel } from './components/UsuariosPanel';
import { PermisosRoles } from './components/PermisosRoles';
import { DatosMaestros } from './components/DatosMaestros';
import { ReportesPanel } from './components/ReportesPanel';
import { MiInformacion } from './components/MiInformacion';
import { AlumnoFichaPagina } from './pages/AlumnoFichaPagina';
import { DocenteFichaPagina } from './pages/DocenteFichaPagina';
import { HorariosPagina } from './pages/HorariosPagina';
import { MiAgendaPagina } from './pages/MiAgendaPagina';
import { ServiciosPagina } from './pages/ServiciosPagina';
import { CalificacionesPagina } from './pages/CalificacionesPagina';
import { MisCalificacionesPagina } from './pages/MisCalificacionesPagina';
import { MisServiciosPagina } from './pages/MisServiciosPagina';
import { debeCambiarPasswordDelToken, rolDelToken } from './utils/token';
import { esSeccionDatos, puedeVer, rutaInicial } from './utils/navegacion';
import { PALETA } from './institucion';

const theme = createTheme({
  palette: {
    primary: {
      main: PALETA.azulOscuro,
    },
    secondary: {
      main: PALETA.verde,
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
  },
});

function PaginaDatos({ token }: { token: string }) {
  const { seccion } = useParams();
  if (!esSeccionDatos(seccion)) {
    return <Navigate to="/datos/niveles" replace />;
  }
  // key: al cambiar de sección se reinicia el estado (avisos, formularios) de la pantalla
  return <DatosMaestros key={seccion} token={token} seccion={seccion} />;
}

function AreaAutenticada({
  token,
  onLogout,
  onTokenActualizado,
}: {
  token: string;
  onLogout: () => void;
  onTokenActualizado: (nuevoToken: string) => void;
}) {
  const rol = rolDelToken(token);
  const puede = (ruta: string) => puedeVer(rol, ruta);

  return (
    <Routes>
      <Route
        element={<Dashboard token={token} onLogout={onLogout} onTokenActualizado={onTokenActualizado} />}
      >
        <Route index element={<Navigate to={rutaInicial(rol)} replace />} />
        {puede('/inicio') && <Route path="inicio" element={<Inicio token={token} />} />}
        {puede('/alumnos') && <Route path="alumnos" element={<AlumnosPanel token={token} />} />}
        {puede('/alumnos') && <Route path="alumnos/:id" element={<AlumnoFichaPagina token={token} />} />}
        {puede('/docentes') && <Route path="docentes/:id" element={<DocenteFichaPagina token={token} />} />}
        {puede('/horarios') && <Route path="horarios" element={<HorariosPagina token={token} />} />}
        {puede('/calificaciones') && <Route path="calificaciones" element={<CalificacionesPagina token={token} />} />}
        {puede('/mis-servicios') && <Route path="mis-servicios" element={<MisServiciosPagina token={token} />} />}
        {puede('/mis-calificaciones') && <Route path="mis-calificaciones" element={<MisCalificacionesPagina token={token} />} />}
        {puede('/mi-agenda') && <Route path="mi-agenda" element={<MiAgendaPagina token={token} />} />}
        {puede('/servicios') && <Route path="servicios" element={<ServiciosPagina token={token} />} />}
        {puede('/docentes') && <Route path="docentes" element={<DocentesPanel token={token} />} />}
        {puede('/reportes') && <Route path="reportes" element={<ReportesPanel token={token} />} />}
        {puede('/usuarios') && <Route path="usuarios" element={<UsuariosPanel token={token} />} />}
        {puede('/permisos') && <Route path="permisos" element={<PermisosRoles token={token} />} />}
        {puede('/mi-informacion') && <Route path="mi-informacion" element={<MiInformacion token={token} />} />}
        {puede('/datos/niveles') && <Route path="datos/:seccion" element={<PaginaDatos token={token} />} />}
        <Route path="*" element={<Navigate to={rutaInicial(rol)} replace />} />
      </Route>
    </Routes>
  );
}

function App() {
  const [token, setToken] = useState<string | null>(null);

  // Recuperar token del localStorage al cargar
  useEffect(() => {
    const savedToken = localStorage.getItem('auth_token');
    if (savedToken) {
      setToken(savedToken);
    }
  }, []);

  const handleLogin = (authToken: string) => {
    setToken(authToken);
    localStorage.setItem('auth_token', authToken);
  };

  const handleLogout = () => {
    setToken(null);
    localStorage.removeItem('auth_token');
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {!token ? (
        <Login onLogin={handleLogin} />
      ) : debeCambiarPasswordDelToken(token) ? (
        <CambiarPassword token={token} onCambiado={handleLogin} onLogout={handleLogout} />
      ) : (
        <BrowserRouter>
          <AreaAutenticada token={token} onLogout={handleLogout} onTokenActualizado={handleLogin} />
        </BrowserRouter>
      )}
    </ThemeProvider>
  );
}

export default App;
