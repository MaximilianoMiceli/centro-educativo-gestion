import React, { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Chip,
  InputAdornment,
  Paper,
  Snackbar,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { Add as AddIcon, Search as SearchIcon } from '@mui/icons-material';
import { docenteService } from '../services/docenteService';
import type { DocenteDTO } from '../services/docenteService';
import { DocenteDialog } from './DocenteDialog';
import { PaginadorTabla } from './PaginadorTabla';
import { usePaginacion } from '../utils/usePaginacion';

interface DocentesPanelProps {
  token: string;
}

const COLORES_ESTADO: Record<DocenteDTO['estado'], 'success' | 'default' | 'warning'> = {
  ACTIVO: 'success',
  INACTIVO: 'default',
  LICENCIA: 'warning',
};

export const DocentesPanel: React.FC<DocentesPanelProps> = ({ token }) => {
  const [docentes, setDocentes] = useState<DocenteDTO[]>([]);
  const [texto, setTexto] = useState('');
  const [cargando, setCargando] = useState(true);
  const [dialogo, setDialogo] = useState<{ abierto: boolean; docente: DocenteDTO | null }>({ abierto: false, docente: null });
  const [aviso, setAviso] = useState<{ tipo: 'success' | 'error'; texto: string } | null>(null);

  const cargar = () => {
    setCargando(true);
    return docenteService
      .listar()
      .then(setDocentes)
      .catch(() => setAviso({ tipo: 'error', texto: 'No se pudieron cargar los docentes' }))
      .finally(() => setCargando(false));
  };

  useEffect(() => {
    docenteService.setToken(token);
    cargar();
  }, [token]);

  const filtrados = useMemo(() => {
    const termino = texto.trim().toLowerCase();
    if (!termino) return docentes;
    return docentes.filter((d) =>
      [d.legajo, d.dni, d.nombre, d.apellido].some((valor) => valor.toLowerCase().includes(termino)));
  }, [docentes, texto]);

  const paginado = usePaginacion(filtrados);

  const alGuardar = (guardado: DocenteDTO, esNuevo: boolean) => {
    setDialogo({ abierto: false, docente: null });
    setAviso({
      tipo: 'success',
      texto: esNuevo ? `Docente ${guardado.apellido}, ${guardado.nombre} dado de alta` : 'Datos actualizados',
    });
    cargar();
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, gap: 2, flexWrap: 'wrap' }}>
        <Typography variant="h5" sx={{ fontWeight: 600 }}>Docentes</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setDialogo({ abierto: true, docente: null })}
          sx={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', fontWeight: 600 }}
        >
          Nuevo docente
        </Button>
      </Box>

      <TextField
        fullWidth
        placeholder="Buscar por nombre, apellido, legajo o DNI"
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
        size="small"
        sx={{ mb: 2 }}
        slotProps={{
          input: {
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon sx={{ color: '#999' }} />
              </InputAdornment>
            ),
          },
        }}
      />

      <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>
        {cargando ? 'Cargando…' : `${filtrados.length} ${filtrados.length === 1 ? 'docente' : 'docentes'}`}
      </Typography>

      <TableContainer component={Paper} sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <Table size="small">
          <TableHead>
            <TableRow sx={{ '& th': { fontWeight: 700, backgroundColor: '#F5F6FA' } }}>
              <TableCell>Legajo</TableCell>
              <TableCell>Docente</TableCell>
              <TableCell>Especialidad</TableCell>
              <TableCell>Cursos a cargo</TableCell>
              <TableCell>Estado</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {paginado.visibles.map((d) => (
              <TableRow
                key={d.id}
                hover
                onClick={() => setDialogo({ abierto: true, docente: d })}
                sx={{ cursor: 'pointer' }}
              >
                <TableCell>{d.legajo}</TableCell>
                <TableCell>{d.apellido}, {d.nombre}</TableCell>
                <TableCell>{d.especialidad}</TableCell>
                <TableCell>{d.cursos.length ? d.cursos.join(', ') : '—'}</TableCell>
                <TableCell>
                  <Chip size="small" label={d.estado} color={COLORES_ESTADO[d.estado]} variant="outlined" />
                </TableCell>
              </TableRow>
            ))}
            {!cargando && filtrados.length === 0 && (
              <TableRow>
                <TableCell colSpan={5} sx={{ textAlign: 'center', color: '#666', py: 4 }}>
                  No hay docentes para la búsqueda.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
      <PaginadorTabla
        total={paginado.total}
        pagina={paginado.pagina}
        porPagina={paginado.porPagina}
        onPaginaChange={paginado.setPagina}
        onPorPaginaChange={paginado.cambiarPorPagina}
      />

      <DocenteDialog
        open={dialogo.abierto}
        docente={dialogo.docente}
        onClose={() => setDialogo({ abierto: false, docente: null })}
        onGuardado={alGuardar}
      />

      <Snackbar
        open={!!aviso}
        autoHideDuration={4000}
        onClose={() => setAviso(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        {aviso ? (
          <Alert severity={aviso.tipo} onClose={() => setAviso(null)} variant="filled">{aviso.texto}</Alert>
        ) : <span />}
      </Snackbar>
    </Box>
  );
};
