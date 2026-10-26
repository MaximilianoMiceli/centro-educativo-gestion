import React from 'react';
import { Button, Dialog, DialogActions, DialogTitle, Typography } from '@mui/material';
import type { DocenteDTO } from '../services/docenteService';
import { DocenteFichaContenido } from './DocenteFichaContenido';

interface DocenteDialogProps {
  open: boolean;
  docente: DocenteDTO | null;
  onClose: () => void;
  onGuardado: (docente: DocenteDTO, esNuevo: boolean) => void;
}

/** Ventana para dar de alta un docente nuevo. Las fichas existentes se abren en su página propia. */
export const DocenteDialog: React.FC<DocenteDialogProps> = ({ open, docente, onClose, onGuardado }) => (
  <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
    <DialogTitle>
      {docente ? `${docente.apellido}, ${docente.nombre}` : 'Nuevo docente'}
      {docente && (
        <Typography variant="body2" sx={{ color: '#666' }}>
          Legajo {docente.legajo} · {docente.estado}
        </Typography>
      )}
    </DialogTitle>
    {open && <DocenteFichaContenido docente={docente} onGuardado={onGuardado} />}
    <DialogActions sx={{ px: 3, py: 2 }}>
      <Button onClick={onClose}>Cerrar</Button>
    </DialogActions>
  </Dialog>
);
