import React, { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Stack,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material';
import { Payments as PaymentsIcon } from '@mui/icons-material';
import { EstadoVacio } from '../components/EstadoVacio';
import { PALETA } from '../institucion';
import { finanzasService } from '../services/finanzasService';
import type {
  IngresosDTO,
  PagosPorDeporteDTO,
  PagosPorFacturaDTO,
  PagosPorRecorridoDTO,
  Rango,
} from '../services/finanzasService';
import {
  agruparPorAnioYAlumno,
  fechaCorta,
  filtrarPorEstado,
  mesLegible,
  moneda,
  type FiltroEstado,
} from '../utils/finanzas';

interface FinanzasPaginaProps {
  token: string;
}

type Pestania = 'ingresos' | 'completos' | 'incompletos' | 'deporte' | 'recorrido';

const PESTANIAS: { valor: Pestania; etiqueta: string; ayuda: string }[] = [
  { valor: 'ingresos', etiqueta: 'Ingresos', ayuda: 'Pagos recibidos en el período, por fecha de la transferencia.' },
  {
    valor: 'completos',
    etiqueta: 'Pagos completos',
    ayuda: 'Facturas pagadas por completo cuyo último pago cae en el período, por año y por alumno.',
  },
  {
    valor: 'incompletos',
    etiqueta: 'Pagos incompletos',
    ayuda: 'Facturas con pagos parciales y saldo pendiente cuyo último pago cae en el período, por año y por alumno.',
  },
  {
    valor: 'deporte',
    etiqueta: 'Por deporte',
    ayuda: 'Ítems de deporte de las facturas emitidas en el período, con nivel, horario y profesor a cargo.',
  },
  {
    valor: 'recorrido',
    etiqueta: 'Por recorrido',
    ayuda: 'Ítems de transporte de las facturas emitidas en el período, por recorrido.',
  },
];

function useListado<T>(cargar: (rango: Rango) => Promise<T>, rango: Rango) {
  const [datos, setDatos] = useState<T | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let vigente = true;
    setDatos(null);
    setError('');
    cargar(rango)
      .then((resultado) => vigente && setDatos(resultado))
      .catch(() => vigente && setError('No se pudo cargar el listado'));
    return () => {
      vigente = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rango.desde, rango.hasta]);

  return { datos, error };
}

const Total: React.FC<{ etiqueta: string; valor: string; color?: string }> = ({ etiqueta, valor, color }) => (
  <Card variant="outlined" sx={{ flex: 1, minWidth: 150 }}>
    <CardContent sx={{ py: 1.5, '&:last-child': { pb: 1.5 } }}>
      <Typography variant="caption" sx={{ color: '#666' }}>{etiqueta}</Typography>
      <Typography variant="h6" sx={{ fontWeight: 700, color: color ?? PALETA.azulOscuro }}>{valor}</Typography>
    </CardContent>
  </Card>
);

const Cargando: React.FC = () => (
  <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
    <CircularProgress />
  </Box>
);

const CABECERA = { '& th': { fontWeight: 700, backgroundColor: '#F5F6FA' } };

