import React, { useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  Container,
  Paper,
} from '@mui/material';
import { API_URL } from '../config';
import { LogoInstitucional } from '../components/LogoInstitucional';
import { FONDO_INGRESO, GRADIENTE_PRINCIPAL, INSTITUCION } from '../institucion';

interface LoginProps {
  onLogin: (token: string) => void;
}

export const Login: React.FC<LoginProps> = ({ onLogin }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await fetch(`${API_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      if (response.status === 401) {
        setError('Usuario o contraseña incorrectos');
        setLoading(false);
        return;
      }

      if (!response.ok) {
        setError('El servidor no pudo procesar el inicio de sesión');
        setLoading(false);
        return;
      }

      const { token } = await response.json();
      onLogin(token);
    } catch {
      setError('No se pudo conectar con el servidor. ¿Está corriendo el backend?');
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: FONDO_INGRESO,
        padding: 2,
      }}
    >
      <Container maxWidth="sm">
        <Paper
          elevation={8}
          sx={{
            padding: 4,
            borderRadius: 2,
            textAlign: 'center',
          }}
        >
          {/* Logo */}
          <Box sx={{ display: 'flex', justifyContent: 'center', marginBottom: 2 }}>
            <LogoInstitucional size={80} />
          </Box>

          {/* Título */}
          <Typography variant="h4" sx={{ fontWeight: 700, marginBottom: 0.5 }}>
            {INSTITUCION.nombre}
          </Typography>
          <Typography variant="subtitle1" sx={{ color: '#666', marginBottom: 3 }}>
            {INSTITUCION.subtitulo} · Sistema de Gestión
          </Typography>

          {/* Formulario */}
          <form onSubmit={handleLogin}>
            {error && (
              <Alert severity="error" sx={{ marginBottom: 2 }} onClose={() => setError('')}>
                {error}
              </Alert>
            )}

            <TextField
              fullWidth
              label="Usuario"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              margin="normal"
              disabled={loading}
              variant="outlined"
            />

            <TextField
              fullWidth
              label="Contraseña"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              margin="normal"
              disabled={loading}
              variant="outlined"
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{
                marginTop: 3,
                marginBottom: 2,
                background: GRADIENTE_PRINCIPAL,
                padding: '12px',
                fontSize: '1rem',
                fontWeight: 600,
              }}
              disabled={loading}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Ingresar'}
            </Button>
          </form>

        </Paper>
      </Container>
    </Box>
  );
};
