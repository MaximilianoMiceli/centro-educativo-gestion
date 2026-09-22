import { useState, useEffect } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';

const theme = createTheme({
  palette: {
    primary: {
      main: '#667eea',
    },
    secondary: {
      main: '#764ba2',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
  },
});

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
      ) : (
        <Dashboard token={token} onLogout={handleLogout} />
      )}
    </ThemeProvider>
  );
}

export default App;
