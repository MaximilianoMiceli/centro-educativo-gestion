import React, { useEffect, useState } from 'react';
import { Alert, Box, Button, Card, CardContent, Divider, Stack, TextField, Typography } from '@mui/material';
import { Sell as SellIcon } from '@mui/icons-material';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { PALETA } from '../institucion';
import { finanzasService } from '../services/finanzasService';
import type { PreciosDTO, ResultadoFacturacion } from '../services/finanzasService';
import { mensajeDeError } from '../utils/errores';
import { mesLegible, moneda, periodoSiguiente } from '../utils/finanzas';

interface PreciosPaginaProps {
  token: string;
}

type Valores = Record<string, string>;

function valoresIniciales(precios: PreciosDTO): Valores {
  const valores: Valores = {
    cuota: precios.cuotaBase === null ? '' : String(precios.cuotaBase),
    comedor: precios.comedor === null ? '' : String(precios.comedor),
  };
  precios.deportes.forEach((d) => (valores[`d${d.deporteId}`] = d.monto === null ? '' : String(d.monto)));
  precios.recorridos.forEach((r) => (valores[`r${r.recorridoId}`] = r.monto === null ? '' : String(r.monto)));
  return valores;
}

/** Un importe válido es un número mayor o igual a cero con hasta dos decimales. */
function errorDeImporte(texto: string): string | undefined {
  if (texto.trim() === '') return 'Falta el valor';
  const numero = Number(texto);
  if (Number.isNaN(numero) || numero < 0) return 'Ingresá un importe mayor o igual a cero';
  if (!/^\d+(\.\d{1,2})?$/.test(texto.trim())) return 'Usá hasta dos decimales';
  return undefined;
}

