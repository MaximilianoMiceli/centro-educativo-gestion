import React, { useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  Container,
  Avatar,
  Paper,
} from '@mui/material';
import { School as SchoolIcon } from '@mui/icons-material';

interface LoginProps {
  onLogin: (token: string) => void;
}

export const Login: React.FC<LoginProps> = ({ onLogin }) => {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('password');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // Obtener token del backend (válido por 1 hora)
      const tokenResponse = await fetch('http://localhost:8080/api/v1/auth/token');

      if (!tokenResponse.ok) {
        setError('No se pudo obtener el token del servidor');
        setLoading(false);
        return;
      }

      const { token } = await tokenResponse.json();
      onLogin(token);
    } catch (err) {
      setError('Error al conectar con el servidor. ¿Está corriendo el backend?');
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
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
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
          <Avatar
            sx={{
              width: 80,
              height: 80,
              margin: '0 auto 2rem',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              fontSize: '2.5rem',
            }}
          >
            <SchoolIcon sx={{ fontSize: 50 }} />
          </Avatar>

          {/* Título */}
          <Typography variant="h4" sx={{ fontWeight: 700, marginBottom: 0.5 }}>
            Centro Educativo
          </Typography>
          <Typography variant="subtitle1" sx={{ color: '#666', marginBottom: 3 }}>
            Sistema de Gestión Educativa
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
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                padding: '12px',
                fontSize: '1rem',
                fontWeight: 600,
              }}
              disabled={loading}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Ingresar'}
            </Button>
          </form>

          {/* Info de Prueba */}
          <Typography variant="caption" sx={{ display: 'block', color: '#999', marginTop: 2 }}>
            <strong>Credenciales de Demo:</strong>
          </Typography>
          <Typography variant="caption" sx={{ display: 'block', color: '#999' }}>
            Usuario: <strong>admin</strong> | Contraseña: <strong>password</strong>
          </Typography>
          <Typography variant="caption" sx={{ display: 'block', color: '#999', marginTop: 1 }}>
            También puedes usar: <strong>profesor1</strong> / <strong>password</strong>
          </Typography>

          <Typography variant="caption" sx={{ display: 'block', color: '#CCC', marginTop: 3 }}>
            Sprint 1 - Demostración | Backend: Java 21 + Spring Boot 4.1.1
          </Typography>
        </Paper>
      </Container>
    </Box>
  );
};