const IngresosVista: React.FC<{ rango: Rango }> = ({ rango }) => {
  const { datos, error } = useListado<IngresosDTO>((r) => finanzasService.ingresos(r), rango);
  if (error) return <Alert severity="error">{error}</Alert>;
  if (!datos) return <Cargando />;
  if (datos.cantidad === 0) {
    return <EstadoVacio titulo="No hay ingresos en este período" descripcion="Probá con otro rango de fechas." />;
  }
  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <Total etiqueta="Total ingresado" valor={moneda(datos.total)} />
        <Total etiqueta="Pagos registrados" valor={String(datos.cantidad)} />
      </Stack>
      <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
        {datos.porMes.map((m) => (
          <Chip key={m.mes} label={`${mesLegible(m.mes)}: ${moneda(m.total)} (${m.cantidad})`} variant="outlined" />
        ))}
      </Stack>
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow sx={CABECERA}>
              <TableCell>Fecha</TableCell>
              <TableCell>Comprobante</TableCell>
              <TableCell>Alumno</TableCell>
              <TableCell>Factura</TableCell>
              <TableCell align="right">Importe</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {datos.filas.map((f) => (
              <TableRow key={f.pagoId} hover>
                <TableCell>{fechaCorta(f.fechaPago)}</TableCell>
                <TableCell>{f.numeroComprobante}</TableCell>
                <TableCell>{f.alumno} <Typography component="span" variant="caption" sx={{ color: '#666' }}>({f.legajo})</Typography></TableCell>
                <TableCell>{f.factura} · {mesLegible(f.periodo)}</TableCell>
                <TableCell align="right">{moneda(f.monto)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Stack>
  );
};

const PagosFacturaVista: React.FC<{ rango: Rango; completos: boolean }> = ({ rango, completos }) => {
  const { datos, error } = useListado<PagosPorFacturaDTO>(
    (r) => (completos ? finanzasService.pagosCompletos(r) : finanzasService.pagosIncompletos(r)),
    rango,
  );
  if (error) return <Alert severity="error">{error}</Alert>;
  if (!datos) return <Cargando />;
  if (datos.filas.length === 0) {
    return (
      <EstadoVacio
        titulo={completos ? 'No hay facturas pagadas por completo en este período' : 'No hay facturas con pagos parciales en este período'}
        descripcion="Probá con otro rango de fechas."
      />
    );
  }
  const anios = agruparPorAnioYAlumno(datos.filas);
  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <Total etiqueta="Facturado" valor={moneda(datos.totalFacturado)} />
        <Total etiqueta="Pagado" valor={moneda(datos.totalPagado)} color={PALETA.verde} />
        {!completos && <Total etiqueta="Saldo pendiente" valor={moneda(datos.totalSaldo)} color={PALETA.naranja} />}
      </Stack>
      {anios.map((anio) => (
        <Box key={anio.anio}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: PALETA.azul }}>
            Año {anio.anio} · {moneda(anio.pagado)} pagado{!completos && ` · ${moneda(anio.saldo)} de saldo`}
          </Typography>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow sx={CABECERA}>
                  <TableCell>Alumno</TableCell>
                  <TableCell>Factura</TableCell>
                  <TableCell>Último pago</TableCell>
                  <TableCell align="right">Total</TableCell>
                  <TableCell align="right">Pagado</TableCell>
                  {!completos && <TableCell align="right">Saldo</TableCell>}
                </TableRow>
              </TableHead>
              <TableBody>
                {anio.alumnos.map((alumno) => (
                  <React.Fragment key={alumno.alumnoId}>
                    <TableRow sx={{ backgroundColor: PALETA.azulSuave }}>
                      <TableCell colSpan={3} sx={{ fontWeight: 700 }}>{alumno.alumno} ({alumno.legajo})</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 700 }}>{moneda(alumno.total)}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 700 }}>{moneda(alumno.pagado)}</TableCell>
                      {!completos && <TableCell align="right" sx={{ fontWeight: 700 }}>{moneda(alumno.saldo)}</TableCell>}
                    </TableRow>
                    {alumno.filas.map((f) => (
                      <TableRow key={f.facturaId} hover>
                        <TableCell sx={{ pl: 4 }}>{mesLegible(f.periodo)}</TableCell>
                        <TableCell>{f.factura}</TableCell>
                        <TableCell>{fechaCorta(f.ultimoPago)}</TableCell>
                        <TableCell align="right">{moneda(f.total)}</TableCell>
                        <TableCell align="right">{moneda(f.pagado)}</TableCell>
                        {!completos && <TableCell align="right">{moneda(f.saldo)}</TableCell>}
                      </TableRow>
                    ))}
                  </React.Fragment>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      ))}
    </Stack>
  );
};

