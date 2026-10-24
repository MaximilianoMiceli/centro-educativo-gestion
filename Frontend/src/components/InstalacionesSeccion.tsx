import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import {
  ETIQUETA_ESTADO,
  ETIQUETA_TIPO,
  instalacionesService,
} from '../services/instalacionesService';
import type {
  EstadoInstalacion,
  InstalacionDTO,
  SolicitudInstalacion,
  TipoInstalacion,
} from '../services/instalacionesService';
import { PaginadorTabla } from './PaginadorTabla';
import { usePaginacion } from '../utils/usePaginacion';

interface InstalacionesSeccionProps {
  onAviso: (aviso: { tipo: 'success' | 'error'; texto: string }) => void;
}

interface Formulario {
  nombre: string;
  tipo: TipoInstalacion;
  ubicacion: string;
  capacidad: string;
  estado: EstadoInstalacion;
  descripcion: string;
}

const FORMULARIO_VACIO: Formulario = {
  nombre: '',
  tipo: 'OTRA',
  ubicacion: '',
  capacidad: '',
  estado: 'ACTIVA',
  descripcion: '',
};

const COLOR_ESTADO: Record<EstadoInstalacion, 'success' | 'warning' | 'default'> = {
  ACTIVA: 'success',
  EN_MANTENIMIENTO: 'warning',
  INACTIVA: 'default',
};

const TIPOS = Object.keys(ETIQUETA_TIPO) as TipoInstalacion[];
const ESTADOS = Object.keys(ETIQUETA_ESTADO) as EstadoInstalacion[];

