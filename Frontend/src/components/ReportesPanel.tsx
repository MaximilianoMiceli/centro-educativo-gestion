import React, { useEffect, useState } from 'react';
import {
  Alert,
  Autocomplete,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { PictureAsPdf as PdfIcon, TableChart as ExcelIcon, Assessment as ReporteIcon } from '@mui/icons-material';
import { alumnosAdminService } from '../services/alumnosAdminService';
import type { AlumnoFicha } from '../services/alumnosAdminService';
import { datosMaestrosService } from '../services/datosMaestrosService';
import type { CursoDTO, MateriaDTO, NivelDTO } from '../services/datosMaestrosService';
import { docenteService } from '../services/docenteService';
import type { DocenteDTO } from '../services/docenteService';
import { serviciosService } from '../services/serviciosService';
import type { Deporte, Recorrido } from '../services/serviciosService';
import { reportesService } from '../services/reportesService';
import type { CatalogoReporte, FormatoDescarga, Reporte, SeccionReporte } from '../services/reportesService';
import { PaginadorTabla } from './PaginadorTabla';
import { usePaginacion } from '../utils/usePaginacion';
import { CLAVE_FILTRO, REGLAS_REPORTES, faltantes } from '../utils/reportes';
import type { FiltroReporte, FiltrosSeleccionados } from '../utils/reportes';

interface ReportesPanelProps {
  token: string;
}

interface Opciones {
  niveles: NivelDTO[];
  cursos: CursoDTO[];
  materias: MateriaDTO[];
  docentes: DocenteDTO[];
  deportes: Deporte[];
  recorridos: Recorrido[];
}

const OPCIONES_VACIAS: Opciones = {
  niveles: [],
  cursos: [],
  materias: [],
  docentes: [],
  deportes: [],
  recorridos: [],
};

const ETIQUETAS: Record<FiltroReporte, string> = {
  alumno: 'Alumno',
  docente: 'Docente',
  nivel: 'Nivel',
  curso: 'Curso',
  materia: 'Materia',
  deporte: 'Deporte',
  recorrido: 'Recorrido',
};

const TablaSeccion: React.FC<{ seccion: SeccionReporte }> = ({ seccion }) => {
  const paginado = usePaginacion(seccion.filas);

  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="subtitle2" color="primary" sx={{ fontWeight: 700, mb: 1 }}>
        {seccion.titulo}
      </Typography>
      <TableContainer sx={{ border: '1px solid #e4ecf4', borderRadius: 1 }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              {seccion.columnas.map((columna) => (
                <TableCell key={columna} sx={{ fontWeight: 700 }}>
                  {columna}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {seccion.filas.length === 0 && (
              <TableRow>
                <TableCell colSpan={seccion.columnas.length} align="center">
                  Sin resultados
                </TableCell>
              </TableRow>
            )}
            {paginado.visibles.map((fila, indice) => (
              <TableRow key={indice} hover>
                {fila.map((celda, columna) => (
                  <TableCell key={columna}>{celda}</TableCell>
                ))}
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
    </Box>
  );
};


export const ReportesPanel: React.FC<ReportesPanelProps> = ({ token }) => {
  const [catalogo, setCatalogo] = useState<CatalogoReporte[]>([]);
  const [opciones, setOpciones] = useState<Opciones>(OPCIONES_VACIAS);
  const [slug, setSlug] = useState('');
  const [filtros, setFiltros] = useState<FiltrosSeleccionados>({});
  const [alumnoElegido, setAlumnoElegido] = useState<AlumnoFicha | null>(null);
  const [resultados, setResultados] = useState<AlumnoFicha[]>([]);
  const [buscandoAlumnos, setBuscandoAlumnos] = useState(false);
  const [reporte, setReporte] = useState<Reporte | null>(null);
  const [generando, setGenerando] = useState(false);
  const [descargando, setDescargando] = useState<FormatoDescarga | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    reportesService.setToken(token);
    alumnosAdminService.setToken(token);
    datosMaestrosService.setToken(token);
    docenteService.setToken(token);
    serviciosService.setToken(token);

    Promise.all([
      reportesService.catalogo(),
      datosMaestrosService.listarNiveles(),
      datosMaestrosService.listarCursos(),
      datosMaestrosService.listarMaterias(),
      docenteService.listar(),
      serviciosService.listarDeportes(),
      serviciosService.listarRecorridos(),
    ])
      .then(([catalogoRecibido, niveles, cursos, materias, docentes, deportes, recorridos]) => {
        setCatalogo(catalogoRecibido);
        setOpciones({ niveles, cursos, materias, docentes, deportes, recorridos });
      })
      .catch(() => setError('No se pudieron cargar los datos para los reportes'));
  }, [token]);

  const reglas = REGLAS_REPORTES[slug];
  const faltan = slug ? faltantes(slug, filtros) : [];
  const listo = Boolean(slug) && faltan.length === 0;

  const cambiarReporte = (nuevoSlug: string) => {
    setSlug(nuevoSlug);
    setFiltros({});
    setAlumnoElegido(null);
    setResultados([]);
    setReporte(null);
    setError('');
  };

  const cambiarFiltro = (filtro: FiltroReporte, valor: number | undefined) => {
    setReporte(null);
    setFiltros((actual) => {
      const siguiente = { ...actual, [CLAVE_FILTRO[filtro]]: valor };
      if (filtro === 'nivel') {
        const cursoActual = opciones.cursos.find((curso) => curso.id === actual.cursoId);
        if (cursoActual && cursoActual.nivelId !== valor) {
          siguiente.cursoId = undefined;
        }
      }
      return siguiente;
    });
  };

  const buscarAlumnos = (texto: string) => {
    if (texto.trim().length < 2) {
      setResultados([]);
      return;
    }
    setBuscandoAlumnos(true);
    alumnosAdminService
      .listar({ busqueda: texto.trim() })
      .then(setResultados)
      .catch(() => setResultados([]))
      .finally(() => setBuscandoAlumnos(false));
  };

  const generar = async () => {
    setError('');
    setGenerando(true);
    try {
      setReporte(await reportesService.generar(slug, filtros));
    } catch {
      setError('No se pudo generar el reporte. Revisá los filtros.');
    } finally {
      setGenerando(false);
    }
  };

  const descargar = async (formato: FormatoDescarga) => {
    setError('');
    setDescargando(formato);
    try {
      await reportesService.descargar(slug, formato, filtros);
    } catch {
      setError('No se pudo descargar el archivo.');
    } finally {
      setDescargando(null);
    }
  };

  const cursosDelNivel = opciones.cursos.filter(
    (curso) => filtros.nivelId === undefined || curso.nivelId === filtros.nivelId,
  );

  const opcionesDe = (filtro: FiltroReporte): { id: number; label: string }[] => {
    switch (filtro) {
      case 'nivel':
        return opciones.niveles.map((nivel) => ({ id: nivel.id, label: nivel.nombre }));
      case 'curso':
        return cursosDelNivel.map((curso) => ({ id: curso.id, label: curso.descripcion }));
      case 'materia':
        return opciones.materias.map((materia) => ({ id: materia.id, label: materia.nombre }));
      case 'docente':
        return opciones.docentes.map((docente) => ({
          id: docente.id,
          label: `${docente.apellido}, ${docente.nombre}`,
        }));
      case 'deporte':
        return opciones.deportes.map((deporte) => ({ id: deporte.id, label: deporte.nombre }));
      case 'recorrido':
        return opciones.recorridos.map((recorrido) => ({ id: recorrido.id, label: recorrido.nombre }));
      default:
        return [];
    }
  };

  const renderFiltro = (filtro: FiltroReporte) => {
    const requerido = reglas?.requeridos.includes(filtro) ?? false;

    if (filtro === 'alumno') {
      return (
        <Autocomplete
          key={filtro}
          sx={{ minWidth: 300, flex: 1 }}
          options={resultados}
          value={alumnoElegido}
          loading={buscandoAlumnos}
          getOptionLabel={(alumno) => `${alumno.apellido}, ${alumno.nombre} (${alumno.legajo})`}
          isOptionEqualToValue={(opcion, valor) => opcion.id === valor.id}
          filterOptions={(opciones) => opciones}
          onInputChange={(_evento, texto, motivo) => {
            if (motivo === 'input') buscarAlumnos(texto);
          }}
          onChange={(_evento, alumno) => {
            setAlumnoElegido(alumno);
            cambiarFiltro('alumno', alumno?.id);
          }}
          renderInput={(params) => (
            <TextField {...params} label="Alumno (apellido, nombre o legajo)" required={requerido} size="small" />
          )}
        />
      );
    }

    const opcionesFiltro = opcionesDe(filtro);
    const valor = filtros[CLAVE_FILTRO[filtro]];
    return (
      <FormControl key={filtro} size="small" sx={{ minWidth: 220, flex: 1 }} required={requerido}>
        <InputLabel>{ETIQUETAS[filtro]}</InputLabel>
        <Select
          label={ETIQUETAS[filtro]}
          value={valor ?? ''}
          onChange={(evento) => {
            const texto = String(evento.target.value);
            cambiarFiltro(filtro, texto === '' ? undefined : Number(texto));
          }}
        >
          {!requerido && (
            <MenuItem value="">
              <em>Todos</em>
            </MenuItem>
          )}
          {opcionesFiltro.map((opcion) => (
            <MenuItem key={opcion.id} value={opcion.id}>
              {opcion.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    );
  };

  return (
    <Card sx={{ boxShadow: '0 4px 12px rgba(0,0,0,0.08)' }}>
      <CardContent>
        <Stack direction="row" spacing={1} sx={{ mb: 2, alignItems: 'center' }}>
          <ReporteIcon color="primary" />
          <Typography variant="h6" sx={{ fontWeight: 600 }}>
            Reportes
          </Typography>
        </Stack>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
            {error}
          </Alert>
        )}

        <Stack spacing={2}>
          <FormControl size="small" fullWidth>
            <InputLabel>Reporte</InputLabel>
            <Select label="Reporte" value={slug} onChange={(evento) => cambiarReporte(String(evento.target.value))}>
              {catalogo.map((item) => (
                <MenuItem key={item.slug} value={item.slug}>
                  {item.codigo} · {item.titulo}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          {reglas && (
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} useFlexGap sx={{ flexWrap: 'wrap' }}>
              {reglas.filtros.map((filtro) => renderFiltro(filtro))}
            </Stack>
          )}

          {faltan.length > 0 && (
            <Typography variant="caption" color="text.secondary">
              Completá los filtros obligatorios para generar el reporte.
            </Typography>
          )}

          {slug && (
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
              <Button variant="contained" onClick={generar} disabled={!listo || generando}>
                {generando ? <CircularProgress size={20} color="inherit" /> : 'Vista previa'}
              </Button>
              <Button
                variant="outlined"
                startIcon={<PdfIcon />}
                onClick={() => descargar('pdf')}
                disabled={!listo || descargando !== null}
              >
                {descargando === 'pdf' ? 'Descargando…' : 'Descargar PDF'}
              </Button>
              <Button
                variant="outlined"
                startIcon={<ExcelIcon />}
                onClick={() => descargar('excel')}
                disabled={!listo || descargando !== null}
              >
                {descargando === 'excel' ? 'Descargando…' : 'Descargar Excel'}
              </Button>
            </Stack>
          )}

          {reporte && (
            <Box sx={{ mt: 1 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                {reporte.codigo} · {reporte.titulo}
              </Typography>
              {reporte.filtros.length > 0 && (
                <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap', my: 1 }}>
                  {reporte.filtros.map((texto) => (
                    <Chip key={texto} size="small" label={texto} />
                  ))}
                </Stack>
              )}

              {reporte.secciones.map((seccion) => (
                <TablaSeccion key={seccion.titulo} seccion={seccion} />
              ))}
            </Box>
          )}
        </Stack>
      </CardContent>
    </Card>
  );
};