const FiltroItems: React.FC<{ valor: FiltroEstado; onCambio: (valor: FiltroEstado) => void }> = ({ valor, onCambio }) => (
  <ToggleButtonGroup
    size="small"
    exclusive
    value={valor}
    onChange={(_e, nuevo: FiltroEstado | null) => nuevo && onCambio(nuevo)}
    aria-label="Estado del pago"
  >
    <ToggleButton value="TODOS">Todos</ToggleButton>
    <ToggleButton value="PAGADO">Pagados</ToggleButton>
    <ToggleButton value="PENDIENTE">Pendientes</ToggleButton>
  </ToggleButtonGroup>
);

const EstadoPago: React.FC<{ estado: 'PAGADO' | 'PENDIENTE'; fecha: string | null }> = ({ estado, fecha }) => (
  <Chip
    size="small"
    label={estado === 'PAGADO' ? `Pagado ${fechaCorta(fecha)}` : 'Pendiente'}
    sx={{
      fontWeight: 600,
      color: estado === 'PAGADO' ? '#2E6B00' : '#B3361A',
      backgroundColor: estado === 'PAGADO' ? '#EAF5DA' : '#FDE8E2',
    }}
  />
);

const DeporteVista: React.FC<{ rango: Rango }> = ({ rango }) => {
  const { datos, error } = useListado<PagosPorDeporteDTO>((r) => finanzasService.pagosPorDeporte(r), rango);
  const [filtro, setFiltro] = useState<FiltroEstado>('TODOS');
  if (error) return <Alert severity="error">{error}</Alert>;
  if (!datos) return <Cargando />;
  if (datos.filas.length === 0) {
    return <EstadoVacio titulo="No hay ítems de deporte en este período" descripcion="Probá con otro rango de fechas." />;
  }
  const filas = filtrarPorEstado(datos.filas, filtro);
  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <Total etiqueta="Pagado" valor={moneda(datos.totalPagado)} color={PALETA.verde} />
        <Total etiqueta="Pendiente" valor={moneda(datos.totalPendiente)} color={PALETA.naranja} />
      </Stack>
      <FiltroItems valor={filtro} onCambio={setFiltro} />
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow sx={CABECERA}>
              <TableCell>Deporte</TableCell>
              <TableCell>Nivel</TableCell>
              <TableCell>Horario</TableCell>
              <TableCell>Profesor a cargo</TableCell>
              <TableCell>Alumno</TableCell>
              <TableCell>Período</TableCell>
              <TableCell align="right">Importe</TableCell>
              <TableCell>Estado</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filas.map((f, i) => (
              <TableRow key={`${f.legajo}-${f.periodo}-${f.deporte}-${i}`} hover>
                <TableCell>{f.deporte}</TableCell>
                <TableCell>{f.nivel}</TableCell>
                <TableCell>{f.horario}</TableCell>
                <TableCell>{f.profesor}</TableCell>
                <TableCell>{f.alumno} <Typography component="span" variant="caption" sx={{ color: '#666' }}>({f.legajo})</Typography></TableCell>
                <TableCell>{mesLegible(f.periodo)}</TableCell>
                <TableCell align="right">{moneda(f.monto)}</TableCell>
                <TableCell><EstadoPago estado={f.estado} fecha={f.fechaPago} /></TableCell>
              </TableRow>
            ))}
            {filas.length === 0 && (
              <TableRow>
                <TableCell colSpan={8}><EstadoVacio titulo="Ningún ítem coincide con este estado" /></TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Stack>
  );
};

