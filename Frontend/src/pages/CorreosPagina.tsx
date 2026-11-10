import React, { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
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
import { Email as EmailIcon, PictureAsPdf as PdfIcon } from '@mui/icons-material';
import { EstadoVacio } from '../components/EstadoVacio';
import { PALETA } from '../institucion';
import { finanzasService } from '../services/finanzasService';
import type { CorreoDTO, ResultadoCorreos, TipoCorreo } from '../services/finanzasService';
import { mensajeDeError } from '../utils/errores';
import { fechaCorta } from '../utils/finanzas';

interface CorreosPaginaProps {
  token: string;
}

const ETIQUETA_TIPO: Record<TipoCorreo, string> = {
  RECORDATORIO: 'Recordatorio de cuotas',
  DEUDA: 'Aviso de deuda',
};

const ETIQUETA_ESTADO: Record<CorreoDTO['estado'], string> = {
  SIMULADO: 'Guardado sin enviar',
  ENVIADO: 'Enviado',
  ERROR: 'Con error',
};

const COLOR_ESTADO: Record<CorreoDTO['estado'], 'default' | 'success' | 'error'> = {
  SIMULADO: 'default',
  ENVIADO: 'success',
  ERROR: 'error',
};

function resumen(r: ResultadoCorreos): string {
  const partes = [`${r.enviados} correos preparados`];
  if (r.facturasGeneradas > 0) partes.push(`${r.facturasGeneradas} facturas emitidas`);
  if (r.yaEnviados > 0) partes.push(`${r.yaEnviados} ya estaban enviados`);
  if (r.conError > 0) partes.push(`${r.conError} con error`);
  if (r.sinCorreo > 0) partes.push(`${r.sinCorreo} padres sin correo cargado`);
  if (r.sinDeuda > 0) partes.push(`${r.sinDeuda} sin deuda`);
  return partes.join(' · ');
}

/** Correos mensuales a los padres: se pueden preparar a pedido y ver con su texto y las facturas adjuntas. */
export const CorreosPagina: React.FC<CorreosPaginaProps> = ({ token }) => {
  const [correos, setCorreos] = useState<CorreoDTO[] | null>(null);
  const [error, setError] = useState('');
  const [tipo, setTipo] = useState<TipoCorreo>('RECORDATORIO');
  const [fecha, setFecha] = useState('');
  const [preparando, setPreparando] = useState(false);
  const [resultado, setResultado] = useState<ResultadoCorreos | null>(null);
  const [errorPreparar, setErrorPreparar] = useState('');
  const [abierto, setAbierto] = useState<CorreoDTO | null>(null);

  const cargar = useCallback(() => {
    finanzasService.correos().then(setCorreos).catch(() => setError('No se pudieron cargar los correos'));
  }, []);

  useEffect(() => {
    finanzasService.setToken(token);
    cargar();
  }, [token, cargar]);

  const preparar = async () => {
    setPreparando(true);
    setErrorPreparar('');
    setResultado(null);
    try {
      setResultado(await finanzasService.prepararCorreos(tipo, fecha || undefined));
      cargar();
    } catch (e) {
      setErrorPreparar(mensajeDeError(e, 'No se pudieron preparar los correos'));
    } finally {
      setPreparando(false);
    }
  };

  const verCorreo = async (id: number) => {
    try {
      setAbierto(await finanzasService.correo(id));
    } catch {
      setError('No se pudo abrir el correo');
    }
  };

  return (
    <Box>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 0.5 }}>
        <EmailIcon sx={{ color: PALETA.azul }} />
        <Typography variant="h5" sx={{ fontWeight: 700 }}>Correos a los padres</Typography>
      </Stack>
      <Typography variant="body2" sx={{ color: '#666', mb: 2 }}>
        El sistema los prepara solo: el recordatorio de cuotas el último día hábil de cada mes y el aviso de deuda el día 20.
        Por ahora quedan guardados sin enviarse, y desde acá se pueden ver con las facturas adjuntas.
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      <Card sx={{ mb: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5 }}>Preparar correos ahora</Typography>
          <Typography variant="body2" sx={{ color: '#666', mb: 2 }}>
            Hace lo mismo que la tarea diaria. Con una fecha se simula otro día, por ejemplo el último día del mes. No repite
            los correos que ya se prepararon ese mes.
          </Typography>
          {errorPreparar && <Alert severity="error" sx={{ mb: 2 }}>{errorPreparar}</Alert>}
          {resultado && <Alert severity="success" sx={{ mb: 2 }}>{resumen(resultado)}</Alert>}
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: { sm: 'flex-start' } }}>
            <TextField select label="Tipo" size="small" value={tipo} onChange={(e) => setTipo(e.target.value as TipoCorreo)} sx={{ minWidth: 220 }}>
              <MenuItem value="RECORDATORIO">{ETIQUETA_TIPO.RECORDATORIO}</MenuItem>
              <MenuItem value="DEUDA">{ETIQUETA_TIPO.DEUDA}</MenuItem>
            </TextField>
            <TextField
              label="Fecha (opcional)"
              type="date"
              size="small"
              value={fecha}
              onChange={(e) => setFecha(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <Button variant="contained" onClick={preparar} disabled={preparando}>
              {preparando ? 'Preparando…' : 'Preparar correos'}
            </Button>
          </Stack>
        </CardContent>
      </Card>

      <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 1 }}>Correos preparados</Typography>
          {correos === null && !error ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}><CircularProgress /></Box>
          ) : correos && correos.length === 0 ? (
            <EstadoVacio titulo="Todavía no se prepararon correos" descripcion="Usá el botón de arriba para preparar el primero." />
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ '& th': { fontWeight: 700, backgroundColor: '#F5F6FA' } }}>
                    <TableCell>Fecha</TableCell>
                    <TableCell>Tipo</TableCell>
                    <TableCell>Padre</TableCell>
                    <TableCell>Asunto</TableCell>
                    <TableCell>Estado</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(correos ?? []).map((c) => (
                    <TableRow key={c.id} hover onClick={() => verCorreo(c.id)} sx={{ cursor: 'pointer' }}>
                      <TableCell>{fechaCorta(c.creadoEn)}</TableCell>
                      <TableCell>{ETIQUETA_TIPO[c.tipo]}</TableCell>
                      <TableCell>{c.padre}<br /><Typography component="span" variant="caption" sx={{ color: '#666' }}>{c.destinatario}</Typography></TableCell>
                      <TableCell>{c.asunto}</TableCell>
                      <TableCell><Chip size="small" color={COLOR_ESTADO[c.estado]} label={ETIQUETA_ESTADO[c.estado]} /></TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>

      <Dialog open={abierto !== null} onClose={() => setAbierto(null)} fullWidth maxWidth="sm">
        {abierto && (
          <>
            <DialogTitle sx={{ fontWeight: 700 }}>{abierto.asunto}</DialogTitle>
            <DialogContent>
              <Typography variant="body2" sx={{ color: '#666', mb: 1 }}>Para: {abierto.destinatario}</Typography>
              {abierto.detalleError && <Alert severity="error" sx={{ mb: 1 }}>{abierto.detalleError}</Alert>}
              <Typography component="pre" variant="body2" sx={{ whiteSpace: 'pre-wrap', fontFamily: 'inherit', m: 0 }}>
                {abierto.cuerpo}
              </Typography>
              {abierto.adjuntos.length > 0 && (
                <Stack spacing={1} sx={{ mt: 2 }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>Adjuntos</Typography>
                  {abierto.adjuntos.map((a) => (
                    <Button
                      key={a.id}
                      variant="outlined"
                      size="small"
                      startIcon={<PdfIcon />}
                      sx={{ alignSelf: 'flex-start' }}
                      onClick={() => finanzasService.abrirPdf(`/correos/${abierto.id}/adjuntos/${a.id}`).catch(() => setError('No se pudo abrir el adjunto'))}
                    >
                      {a.nombre}
                    </Button>
                  ))}
                </Stack>
              )}
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setAbierto(null)}>Cerrar</Button>
            </DialogActions>
          </>
        )}
      </Dialog>
    </Box>
  );
};
