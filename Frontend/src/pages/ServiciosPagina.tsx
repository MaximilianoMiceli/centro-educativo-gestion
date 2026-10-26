import React, { useEffect, useState } from 'react';
import { Alert, Box, Card, CardContent, MenuItem, Paper, Stack, Tab, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tabs, TextField, Typography } from '@mui/material';
import { listadosServiciosService } from '../services/listadosServiciosService';
import type { AtencionListado, InscriptoApoyo, InscriptoIdioma } from '../services/listadosServiciosService';
import { datosMaestrosService } from '../services/datosMaestrosService';
import type { CursoDTO, NivelDTO } from '../services/datosMaestrosService';
import { serviciosService } from '../services/serviciosService';
import type { Idioma } from '../services/serviciosService';
import { EstadoVacio } from '../components/EstadoVacio';
import { FilasCargando } from '../components/FilasCargando';
import { PaginadorTabla } from '../components/PaginadorTabla';
import { usePaginacion } from '../utils/usePaginacion';
import { rolDelToken } from '../utils/token';

interface ServiciosPaginaProps {
  token: string;
}

type Pestana = 'idiomas' | 'apoyo' | 'enfermeria';

const fecha = (valor: string) => new Date(valor.length > 10 ? valor : `${valor}T00:00:00`).toLocaleDateString('es-AR');
const fechaHora = (valor: string) => new Date(valor).toLocaleString('es-AR');

