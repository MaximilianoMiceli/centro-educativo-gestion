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
import { ConfirmDialog } from './ConfirmDialog';
import type {
  AtencionEnfermeria,
  Deporte,
  Idioma,
  Recorrido,
  ServiciosAlumno as Servicios,
} from '../services/serviciosService';

interface ServiciosAlumnoProps {
  alumnoId: number;
  editable: boolean;
}

const fechaCorta = (valor: string) => new Date(`${valor}T00:00:00`).toLocaleDateString('es-AR');
const fechaHora = (valor: string) => new Date(valor).toLocaleString('es-AR');

export const ServiciosAlumno: React.FC<ServiciosAlumnoProps> = ({ alumnoId, editable }) => {
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
  const [pendiente, setPendiente] = useState<{ titulo: string; mensaje: string; operacion: () => Promise<Servicios> } | null>(null);
  const [confirmando, setConfirmando] = useState(false);

  const cargar = async () => {
    setServicios(await serviciosService.servicios(alumnoId));
  };

  useEffect(() => {
    setError(null);
    setDeporteSel('');
    setGrupoSel('');
    setIdiomaSel('');
    cargar().catch(() => setError('No se pudieron cargar los servicios del alumno'));
    serviciosService.listarDeportes().then(setDeportes);
    serviciosService.listarRecorridos().then(setRecorridos);
    serviciosService.listarIdiomas().then(setIdiomas);
    if (editable) {
      serviciosService.atencionesEnfermeria(alumnoId).then(setAtenciones).catch(() => setAtenciones([]));
    }
  }, [alumnoId, editable]);

  const pedirBaja = (titulo: string, mensaje: string, operacion: () => Promise<Servicios>) => {
    setPendiente({ titulo, mensaje, operacion });
  };

  const confirmarBaja = async () => {
    if (!pendiente) return;
    setConfirmando(true);
    await ejecutar(pendiente.operacion);
    setConfirmando(false);
    setPendiente(null);
  };

  const ejecutar = async (operacion: () => Promise<Servicios>) => {
    setError(null);
    try {
      setServicios(await operacion());
    } catch (err: any) {
      const data = err.response?.data;
      const detalle = data?.errores ? Object.values(data.errores).join(' · ') : null;
      setError(detalle ?? data?.message ?? 'No se pudo completar la operación');
    }
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
    return <Typography variant="body2" sx={{ color: '#666' }}>Cargando servicios…</Typography>;
  }

  const gruposDelDeporte = deportes
    .find((d) => String(d.id) === deporteSel)
    ?.grupos.filter((g) => g.nivel === servicios.nivel) ?? [];
  const deportesDisponibles = deportes.filter((d) =>
    d.grupos.some((g) => g.nivel === servicios.nivel)
      && !servicios.deportes.some((i) => i.deporteId === d.id));
  const idiomasDisponibles = idiomas.filter((i) => !servicios.idiomas.some((x) => x.idiomaId === i.id));

  return (
    <Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>{error}</Alert>}

      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Deportes</Typography>
      <List dense>
        {servicios.deportes.map((i) => (
          <ListItemText
            key={i.id}
            sx={{ pl: 1 }}
            primary={`${i.deporte} · ${i.horario}`}
            secondary={editable ? (
              <Button size="small" color="error" onClick={() => pedirBaja('Dar de baja el deporte', `${i.deporte} dejará de figurar en su horario.`, () => serviciosService.quitarDeporte(alumnoId, i.id))}>
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
            onClick={() => ejecutar(() => serviciosService.inscribirDeporte(alumnoId, Number(grupoSel)))}
          >
            Inscribir
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
              onClick={() => ejecutar(() => serviciosService.asignarTransporte(alumnoId, Number(recorridoSel)))}
            >
              Asignar
            </Button>
            {servicios.transporte && (
              <Button color="error" onClick={() => pedirBaja('Dar de baja el transporte', 'El alumno deja de usar el recorrido asignado.', () => serviciosService.quitarTransporte(alumnoId))}>
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
            onChange={(e) => ejecutar(() => serviciosService.asignarComedor(alumnoId, e.target.checked))}
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
            onDelete={editable ? () => pedirBaja('Dar de baja el idioma', `${i.idioma} se quita de su inscripción.`, () => serviciosService.quitarIdioma(alumnoId, i.id)) : undefined}
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
              await ejecutar(() => serviciosService.inscribirIdioma(alumnoId, Number(idiomaSel)));
              setIdiomaSel('');
            }}
          >
            Inscribir
          </Button>
        </Box>
      )}

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
      {editable && (
        <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2, flexWrap: 'wrap', mt: 1 }}>
          <TextField
            label="Observaciones (opcional)"
            value={apoyoObservaciones}
            onChange={(e) => setApoyoObservaciones(e.target.value)}
            size="small"
            sx={{ minWidth: 320 }}
          />
          <Button variant="outlined" onClick={() => ejecutar(() => serviciosService.asignarApoyo(alumnoId, apoyoObservaciones))}>
            {servicios.apoyo ? 'Guardar observaciones' : 'Inscribir'}
          </Button>
          {servicios.apoyo && (
            <Button color="error" onClick={() => pedirBaja('Dar de baja del apoyo estudiantil', 'Se borran las observaciones cargadas.', () => serviciosService.quitarApoyo(alumnoId))}>
              Dar de baja
            </Button>
          )}
        </Box>
      )}

      {editable && (
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
              <Typography variant="body2" sx={{ pl: 1, color: '#666' }}>Sin atenciones registradas</Typography>
            )}
          </List>
        </>
      )}
      <ConfirmDialog
        abierto={!!pendiente}
        titulo={pendiente?.titulo ?? ''}
        mensaje={pendiente?.mensaje ?? ''}
        textoConfirmar="Dar de baja"
        peligro
        cargando={confirmando}
        onConfirmar={confirmarBaja}
        onCancelar={() => setPendiente(null)}
      />
    </Box>
  );
};
