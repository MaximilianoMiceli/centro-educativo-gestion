import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';

interface ConfirmDialogProps {
  abierto: boolean;
  titulo: string;
  mensaje: React.ReactNode;
  textoConfirmar?: string;
  peligro?: boolean;
  cargando?: boolean;
  onConfirmar: () => void;
  onCancelar: () => void;
}

/** Pide confirmación antes de una acción que da de baja algo o cambia el acceso de alguien. */
export const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  abierto,
  titulo,
  mensaje,
  textoConfirmar = 'Confirmar',
  peligro = false,
  cargando = false,
  onConfirmar,
  onCancelar,
}) => (
  <Dialog open={abierto} onClose={cargando ? undefined : onCancelar} fullWidth maxWidth="xs">
    <DialogTitle sx={{ fontWeight: 700 }}>{titulo}</DialogTitle>
    <DialogContent>
      <Typography variant="body2" sx={{ color: '#444' }}>{mensaje}</Typography>
    </DialogContent>
    <DialogActions sx={{ px: 3, pb: 2 }}>
      <Button onClick={onCancelar} disabled={cargando}>Cancelar</Button>
      <Button
        variant="contained"
        color={peligro ? 'error' : 'primary'}
        onClick={onConfirmar}
        disabled={cargando}
      >
        {textoConfirmar}
      </Button>
    </DialogActions>
  </Dialog>
);