const RecorridoVista: React.FC<{ rango: Rango }> = ({ rango }) => {
  const { datos, error } = useListado<PagosPorRecorridoDTO>((r) => finanzasService.pagosPorRecorrido(r), rango);
  const [filtro, setFiltro] = useState<FiltroEstado>('TODOS');
  if (error) return <Alert severity="error">{error}</Alert>;
  if (!datos) return <Cargando />;
  if (datos.filas.length === 0) {
    return <EstadoVacio titulo="No hay ítems de transporte en este período" descripcion="Probá con otro rango de fechas." />;
  }
  const filas = filtrarPorEstado(datos.filas, filtro);
  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <Total etiqueta="Pagado" valor={moneda(datos.totalPagado)} color={PALETA.verde} />
        <Total etiqueta="Pendiente" valor={moneda(datos.totalPendiente)} color={PALETA.naranja} />
      </Stack>
      <FiltroItems valor={filtro} onCambio={setFiltro} />
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow sx={CABECERA}>
              <TableCell>Recorrido</TableCell>
              <TableCell>Alumno</TableCell>
              <TableCell>Nivel y curso</TableCell>
              <TableCell>Período</TableCell>
              <TableCell align="right">Importe</TableCell>
              <TableCell>Estado</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filas.map((f, i) => (
              <TableRow key={`${f.legajo}-${f.periodo}-${i}`} hover>
                <TableCell>{f.recorrido}</TableCell>
                <TableCell>{f.alumno} <Typography component="span" variant="caption" sx={{ color: '#666' }}>({f.legajo})</Typography></TableCell>
                <TableCell>{f.curso}</TableCell>
                <TableCell>{mesLegible(f.periodo)}</TableCell>
                <TableCell align="right">{moneda(f.monto)}</TableCell>
                <TableCell><EstadoPago estado={f.estado} fecha={f.fechaPago} /></TableCell>
              </TableRow>
            ))}
            {filas.length === 0 && (
              <TableRow>
                <TableCell colSpan={6}><EstadoVacio titulo="Ningún ítem coincide con este estado" /></TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Stack>
  );
};

/** Listados de ingresos y pagos para la administración y la dirección. */
export const FinanzasPagina: React.FC<FinanzasPaginaProps> = ({ token }) => {
  const [pestania, setPestania] = useState<Pestania>('ingresos');
  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');
  const [rango, setRango] = useState<Rango>({});
  const invalido = desde !== '' && hasta !== '' && desde > hasta;

  useEffect(() => {
    finanzasService.setToken(token);
  }, [token]);

  useEffect(() => {
    if (!invalido) {
      setRango({ desde: desde || undefined, hasta: hasta || undefined });
    }
  }, [desde, hasta, invalido]);

  const actual = PESTANIAS.find((p) => p.valor === pestania)!;
  return (
    <Box>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 0.5 }}>
        <PaymentsIcon sx={{ color: PALETA.azul }} />
        <Typography variant="h5" sx={{ fontWeight: 700 }}>Finanzas</Typography>
      </Stack>
      <Typography variant="body2" sx={{ color: '#666', mb: 2 }}>
        Ingresos y pagos de las cuotas. Dejá las fechas vacías para ver todo el historial.
      </Typography>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField
          label="Desde"
          type="date"
          size="small"
          value={desde}
          onChange={(e) => setDesde(e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
          error={invalido}
        />
        <TextField
          label="Hasta"
          type="date"
          size="small"
          value={hasta}
          onChange={(e) => setHasta(e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
          error={invalido}
          helperText={invalido ? 'La fecha hasta no puede ser anterior a la fecha desde' : undefined}
        />
      </Stack>

      <Tabs value={pestania} onChange={(_e, valor: Pestania) => setPestania(valor)} variant="scrollable" scrollButtons="auto" sx={{ mb: 1 }}>
        {PESTANIAS.map((p) => (
          <Tab key={p.valor} value={p.valor} label={p.etiqueta} />
        ))}
      </Tabs>
      <Typography variant="body2" sx={{ color: '#666', mb: 2 }}>{actual.ayuda}</Typography>

      <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          {pestania === 'ingresos' && <IngresosVista rango={rango} />}
          {pestania === 'completos' && <PagosFacturaVista rango={rango} completos />}
          {pestania === 'incompletos' && <PagosFacturaVista rango={rango} completos={false} />}
          {pestania === 'deporte' && <DeporteVista rango={rango} />}
          {pestania === 'recorrido' && <RecorridoVista rango={rango} />}
        </CardContent>
      </Card>
    </Box>
  );
};
