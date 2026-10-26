import { GRADIENTE_PRINCIPAL } from '../institucion';
import React, { useState } from 'react';
import { Alert, Box, Button, Card, CardContent, Container, TextField, Typography } from '@mui/material';
import { API_URL } from '../config';

interface FormularioCambioPasswordProps {
  token: string;
  onCambiado: (nuevoToken: string) => void;
  onCancelar?: () => void;
  obligatorio?: boolean;
}

export const FormularioCambioPassword: React.FC<FormularioCambioPasswordProps> = ({
  token,
  onCambiado,
  onCancelar,
  obligatorio = false,
}) => {
  const [actual, setActual] = useState('');
  const [nueva, setNueva] = useState('');
  const [repetir, setRepetir] = useState('');
  const [errores, setErrores] = useState<Record<string, string>>({});
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [guardando, setGuardando] = useState(false);

  const coincidencia = repetir.length > 0 && repetir !== nueva ? 'Las contraseñas no coinciden' : '';

  const guardar = async () => {
    setErrores({});
    setMensaje(null);
    if (nueva !== repetir) {
      setErrores({ passwordRepetir: 'Las contraseñas no coinciden' });
      return;
    }
    setGuardando(true);
    try {
      const response = await fetch(`${API_URL}/auth/cambiar-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ passwordActual: actual, passwordNueva: nueva }),
      });
      const data = await response.json();
      if (response.ok) {
        onCambiado(data.token);
        return;
      }
      if (response.status === 400) {
        setErrores(data.errores ?? {});
      }
      setMensaje(data.message ?? 'No se pudo cambiar la contraseña');
    } catch {
      setMensaje('No se pudo conectar con el servidor');
    } finally {
      setGuardando(false);
    }
  };

  const campo = (valor: string, setValor: (v: string) => void, etiqueta: string, clave: string, extra = '') => (
    <TextField
      label={etiqueta}
      type="password"
      value={valor}
      onChange={(e) => setValor(e.target.value)}
      error={!!errores[clave] || !!extra}
      helperText={errores[clave] ?? (extra || ' ')}
      disabled={guardando}
      fullWidth
      size="small"
    />
  );

  return (
    <Box>
      {obligatorio && (
        <Typography variant="body2" sx={{ color: '#666', mb: 2 }}>
          Tu cuenta tiene una contraseña temporal. Para continuar, elegí una nueva (mínimo 8 caracteres).
        </Typography>
      )}

      {mensaje && <Alert severity="error" sx={{ mb: 2 }}>{mensaje}</Alert>}

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        {campo(actual, setActual, obligatorio ? 'Contraseña temporal' : 'Contraseña actual', 'passwordActual')}
        {campo(nueva, setNueva, 'Nueva contraseña', 'passwordNueva')}
        {campo(repetir, setRepetir, 'Repetir nueva contraseña', 'passwordRepetir', coincidencia)}
      </Box>

      <Button
        fullWidth
        variant="contained"
        onClick={guardar}
        disabled={guardando || !actual || !nueva || !repetir}
        sx={{ mt: 2, background: GRADIENTE_PRINCIPAL, fontWeight: 600 }}
      >
        Guardar nueva contraseña
      </Button>
      {onCancelar && (
        <Button fullWidth onClick={onCancelar} sx={{ mt: 1 }} disabled={guardando}>
          Cancelar
        </Button>
      )}
    </Box>
  );
};

interface CambiarPasswordProps {
  token: string;
  onCambiado: (nuevoToken: string) => void;
  onLogout: () => void;
}

export const CambiarPassword: React.FC<CambiarPasswordProps> = ({ token, onCambiado, onLogout }) => (
  <Container maxWidth="sm" sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center' }}>
    <Card sx={{ width: '100%', boxShadow: '0 4px 12px rgba(0,0,0,0.12)' }}>
      <CardContent>
        <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>Cambiá tu contraseña</Typography>
        <FormularioCambioPassword token={token} onCambiado={onCambiado} obligatorio />
        <Button fullWidth onClick={onLogout} sx={{ mt: 1 }}>Salir</Button>
      </CardContent>
    </Card>
  </Container>
);
