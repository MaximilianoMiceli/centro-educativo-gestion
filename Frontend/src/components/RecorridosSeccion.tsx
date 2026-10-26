import React, { useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, List, ListItem, Stack, TextField, Typography } from '@mui/material';
import { serviciosService } from '../services/serviciosService';
import type { Recorrido } from '../services/serviciosService';

interface RecorridosSeccionProps {
  onAviso: (aviso: { tipo: 'success' | 'error'; texto: string }) => void;
}

export const RecorridosSeccion: React.FC<RecorridosSeccionProps> = ({ onAviso }) => {
  const [recorridos, setRecorridos] = useState<Recorrido[]>([]);
  const [editandoId, setEditandoId] = useState<number | null>(null);
  const [nombre, setNombre] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    serviciosService
      .listarRecorridos()
      .then(setRecorridos)
      .catch(() => onAviso({ tipo: 'error', texto: 'No se pudieron cargar los recorridos' }));
  }, [onAviso]);

  const guardar = async (recorrido: Recorrido) => {
    setError('');
    try {
      const actualizado = await serviciosService.renombrarRecorrido(recorrido.id, nombre);
      setRecorridos((previos) => previos.map((r) => (r.id === actualizado.id ? actualizado : r)));
      setEditandoId(null);
      onAviso({ tipo: 'success', texto: 'Recorrido renombrado' });
    } catch (err: any) {
      const data = err.response?.data;
      setError(data?.errores ? Object.values(data.errores).join(' · ') : (data?.message ?? 'No se pudo renombrar'));
    }
  };

  return (
    <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
      <CardContent>
        <Typography variant="body2" sx={{ color: '#666', mb: 2 }}>
          El servicio de transporte tiene exactamente cuatro recorridos. Se pueden renombrar, pero no crear ni eliminar.
        </Typography>
        <List>
          {recorridos.map((r) => (
            <ListItem key={r.id} divider sx={{ gap: 2, flexWrap: 'wrap' }}>
              {editandoId === r.id ? (
                <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'flex-start', flex: 1 }}>
                  <TextField
                    value={nombre}
                    onChange={(e) => setNombre(e.target.value)}
                    error={!!error}
                    helperText={error || ' '}
                    size="small"
                    sx={{ minWidth: 280 }}
                  />
                  <Stack direction="row" spacing={1}>
                    <Button variant="contained" onClick={() => guardar(r)} disabled={nombre.trim().length < 3}>
                      Guardar
                    </Button>
                    <Button onClick={() => setEditandoId(null)}>Cancelar</Button>
                  </Stack>
                </Box>
              ) : (
                <>
                  <Typography sx={{ flex: 1, fontWeight: 500 }}>{r.nombre}</Typography>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() => {
                      setError('');
                      setNombre(r.nombre);
                      setEditandoId(r.id);
                    }}
                  >
                    Renombrar
                  </Button>
                </>
              )}
            </ListItem>
          ))}
        </List>
      </CardContent>
    </Card>
  );
};