export const InstalacionesSeccion: React.FC<InstalacionesSeccionProps> = ({ onAviso }) => {
  const [instalaciones, setInstalaciones] = useState<InstalacionDTO[]>([]);
  const [dialogo, setDialogo] = useState<{ abierto: boolean; instalacion: InstalacionDTO | null }>({
    abierto: false,
    instalacion: null,
  });
  const [form, setForm] = useState<Formulario>(FORMULARIO_VACIO);
  const [errores, setErrores] = useState<Record<string, string>>({});
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null);
  const paginado = usePaginacion(instalaciones);

  const cargar = () => instalacionesService.listar().then(setInstalaciones);

  useEffect(() => {
    instalacionesService
      .listar()
      .then(setInstalaciones)
      .catch(() => onAviso({ tipo: 'error', texto: 'No se pudieron cargar las instalaciones' }));
  }, [onAviso]);

  const abrir = (instalacion: InstalacionDTO | null) => {
    setErrores({});
    setErrorGeneral(null);
    setForm(instalacion
      ? {
          nombre: instalacion.nombre,
          tipo: instalacion.tipo,
          ubicacion: instalacion.ubicacion ?? '',
          capacidad: instalacion.capacidad?.toString() ?? '',
          estado: instalacion.estado,
          descripcion: instalacion.descripcion ?? '',
        }
      : FORMULARIO_VACIO);
    setDialogo({ abierto: true, instalacion });
  };

  const cambiar = (campo: keyof Formulario, valor: string) => {
    setForm((previo) => ({ ...previo, [campo]: valor }));
  };

  const guardar = async () => {
    const datos: SolicitudInstalacion = {
      nombre: form.nombre,
      tipo: form.tipo,
      ubicacion: form.ubicacion.trim() || null,
      capacidad: form.capacidad.trim() ? Number(form.capacidad) : null,
      estado: form.estado,
      descripcion: form.descripcion.trim() || null,
    };
    try {
      const actual = dialogo.instalacion;
      if (actual) {
        await instalacionesService.modificar(actual.id, datos);
        onAviso({ tipo: 'success', texto: `Instalación "${datos.nombre}" actualizada` });
      } else {
        await instalacionesService.crear(datos);
        onAviso({ tipo: 'success', texto: `Instalación "${datos.nombre}" creada` });
      }
      setDialogo({ abierto: false, instalacion: null });
      await cargar();
    } catch (err: any) {
      setErrores(err.response?.data?.errores ?? {});
      setErrorGeneral(err.response?.data?.errores ? null : (err.response?.data?.message ?? 'No se pudo guardar la instalación'));
    }
  };

  const campo = (nombre: keyof Formulario, etiqueta: string, extra: { multilinea?: boolean; tipo?: string } = {}) => (
    <TextField
      label={etiqueta}
      value={form[nombre]}
      onChange={(e) => cambiar(nombre, e.target.value)}
      error={!!errores[nombre]}
      helperText={errores[nombre] ?? ' '}
      multiline={extra.multilinea}
      type={extra.tipo}
      size="small"
      fullWidth
    />
  );

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, gap: 2, flexWrap: 'wrap' }}>
        <Typography variant="h6" sx={{ fontWeight: 600 }}>Instalaciones</Typography>
        <Button variant="contained" onClick={() => abrir(null)}>Nueva instalación</Button>
      </Box>

      <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>
        {instalaciones.length} {instalaciones.length === 1 ? 'instalación' : 'instalaciones'}
      </Typography>

      <TableContainer component={Paper} sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <Table size="small">
          <TableHead>
            <TableRow sx={{ '& th': { fontWeight: 700, backgroundColor: '#F5F6FA' } }}>
              <TableCell>Nombre</TableCell>
              <TableCell>Tipo</TableCell>
              <TableCell>Ubicación</TableCell>
              <TableCell>Capacidad</TableCell>
              <TableCell>Estado</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {paginado.visibles.map((i) => (
              <TableRow key={i.id} hover onClick={() => abrir(i)} sx={{ cursor: 'pointer' }}>
                <TableCell>{i.nombre}</TableCell>
                <TableCell>{ETIQUETA_TIPO[i.tipo]}</TableCell>
                <TableCell>{i.ubicacion ?? '—'}</TableCell>
                <TableCell>{i.capacidad ?? '—'}</TableCell>
                <TableCell>
                  <Chip size="small" label={ETIQUETA_ESTADO[i.estado]} color={COLOR_ESTADO[i.estado]} variant="outlined" />
                </TableCell>
              </TableRow>
            ))}
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

      <Dialog open={dialogo.abierto} onClose={() => setDialogo({ abierto: false, instalacion: null })} fullWidth maxWidth="sm">
        <DialogTitle>{dialogo.instalacion ? 'Editar instalación' : 'Nueva instalación'}</DialogTitle>
        <DialogContent>
          {errorGeneral && <Alert severity="error" sx={{ mb: 2 }}>{errorGeneral}</Alert>}
          <Card variant="outlined" sx={{ boxShadow: 'none' }}>
            <CardContent sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
              <Box sx={{ gridColumn: { sm: '1 / -1' } }}>{campo('nombre', 'Nombre')}</Box>
              <TextField
                select
                label="Tipo"
                value={form.tipo}
                onChange={(e) => cambiar('tipo', e.target.value)}
                error={!!errores.tipo}
                helperText={errores.tipo ?? ' '}
                size="small"
                fullWidth
              >
                {TIPOS.map((tipo) => (
                  <MenuItem key={tipo} value={tipo}>{ETIQUETA_TIPO[tipo]}</MenuItem>
                ))}
              </TextField>
              <TextField
                select
                label="Estado"
                value={form.estado}
                onChange={(e) => cambiar('estado', e.target.value)}
                error={!!errores.estado}
                helperText={errores.estado ?? ' '}
                size="small"
                fullWidth
              >
                {ESTADOS.map((estado) => (
                  <MenuItem key={estado} value={estado}>{ETIQUETA_ESTADO[estado]}</MenuItem>
                ))}
              </TextField>
              {campo('ubicacion', 'Ubicación')}
              {campo('capacidad', 'Capacidad (personas)', { tipo: 'number' })}
              <Box sx={{ gridColumn: { sm: '1 / -1' } }}>{campo('descripcion', 'Descripción', { multilinea: true })}</Box>
            </CardContent>
          </Card>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setDialogo({ abierto: false, instalacion: null })}>Cancelar</Button>
          <Button variant="contained" onClick={guardar}>Guardar</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
