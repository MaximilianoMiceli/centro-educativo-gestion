import { GRADIENTE_PRINCIPAL } from '../institucion';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Chip,
  InputAdornment,
  MenuItem,
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
import { alumnosAdminService } from '../services/alumnosAdminService';
import type { AlumnoFicha } from '../services/alumnosAdminService';
import { datosMaestrosService } from '../services/datosMaestrosService';
import type { CursoDTO, NivelDTO } from '../services/datosMaestrosService';
import { rolDelToken } from '../utils/token';
import { AlumnoDialog } from './AlumnoDialog';
import { EstadoVacio } from './EstadoVacio';
import { FilasCargando } from './FilasCargando';
import { PaginadorTabla } from './PaginadorTabla';
import { usePaginacion } from '../utils/usePaginacion';

interface AlumnosPanelProps {
  token: string;
}

const COLORES_ESTADO: Record<AlumnoFicha['estado'], 'success' | 'default' | 'info'> = {
  ACTIVO: 'success',
  INACTIVO: 'default',
  EGRESADO: 'info',
};

export const AlumnosPanel: React.FC<AlumnosPanelProps> = ({ token }) => {
  const navigate = useNavigate();
  const esAdmin = rolDelToken(token) === 'ADMIN';
  const [niveles, setNiveles] = useState<NivelDTO[]>([]);
  const [cursos, setCursos] = useState<CursoDTO[]>([]);
  const [texto, setTexto] = useState('');
  const [filtroNivel, setFiltroNivel] = useState('');
  const [filtroCurso, setFiltroCurso] = useState('');
  const [alumnos, setAlumnos] = useState<AlumnoFicha[]>([]);
  const [cargando, setCargando] = useState(false);
  const [dialogo, setDialogo] = useState<{ abierto: boolean; alumno: AlumnoFicha | null }>({ abierto: false, alumno: null });
  const [aviso, setAviso] = useState<{ tipo: 'success' | 'error'; texto: string } | null>(null);
  const paginado = usePaginacion(alumnos);

  useEffect(() => {
    alumnosAdminService.setToken(token);
    datosMaestrosService.setToken(token);
    datosMaestrosService.listarNiveles().then(setNiveles);
    datosMaestrosService.listarCursos().then(setCursos);
  }, [token]);

  const filtros = () => ({
    busqueda: texto.trim(),
    nivelId: filtroNivel ? Number(filtroNivel) : undefined,
    cursoId: filtroCurso ? Number(filtroCurso) : undefined,
  });

  useEffect(() => {
    setCargando(true);
    const demora = setTimeout(() => {
      alumnosAdminService
        .listar(filtros())
        .then(setAlumnos)
        .catch(() => setAviso({ tipo: 'error', texto: 'No se pudieron cargar los alumnos' }))
        .finally(() => setCargando(false));
    }, 300);
    return () => clearTimeout(demora);
  }, [texto, filtroNivel, filtroCurso, token]);

  const cursosFiltrados = cursos.filter((c) => !filtroNivel || c.nivelId === Number(filtroNivel));

  const cambiarNivel = (nivelId: string) => {
    setFiltroNivel(nivelId);
    if (filtroCurso && !cursos.some((c) => c.id === Number(filtroCurso) && (!nivelId || c.nivelId === Number(nivelId)))) {
      setFiltroCurso('');
    }
  };

  const recargar = () => alumnosAdminService.listar(filtros()).then(setAlumnos);

  const alGuardar = (guardado: AlumnoFicha, esNuevo: boolean) => {
    setDialogo({ abierto: false, alumno: null });
    setAviso({ tipo: 'success', texto: esNuevo ? `Alumno ${guardado.apellido}, ${guardado.nombre} dado de alta` : 'Datos actualizados' });
    recargar();
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, gap: 2, flexWrap: 'wrap' }}>
        <Typography variant="h5" sx={{ fontWeight: 600 }}>Alumnos</Typography>
        {esAdmin && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setDialogo({ abierto: true, alumno: null })}
            sx={{ background: GRADIENTE_PRINCIPAL, fontWeight: 600 }}
          >
            Nuevo alumno
          </Button>
        )}
      </Box>

      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '2fr 1fr 1fr' }, gap: 2, mb: 2 }}>
        <TextField
          placeholder="Buscar por nombre, apellido, legajo o DNI"
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          size="small"
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
        <TextField select label="Nivel" value={filtroNivel} onChange={(e) => cambiarNivel(e.target.value)} size="small">
          <MenuItem value="">Todos los niveles</MenuItem>
          {niveles.map((n) => (
            <MenuItem key={n.id} value={String(n.id)}>{n.nombre}</MenuItem>
          ))}
        </TextField>
        <TextField select label="Curso" value={filtroCurso} onChange={(e) => setFiltroCurso(e.target.value)} size="small">
          <MenuItem value="">Todos los cursos</MenuItem>
          {cursosFiltrados.map((c) => (
            <MenuItem key={c.id} value={String(c.id)}>{c.descripcion}</MenuItem>
          ))}
        </TextField>
      </Box>

      <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>
        {cargando ? 'Buscando…' : `${alumnos.length} ${alumnos.length === 1 ? 'alumno' : 'alumnos'}`}
      </Typography>

      <TableContainer component={Paper} sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <Table size="small">
          <TableHead>
            <TableRow sx={{ '& th': { fontWeight: 700, backgroundColor: '#F5F6FA' } }}>
              <TableCell>Legajo</TableCell>
              <TableCell>Alumno</TableCell>
              <TableCell>DNI</TableCell>
              <TableCell>Curso</TableCell>
              <TableCell>Estado</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cargando && alumnos.length === 0 && <FilasCargando columnas={5} />}
            {paginado.visibles.map((a) => (
              <TableRow
                key={a.id}
                hover
                onClick={() => navigate(`/alumnos/${a.id}`)}
                sx={{ cursor: 'pointer' }}
              >
                <TableCell>{a.legajo}</TableCell>
                <TableCell>{a.apellido}, {a.nombre}</TableCell>
                <TableCell>{a.dni}</TableCell>
                <TableCell>{a.cursoDescripcion}</TableCell>
                <TableCell>
                  <Chip size="small" label={a.estado} color={COLORES_ESTADO[a.estado]} variant="outlined" />
                </TableCell>
              </TableRow>
            ))}
            {!cargando && alumnos.length === 0 && (
              <TableRow>
                <TableCell colSpan={5}>
                  <EstadoVacio
                    titulo="No hay alumnos con esos filtros"
                    descripcion="Probá con otro nombre, legajo o DNI, o quitá el filtro de nivel o curso."
                  />
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

      <AlumnoDialog
        token={token}
        open={dialogo.abierto}
        alumno={dialogo.alumno}
        cursos={cursos}
        editable={esAdmin}
        onClose={() => setDialogo({ abierto: false, alumno: null })}
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