/** Quiénes están inscriptos en cada servicio, con filtros por nivel, curso e idioma. */
export const ServiciosPagina: React.FC<ServiciosPaginaProps> = ({ token }) => {
  const esAdmin = rolDelToken(token) === 'ADMIN';
  const [pestana, setPestana] = useState<Pestana>('idiomas');
  const [niveles, setNiveles] = useState<NivelDTO[]>([]);
  const [cursos, setCursos] = useState<CursoDTO[]>([]);
  const [idiomas, setIdiomas] = useState<Idioma[]>([]);
  const [nivelId, setNivelId] = useState<number | ''>('');
  const [cursoId, setCursoId] = useState<number | ''>('');
  const [idiomaId, setIdiomaId] = useState<number | ''>('');
  const [filas, setFilas] = useState<(InscriptoIdioma | InscriptoApoyo | AtencionListado)[] | null>(null);
  const [error, setError] = useState('');
  const paginado = usePaginacion(filas ?? []);

  useEffect(() => {
    listadosServiciosService.setToken(token);
    datosMaestrosService.setToken(token);
    serviciosService.setToken(token);
    datosMaestrosService.listarNiveles().then(setNiveles).catch(() => setNiveles([]));
    datosMaestrosService.listarCursos().then(setCursos).catch(() => setCursos([]));
    serviciosService.listarIdiomas().then(setIdiomas).catch(() => setIdiomas([]));
  }, [token]);

  useEffect(() => {
    const filtros = {
      nivelId: nivelId === '' ? undefined : nivelId,
      cursoId: cursoId === '' ? undefined : cursoId,
      idiomaId: idiomaId === '' ? undefined : idiomaId,
    };
    setFilas(null);
    setError('');
    const consulta = pestana === 'idiomas'
      ? listadosServiciosService.idiomas(filtros)
      : pestana === 'apoyo'
        ? listadosServiciosService.apoyo({ nivelId: filtros.nivelId, cursoId: filtros.cursoId })
        : listadosServiciosService.enfermeria({ nivelId: filtros.nivelId, cursoId: filtros.cursoId });
    consulta
      .then(setFilas)
      .catch(() => setError('No se pudo cargar el listado'));
  }, [pestana, nivelId, cursoId, idiomaId]);

  const cursosDelNivel = cursos.filter((c) => nivelId === '' || c.nivelId === nivelId);
  const columnas = pestana === 'idiomas'
    ? ['Alumno', 'Legajo', 'Nivel', 'Curso', 'Idioma', 'Desde']
    : pestana === 'apoyo'
      ? ['Alumno', 'Legajo', 'Nivel', 'Curso', 'Desde', 'Observaciones']
      : ['Alumno', 'Legajo', 'Curso', 'Fecha', 'Motivo', 'Registrado por'];

  const celdas = (fila: InscriptoIdioma | InscriptoApoyo | AtencionListado): React.ReactNode[] => {
    if (pestana === 'idiomas') {
      const f = fila as InscriptoIdioma;
      return [f.alumno, f.legajo, f.nivel, f.curso, f.idioma, fecha(f.fechaInscripcion)];
    }
    if (pestana === 'apoyo') {
      const f = fila as InscriptoApoyo;
      return [f.alumno, f.legajo, f.nivel, f.curso, fecha(f.fechaInicio), f.observaciones ?? '—'];
    }
    const f = fila as AtencionListado;
    return [f.alumno, f.legajo, f.curso, fechaHora(f.fechaHora), f.motivo, f.registradoPor ?? '—'];
  };

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>Inscripciones a servicios</Typography>
        <Typography variant="body2" sx={{ color: '#666' }}>
          Quiénes cursan idiomas, participan del apoyo estudiantil y tuvieron atención en enfermería.
        </Typography>
      </Box>

      <Tabs
        value={pestana}
        onChange={(_e, nueva: Pestana) => {
          setPestana(nueva);
          setIdiomaId('');
        }}
        variant="scrollable"
        scrollButtons="auto"
        sx={{ mb: 2, borderBottom: '1px solid #e4ecf4' }}
      >
        <Tab value="idiomas" label="Idiomas" />
        <Tab value="apoyo" label="Apoyo estudiantil" />
        {esAdmin && <Tab value="enfermeria" label="Enfermería" />}
      </Tabs>

      <Card sx={{ mb: 2, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} useFlexGap sx={{ flexWrap: 'wrap' }}>
            <TextField select label="Nivel" value={nivelId} size="small" sx={{ minWidth: 200 }}
              onChange={(e) => {
                setNivelId(e.target.value === '' ? '' : Number(e.target.value));
                setCursoId('');
              }}>
              <MenuItem value="">Todos</MenuItem>
              {niveles.map((n) => <MenuItem key={n.id} value={n.id}>{n.nombre}</MenuItem>)}
            </TextField>
            <TextField select label="Curso" value={cursoId} size="small" sx={{ minWidth: 220 }}
              onChange={(e) => setCursoId(e.target.value === '' ? '' : Number(e.target.value))}>
              <MenuItem value="">Todos</MenuItem>
              {cursosDelNivel.map((c) => <MenuItem key={c.id} value={c.id}>{c.descripcion}</MenuItem>)}
            </TextField>
            {pestana === 'idiomas' && (
              <TextField select label="Idioma" value={idiomaId} size="small" sx={{ minWidth: 180 }}
                onChange={(e) => setIdiomaId(e.target.value === '' ? '' : Number(e.target.value))}>
                <MenuItem value="">Todos</MenuItem>
                {idiomas.map((i) => <MenuItem key={i.id} value={i.id}>{i.nombre}</MenuItem>)}
              </TextField>
            )}
          </Stack>
        </CardContent>
      </Card>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Paper sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)', borderRadius: 2, overflow: 'hidden' }}>
        {filas !== null && filas.length === 0 ? (
          <EstadoVacio
            titulo="Nadie inscripto con esos filtros"
            descripcion="Probá con otro nivel o curso, o inscribí alumnos desde su ficha."
          />
        ) : (
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow sx={{ '& th': { fontWeight: 700, backgroundColor: '#F5F6FA' } }}>
                  {columnas.map((c) => <TableCell key={c}>{c}</TableCell>)}
                </TableRow>
              </TableHead>
              <TableBody>
                {filas === null && <FilasCargando columnas={columnas.length} />}
                {filas !== null && paginado.visibles.map((fila, indice) => (
                  <TableRow key={indice} hover>
                    {celdas(fila).map((valor, columna) => <TableCell key={columna}>{valor}</TableCell>)}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>

      {filas !== null && filas.length > 0 && (
        <PaginadorTabla
          total={paginado.total}
          pagina={paginado.pagina}
          porPagina={paginado.porPagina}
          onPaginaChange={paginado.setPagina}
          onPorPaginaChange={paginado.cambiarPorPagina}
        />
      )}
    </Box>
  );
};
