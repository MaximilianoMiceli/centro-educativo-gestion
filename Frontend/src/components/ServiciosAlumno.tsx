import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  Divider,
  FormControlLabel,
  List,
  ListItemText,
  MenuItem,
  TextField,
  Typography,
} from '@mui/material';
import { serviciosService } from '../services/serviciosService';
import type {
  AtencionEnfermeria,
  Deporte,
  Idioma,
  Recorrido,
  ServiciosAlumno as Servicios,
} from '../services/serviciosService';
import { miServiciosService } from '../services/miServiciosService';
import { ConfirmDialog } from './ConfirmDialog';
import { EstadoVacio } from './EstadoVacio';

interface ServiciosAlumnoProps {
  alumnoId: number;
  /** Sesión: los servicios necesitan el token para autorizar las llamadas. */
  token: string;
  /** Permite inscribir y dar de baja. */
  editable: boolean;
  /** Vista del propio alumno: solo ve y gestiona deportes, transporte, comedor e idiomas. Apoyo y enfermería son de administración. */
  propio?: boolean;
}

/** Operaciones sobre los servicios, iguales para el administrador y para el propio alumno. */
interface ApiServicios {
  cargar: () => Promise<Servicios>;
  inscribirDeporte: (grupoId: number) => Promise<Servicios>;
  quitarDeporte: (inscripcionId: number) => Promise<Servicios>;
  asignarTransporte: (recorridoId: number) => Promise<Servicios>;
  quitarTransporte: () => Promise<Servicios>;
  asignarComedor: (inscripto: boolean) => Promise<Servicios>;
  inscribirIdioma: (idiomaId: number) => Promise<Servicios>;
  quitarIdioma: (inscripcionId: number) => Promise<Servicios>;
}

function apiAdministracion(alumnoId: number): ApiServicios {
  return {
    cargar: () => serviciosService.servicios(alumnoId),
    inscribirDeporte: (grupoId) => serviciosService.inscribirDeporte(alumnoId, grupoId),
    quitarDeporte: (inscripcionId) => serviciosService.quitarDeporte(alumnoId, inscripcionId),
    asignarTransporte: (recorridoId) => serviciosService.asignarTransporte(alumnoId, recorridoId),
    quitarTransporte: () => serviciosService.quitarTransporte(alumnoId),
    asignarComedor: (inscripto) => serviciosService.asignarComedor(alumnoId, inscripto),
    inscribirIdioma: (idiomaId) => serviciosService.inscribirIdioma(alumnoId, idiomaId),
    quitarIdioma: (inscripcionId) => serviciosService.quitarIdioma(alumnoId, inscripcionId),
  };
}

const apiPropia: ApiServicios = {
  cargar: () => miServiciosService.servicios(),
  inscribirDeporte: (grupoId) => miServiciosService.inscribirDeporte(grupoId),
  quitarDeporte: (inscripcionId) => miServiciosService.quitarDeporte(inscripcionId),
  asignarTransporte: (recorridoId) => miServiciosService.asignarTransporte(recorridoId),
  quitarTransporte: () => miServiciosService.quitarTransporte(),
  asignarComedor: (inscripto) => miServiciosService.asignarComedor(inscripto),
  inscribirIdioma: (idiomaId) => miServiciosService.inscribirIdioma(idiomaId),
  quitarIdioma: (inscripcionId) => miServiciosService.quitarIdioma(inscripcionId),
};

interface Baja {
  titulo: string;
  mensaje: string;
  operacion: () => Promise<Servicios>;
}

const fechaCorta = (valor: string) => new Date(`${valor}T00:00:00`).toLocaleDateString('es-AR');
const fechaHora = (valor: string) => new Date(valor).toLocaleString('es-AR');