/** El administrador fija los valores de la cuota y los servicios, y emite la facturación del mes. */
export const PreciosPagina: React.FC<PreciosPaginaProps> = ({ token }) => {
  const [precios, setPrecios] = useState<PreciosDTO | null>(null);
  const [valores, setValores] = useState<Valores>({});
  const [error, setError] = useState('');
  const [aviso, setAviso] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [periodo, setPeriodo] = useState(periodoSiguiente());
  const [confirmar, setConfirmar] = useState(false);
  const [generando, setGenerando] = useState(false);
  const [resultado, setResultado] = useState<ResultadoFacturacion | null>(null);
  const [errorFacturacion, setErrorFacturacion] = useState('');

  useEffect(() => {
    finanzasService.setToken(token);
    finanzasService
      .precios()
      .then((p) => {
        setPrecios(p);
        setValores(valoresIniciales(p));
      })
      .catch(() => setError('No se pudieron cargar los valores'));
  }, [token]);

  const errores: Valores = {};
  Object.entries(valores).forEach(([clave, texto]) => {
    const e = errorDeImporte(texto);
    if (e) errores[clave] = e;
  });
  const hayErrores = Object.keys(errores).length > 0;

  const cambiar = (clave: string, texto: string) => {
    setAviso('');
    setValores((previos) => ({ ...previos, [clave]: texto }));
  };

  const guardar = async () => {
    if (!precios) return;
    setGuardando(true);
    setError('');
    try {
      const actualizados = await finanzasService.guardarPrecios({
        cuotaBase: Number(valores.cuota),
        comedor: Number(valores.comedor),
        deportes: precios.deportes.map((d) => ({ deporteId: d.deporteId, monto: Number(valores[`d${d.deporteId}`]) })),
        recorridos: precios.recorridos.map((r) => ({ recorridoId: r.recorridoId, monto: Number(valores[`r${r.recorridoId}`]) })),
      });
      setPrecios(actualizados);
      setValores(valoresIniciales(actualizados));
      setAviso('Valores guardados. Se aplican a las facturas que se emitan desde ahora.');
    } catch (e) {
      setError(mensajeDeError(e, 'No se pudieron guardar los valores'));
    } finally {
      setGuardando(false);
    }
  };

  const generar = async () => {
    setGenerando(true);
    setErrorFacturacion('');
    setResultado(null);
    try {
      setResultado(await finanzasService.generarFacturacion(periodo));
    } catch (e) {
      setErrorFacturacion(mensajeDeError(e, 'No se pudo generar la facturación'));
    } finally {
      setGenerando(false);
      setConfirmar(false);
    }
  };

  const campo = (clave: string, etiqueta: string) => (
    <TextField
      key={clave}
      label={etiqueta}
      size="small"
      type="number"
      value={valores[clave] ?? ''}
      onChange={(e) => cambiar(clave, e.target.value)}
      error={!!errores[clave]}
      helperText={errores[clave]}
      slotProps={{ htmlInput: { min: 0, step: 0.01 }, input: { startAdornment: <Typography sx={{ mr: 0.5 }}>$</Typography> } }}
      fullWidth
    />
  );

  return (
    <Box>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 0.5 }}>
        <SellIcon sx={{ color: PALETA.azul }} />
        <Typography variant="h5" sx={{ fontWeight: 700 }}>Precios y facturación</Typography>
      </Stack>
      <Typography variant="body2" sx={{ color: '#666', mb: 2 }}>
        Los valores se copian a cada factura cuando se emite: un cambio de precio no modifica facturas anteriores.
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {aviso && <Alert severity="success" sx={{ mb: 2 }}>{aviso}</Alert>}

      <Card sx={{ mb: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 1.5 }}>Valores mensuales</Typography>
          {!precios ? (
            <Typography variant="body2" sx={{ color: '#666' }}>Cargando…</Typography>
          ) : (
            <Stack spacing={3}>
              <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
                {campo('cuota', 'Cuota base')}
                {campo('comedor', 'Comedor')}
              </Box>
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 1 }}>Deportes</Typography>
                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: '1fr 1fr 1fr' }, gap: 2 }}>
                  {precios.deportes.map((d) => campo(`d${d.deporteId}`, d.deporte))}
                </Box>
              </Box>
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 1 }}>Transporte, por recorrido</Typography>
                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
                  {precios.recorridos.map((r) => campo(`r${r.recorridoId}`, r.recorrido))}
                </Box>
              </Box>
              <Box>
                <Button variant="contained" onClick={guardar} disabled={hayErrores || guardando}>
                  {guardando ? 'Guardando…' : 'Guardar valores'}
                </Button>
              </Box>
            </Stack>
          )}
        </CardContent>
      </Card>

      <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>Emitir la facturación de un mes</Typography>
          <Typography variant="body2" sx={{ color: '#666', mb: 2 }}>
            Se crea una factura por alumno activo, con la cuota base y los servicios en los que está inscripto. Es seguro
            repetirlo: los alumnos que ya tienen factura ese mes se omiten. Vence el día 20.
          </Typography>
          <Divider sx={{ mb: 2 }} />
          {errorFacturacion && <Alert severity="error" sx={{ mb: 2 }}>{errorFacturacion}</Alert>}
          {resultado && (
            <Alert severity="success" sx={{ mb: 2 }}>
              Facturación de {mesLegible(resultado.periodo)}: {resultado.generadas} facturas generadas por{' '}
              {moneda(resultado.totalFacturado)}
              {resultado.omitidas > 0 && `; ${resultado.omitidas} ya existían y se omitieron`}.
            </Alert>
          )}
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: { sm: 'flex-start' } }}>
            <TextField
              label="Mes a facturar"
              type="month"
              size="small"
              value={periodo}
              onChange={(e) => setPeriodo(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <Button variant="contained" disabled={!periodo} onClick={() => setConfirmar(true)}>
              Generar facturación
            </Button>
          </Stack>
        </CardContent>
      </Card>

      <ConfirmDialog
        abierto={confirmar}
        titulo="Generar la facturación"
        mensaje={`Se van a emitir las facturas de ${periodo ? mesLegible(periodo) : ''} para todos los alumnos activos que todavía no tengan una.`}
        textoConfirmar="Generar"
        cargando={generando}
        onConfirmar={generar}
        onCancelar={() => setConfirmar(false)}
      />
    </Box>
  );
};
