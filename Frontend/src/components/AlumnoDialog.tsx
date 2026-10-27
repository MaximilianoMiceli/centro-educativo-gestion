import React from 'react';
import { Button, Dialog, DialogActions, DialogTitle, Typography } from '@mui/material';
import type { AlumnoFicha } from '../services/alumnosAdminService';
import type { CursoDTO } from '../services/datosMaestrosService';
import { AlumnoFichaContenido } from './AlumnoFichaContenido';

interface AlumnoDialogProps {
  open: boolean;
  token: string;
  alumno: AlumnoFicha | null;
  cursos: CursoDTO[];
  editable: boolean;
  onClose: () => void;
  onGuardado: (alumno: AlumnoFicha, esNuevo: boolean) => void;
}

/** Ventana para dar de alta un alumno nuevo. Las fichas existentes se abren en su página propia. */
export const AlumnoDialog: React.FC<AlumnoDialogProps> = ({ open, alumno, cursos, editable, onClose, onGuardado, token }) => (
  <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
    <DialogTitle>
      {alumno ? `${alumno.apellido}, ${alumno.nombre}` : 'Nuevo alumno'}
      {alumno && (
        <Typography variant="body2" sx={{ color: '#666' }}>
          Legajo {alumno.legajo} · {alumno.cursoDescripcion} · {alumno.estado}
        </Typography>
      )}
    </DialogTitle>
    {open && (
      <AlumnoFichaContenido alumno={alumno} token={token} cursos={cursos} editable={editable} onGuardado={onGuardado} />
    )}
    <DialogActions sx={{ px: 3, py: 2 }}>
      <Button onClick={onClose}>Cerrar</Button>
    </DialogActions>
  </Dialog>
);