export const ServiciosAlumno: React.FC<ServiciosAlumnoProps> = ({ alumnoId, token, editable, propio = false }) => {
  const api = propio ? apiPropia : apiAdministracion(alumnoId);
  const administra = editable && !propio;
  const [servicios, setServicios] = useState<Servicios | null>(null);
  const [deportes, setDeportes] = useState<Deporte[]>([]);
  const [recorridos, setRecorridos] = useState<Recorrido[]>([]);
  const [idiomas, setIdiomas] = useState<Idioma[]>([]);
  const [atenciones, setAtenciones] = useState<AtencionEnfermeria[]>([]);
  const [deporteSel, setDeporteSel] = useState('');
  const [grupoSel, setGrupoSel] = useState('');
  const [recorridoSel, setRecorridoSel] = useState('');
  const [idiomaSel, setIdiomaSel] = useState('');
  const [apoyoObservaciones, setApoyoObservaciones] = useState('');
  const [motivo, setMotivo] = useState('');
  const [observacionesEnfermeria, setObservacionesEnfermeria] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [baja, setBaja] = useState<Baja | null>(null);
  const [confirmando, setConfirmando] = useState(false);

  useEffect(() => {
    serviciosService.setToken(token);
    miServiciosService.setToken(token);
    setError(null);
    setAviso(null);
    setDeporteSel('');
    setGrupoSel('');
    setIdiomaSel('');
    setServicios(null);
    api.cargar().then(setServicios).catch(() => setError('No se pudieron cargar los servicios'));
    serviciosService.listarDeportes().then(setDeportes).catch(() => setDeportes([]));
    serviciosService.listarRecorridos().then(setRecorridos);
    serviciosService.listarIdiomas().then(setIdiomas);
    if (administra) {
      serviciosService.atencionesEnfermeria(alumnoId).then(setAtenciones).catch(() => setAtenciones([]));
    }
  }, [alumnoId, propio, administra]);

  const ejecutar = async (operacion: () => Promise<Servicios>, mensajeOk?: string) => {
    setError(null);
    setAviso(null);
    try {
      setServicios(await operacion());
      if (mensajeOk) setAviso(mensajeOk);
    } catch (err: any) {
      const data = err.response?.data;
      const detalle = data?.errores ? Object.values(data.errores).join(' · ') : null;
      setError(detalle ?? data?.message ?? 'No se pudo completar la operación');
    }
  };

  const pedirBaja = (titulo: string, mensaje: string, operacion: () => Promise<Servicios>) => {
    setBaja({ titulo, mensaje, operacion });
  };

  const confirmarBaja = async () => {
    if (!baja) return;
    setConfirmando(true);
    await ejecutar(baja.operacion, 'Baja registrada');
    setConfirmando(false);
    setBaja(null);
  };

  const registrarAtencion = async () => {
    setError(null);
    try {
      const nueva = await serviciosService.registrarAtencion(alumnoId, motivo, observacionesEnfermeria);
      setAtenciones((previas) => [nueva, ...previas]);
      setMotivo('');
      setObservacionesEnfermeria('');
    } catch (err: any) {
      const data = err.response?.data;
      const detalle = data?.errores ? Object.values(data.errores).join(' · ') : null;
      setError(detalle ?? data?.message ?? 'No se pudo registrar la atención');
    }
  };

  if (!servicios) {
    return error
      ? <Alert severity="error">{error}</Alert>
      : <Typography variant="body2" sx={{ color: '#666' }}>Cargando servicios…</Typography>;
  }

  const gruposDelDeporte = deportes
    .find((d) => String(d.id) === deporteSel)
    ?.grupos.filter((g) => g.nivel === servicios.nivel) ?? [];
  const deportesDisponibles = deportes.filter((d) =>
    d.grupos.some((g) => g.nivel === servicios.nivel)
      && !servicios.deportes.some((i) => i.deporteId === d.id));
  const idiomasDisponibles = idiomas.filter((i) => !servicios.idiomas.some((x) => x.idiomaId === i.id));
  const textoInscribir = propio ? 'Inscribirme' : 'Inscribir';

  return (
    <Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}
      {aviso && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setAviso(null)}>{aviso}</Alert>}

      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Deportes</Typography>
      <List dense>
        {servicios.deportes.map((i) => (
          <ListItemText
            key={i.id}
            sx={{ pl: 1 }}
            primary={`${i.deporte} · ${i.horario}`}
            secondary={editable ? (
              <Button
                size="small"
                color="error"
                onClick={() => pedirBaja('Dar de baja el deporte', `${i.deporte} dejará de figurar en su horario.`,
                  () => api.quitarDeporte(i.id))}
              >
                Dar de baja
              </Button>
            ) : undefined}
          />
        ))}
        {servicios.deportes.length === 0 && (
          <Typography variant="body2" sx={{ pl: 1, color: '#666' }}>Sin deportes inscriptos (máximo 2).</Typography>
        )}
      </List>

      {editable && servicios.deportes.length < 2 && (
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr auto' }, gap: 2, mt: 1, alignItems: 'flex-start' }}>
          <TextField
            select
            label="Deporte"
            value={deporteSel}
            onChange={(e) => {
              setDeporteSel(e.target.value);
              setGrupoSel('');
            }}
            size="small"
            helperText={deportesDisponibles.length === 0 ? 'No hay deportes con horario para su nivel' : ' '}
          >
            {deportesDisponibles.map((d) => (
              <MenuItem key={d.id} value={String(d.id)}>{d.nombre}</MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Grupo (horario)"
            value={grupoSel}
            onChange={(e) => setGrupoSel(e.target.value)}
            size="small"
            disabled={!deporteSel}
          >
            {gruposDelDeporte.map((g) => (
              <MenuItem key={g.id} value={String(g.id)}>{g.horario}</MenuItem>
            ))}
          </TextField>
          <Button
            variant="contained"
            disabled={!grupoSel}
            onClick={() => ejecutar(() => api.inscribirDeporte(Number(grupoSel)), 'Inscripción registrada')}
            sx={{ mt: 0.5 }}
          >
            {textoInscribir}
          </Button>
        </Box>
      )}

      <Divider sx={{ my: 2 }} />

      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Transporte</Typography>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap', mt: 1 }}>
        {servicios.transporte
          ? <Chip label={servicios.transporte.nombre} color="primary" variant="outlined" />
          : <Typography variant="body2" sx={{ color: '#666' }}>Sin transporte asignado</Typography>}
        {editable && (
          <>
            <TextField
              select
              label="Recorrido"
              value={recorridoSel}
              onChange={(e) => setRecorridoSel(e.target.value)}
              size="small"
              sx={{ minWidth: 220 }}
            >
              {recorridos.map((r) => (
                <MenuItem key={r.id} value={String(r.id)}>{r.nombre}</MenuItem>
              ))}
            </TextField>
            <Button
              variant="outlined"
              disabled={!recorridoSel}
              onClick={() => ejecutar(() => api.asignarTransporte(Number(recorridoSel)), 'Transporte asignado')}
            >
              {propio && servicios.transporte ? 'Cambiar' : 'Asignar'}
            </Button>
            {servicios.transporte && (
              <Button
                color="error"
                onClick={() => pedirBaja('Dar de baja el transporte', 'Deja de usar el recorrido asignado.', () => api.quitarTransporte())}
              >
                Dar de baja
              </Button>
            )}
          </>
        )}
      </Box>

      <Divider sx={{ my: 2 }} />

      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Comedor</Typography>
      <FormControlLabel
        control={
          <Checkbox
            checked={servicios.comedor}
            disabled={!editable}
            onChange={(e) => ejecutar(() => api.asignarComedor(e.target.checked),
              e.target.checked ? 'Inscripción al comedor registrada' : 'Baja del comedor registrada')}
          />
        }
        label={servicios.comedor ? 'Inscripto en el comedor' : 'No inscripto'}
      />

      <Divider sx={{ my: 2 }} />

      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Idiomas</Typography>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap', mt: 1 }}>
        {servicios.idiomas.length === 0 && (
          <Typography variant="body2" sx={{ color: '#666' }}>Sin idiomas inscriptos</Typography>
        )}
        {servicios.idiomas.map((i) => (
          <Chip
            key={i.id}
            label={`${i.idioma} · desde ${fechaCorta(i.fechaInscripcion)}`}
            color="secondary"
            variant="outlined"
            onDelete={editable ? () => pedirBaja('Dar de baja el idioma', `${i.idioma} se quita de su inscripción.`,
              () => api.quitarIdioma(i.id)) : undefined}
          />
        ))}
      </Box>
      {editable && idiomasDisponibles.length > 0 && (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap', mt: 1 }}>
          <TextField
            select
            label="Idioma"
            value={idiomaSel}
            onChange={(e) => setIdiomaSel(e.target.value)}
            size="small"
            sx={{ minWidth: 220 }}
          >
            {idiomasDisponibles.map((i) => (
              <MenuItem key={i.id} value={String(i.id)}>{i.nombre}</MenuItem>
            ))}
          </TextField>
          <Button
            variant="outlined"
            disabled={!idiomaSel}
            onClick={async () => {
              await ejecutar(() => api.inscribirIdioma(Number(idiomaSel)), 'Inscripción al idioma registrada');
              setIdiomaSel('');
            }}
          >
            {textoInscribir}
          </Button>
        </Box>
      )}

      {!propio && (
        <>
          <Divider sx={{ my: 2 }} />

          <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Servicio de apoyo estudiantil</Typography>
          {servicios.apoyo ? (
            <Box sx={{ mt: 1 }}>
              <Chip label={`Inscripto desde ${fechaCorta(servicios.apoyo.fechaInicio)}`} color="success" variant="outlined" />
              {servicios.apoyo.observaciones && (
                <Typography variant="body2" sx={{ mt: 1 }}>{servicios.apoyo.observaciones}</Typography>
              )}
            </Box>
          ) : (
            <Typography variant="body2" sx={{ color: '#666', mt: 1 }}>No inscripto</Typography>
          )}
          {administra && (
            <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2, flexWrap: 'wrap', mt: 1 }}>
              <TextField
                label="Observaciones (opcional)"
                value={apoyoObservaciones}
                onChange={(e) => setApoyoObservaciones(e.target.value)}
                size="small"
                sx={{ minWidth: 320 }}
              />
              <Button variant="outlined" onClick={() => ejecutar(() => serviciosService.asignarApoyo(alumnoId, apoyoObservaciones), 'Apoyo estudiantil actualizado')}>
                {servicios.apoyo ? 'Guardar observaciones' : 'Inscribir'}
              </Button>
              {servicios.apoyo && (
                <Button
                  color="error"
                  onClick={() => pedirBaja('Dar de baja del apoyo estudiantil', 'Se borran las observaciones cargadas.',
                    () => serviciosService.quitarApoyo(alumnoId))}
                >
                  Dar de baja
                </Button>
              )}
            </Box>
          )}
        </>
      )}

      {administra && (
        <>
          <Divider sx={{ my: 2 }} />

          <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Enfermería</Typography>
          <Typography variant="caption" sx={{ color: '#666', display: 'block', mb: 1 }}>
            Datos de salud: solo los ve quien administra alumnos.
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2, flexWrap: 'wrap' }}>
            <TextField
              label="Motivo"
              value={motivo}
              onChange={(e) => setMotivo(e.target.value)}
              size="small"
              sx={{ minWidth: 240 }}
            />
            <TextField
              label="Observaciones (opcional)"
              value={observacionesEnfermeria}
              onChange={(e) => setObservacionesEnfermeria(e.target.value)}
              size="small"
              sx={{ minWidth: 280 }}
            />
            <Button variant="contained" disabled={motivo.trim().length < 3} onClick={registrarAtencion}>
              Registrar atención
            </Button>
          </Box>
          <List dense>
            {atenciones.map((a) => (
              <ListItemText
                key={a.id}
                sx={{ pl: 1 }}
                primary={`${fechaHora(a.fechaHora)} · ${a.motivo}`}
                secondary={`${a.observaciones ?? 'Sin observaciones'} · registrado por ${a.registradoPor ?? '—'}`}
              />
            ))}
            {atenciones.length === 0 && (
              <EstadoVacio titulo="Sin atenciones registradas" descripcion="Cuando el alumno pase por enfermería, la atención aparecerá acá." />
            )}
          </List>
        </>
      )}

      <ConfirmDialog
        abierto={!!baja}
        titulo={baja?.titulo ?? ''}
        mensaje={baja?.mensaje ?? ''}
        textoConfirmar="Dar de baja"
        peligro
        cargando={confirmando}
        onConfirmar={confirmarBaja}
        onCancelar={() => setBaja(null)}
      />
    </Box>
  );
};
