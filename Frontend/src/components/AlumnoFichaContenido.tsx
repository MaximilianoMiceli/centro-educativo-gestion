import React, { useEffect, useState } from 'react';
import { Alert, Box, Button, List, ListItemText, MenuItem, Tab, Tabs, TextField, Typography } from '@mui/material';
import { alumnosAdminService } from '../services/alumnosAdminService';
import type { AlumnoFicha, AlumnoMateria, DatosAlumno } from '../services/alumnosAdminService';
import type { CursoDTO } from '../services/datosMaestrosService';
import { ServiciosAlumno } from './ServiciosAlumno';
import { EstadoVacio } from './EstadoVacio';

interface AlumnoFichaContenidoProps {
  alumno: AlumnoFicha | null;
  cursos: CursoDTO[];
  editable: boolean;
  onGuardado: (alumno: AlumnoFicha, esNuevo: boolean) => void;
}

type Errores = Record<string, string>;
type Pestana = 'datos' | 'materias' | 'servicios';

const FORMULARIO_VACIO: DatosAlumno = {
  legajo: '',
  dni: '',
  nombre: '',
  apellido: '',
  fechaNacimiento: '',
  domicilio: '',
  telefono: '',
  email: '',
  cursoId: 0,
  estado: 'ACTIVO',
};

function aFormulario(alumno: AlumnoFicha): DatosAlumno {
  return {
    legajo: alumno.legajo,
    dni: alumno.dni,
    nombre: alumno.nombre,
    apellido: alumno.apellido,
    fechaNacimiento: alumno.fechaNacimiento,
    domicilio: alumno.domicilio ?? '',
    telefono: alumno.telefono ?? '',
    email: alumno.email ?? '',
    cursoId: alumno.cursoId,
    estado: alumno.estado,
  };
}

/**
 * Contenido de la ficha de un alumno: datos, materias y servicios. Se usa dentro del diálogo y en la página
 * completa, así que la pantalla que lo contiene decide si se muestra como ventana o como página.
 */
export const AlumnoFichaContenido: React.FC<AlumnoFichaContenidoProps> = ({ alumno, cursos, editable, onGuardado }) => {
  const [pestana, setPestana] = useState<Pestana>('datos');
  const [form, setForm] = useState<DatosAlumno>(alumno ? aFormulario(alumno) : FORMULARIO_VACIO);
  const [materias, setMaterias] = useState<AlumnoMateria[] | null>(null);
  const [errores, setErrores] = useState<Errores>({});
  const [error, setError] = useState<string | null>(null);
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    if (alumno) {
      alumnosAdminService.materias(alumno.id).then(setMaterias).catch(() => setMaterias([]));
    }
  }, [alumno]);

  const cambiar = (campo: keyof DatosAlumno, valor: string | number) => {
    setForm((previo) => ({ ...previo, [campo]: valor }));
  };

  const guardar = async () => {
    setGuardando(true);
    setErrores({});
    setError(null);
    try {
      const guardado = alumno
        ? await alumnosAdminService.modificar(alumno.id, form)
        : await alumnosAdminService.crear(form);
      onGuardado(guardado, !alumno);
    } catch (err: any) {
      const status = err.response?.status;
      if (status === 400) {
        setErrores(err.response.data?.errores ?? {});
        setError(err.response.data?.message ?? 'Revisá los datos ingresados');
      } else {
        setError(err.response?.data?.message ?? 'No se pudo guardar el alumno');
      }
    } finally {
      setGuardando(false);
    }
  };

  const soloLectura = !editable;
  const campo = (nombre: keyof DatosAlumno, etiqueta: string, tipo = 'text', deshabilitado = false) => (
    <TextField
      label={etiqueta}
      type={tipo}
      value={form[nombre]}
      onChange={(e) => cambiar(nombre, e.target.value)}
      disabled={soloLectura || deshabilitado || guardando}
      error={!!errores[nombre]}
      helperText={errores[nombre] ?? ' '}
      size="small"
      fullWidth
      slotProps={tipo === 'date' ? { inputLabel: { shrink: true } } : undefined}
    />
  );

  return (
    <Box>
      {alumno && (
        <Tabs
          value={pestana}
          onChange={(_e, nueva: Pestana) => setPestana(nueva)}
          sx={{ px: { xs: 1, sm: 3 }, borderBottom: '1px solid #e4ecf4' }}
          variant="scrollable"
          scrollButtons="auto"
        >
          <Tab value="datos" label="Datos" />
          <Tab value="materias" label="Materias cursadas" />
          <Tab value="servicios" label="Servicios" />
        </Tabs>
      )}

      <Box sx={{ p: { xs: 2, sm: 3 } }}>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        {pestana === 'datos' && (
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, pt: 1 }}>
            {campo('legajo', 'Legajo', 'text', !!alumno)}
            {campo('dni', 'DNI')}
            {campo('nombre', 'Nombre')}
            {campo('apellido', 'Apellido')}
            {campo('fechaNacimiento', 'Fecha de nacimiento', 'date')}
            <TextField
              select
              label="Curso"
              value={form.cursoId || ''}
              onChange={(e) => cambiar('cursoId', Number(e.target.value))}
              disabled={soloLectura || guardando}
              error={!!errores.cursoId}
              helperText={errores.cursoId ?? ' '}
              size="small"
              fullWidth
            >
              {cursos.map((c) => (
                <MenuItem key={c.id} value={c.id}>{c.descripcion}</MenuItem>
              ))}
            </TextField>
            {campo('domicilio', 'Domicilio')}
            {campo('telefono', 'Teléfono')}
            {campo('email', 'Email')}
            <TextField
              select
              label="Estado"
              value={form.estado}
              onChange={(e) => cambiar('estado', e.target.value)}
              disabled={soloLectura || guardando}
              error={!!errores.estado}
              helperText={errores.estado ?? ' '}
              size="small"
              fullWidth
            >
              <MenuItem value="ACTIVO">Activo</MenuItem>
              <MenuItem value="INACTIVO">Inactivo</MenuItem>
              <MenuItem value="EGRESADO">Egresado</MenuItem>
            </TextField>
          </Box>
        )}

        {pestana === 'servicios' && alumno && <ServiciosAlumno alumnoId={alumno.id} editable={editable} />}

        {pestana === 'materias' && (
          <List dense>
            {materias === null && (
              <Typography variant="body2" sx={{ color: '#666', p: 1 }}>Cargando materias…</Typography>
            )}
            {materias?.length === 0 && (
              <EstadoVacio
                titulo="Sin materias asignadas"
                descripcion="El curso del alumno todavía no tiene materias con docente asignado."
              />
            )}
            {materias?.map((m) => (
              <ListItemText
                key={m.materia}
                sx={{ px: 1, py: 0.5 }}
                primary={`${m.materia} · ${m.docente}`}
                secondary={m.horarios.length ? m.horarios.join(' · ') : 'Sin horarios cargados'}
              />
            ))}
          </List>
        )}
      </Box>

      {editable && pestana === 'datos' && (
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', px: { xs: 2, sm: 3 }, pb: 2 }}>
          <Button variant="contained" onClick={guardar} disabled={guardando}>
            {alumno ? 'Guardar cambios' : 'Dar de alta'}
          </Button>
        </Box>
      )}
    </Box>
  );
};
