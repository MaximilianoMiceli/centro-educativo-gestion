import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';

export interface Rango {
  desde?: string;
  hasta?: string;
}

export interface PreciosDTO {
  cuotaBase: number | null;
  comedor: number | null;
  deportes: { deporteId: number; deporte: string; monto: number | null }[];
  recorridos: { recorridoId: number; recorrido: string; monto: number | null }[];
}

export interface ActualizarPrecios {
  cuotaBase?: number;
  comedor?: number;
  deportes?: { deporteId: number; monto: number }[];
  recorridos?: { recorridoId: number; monto: number }[];
}

export interface ResultadoFacturacion {
  periodo: string;
  generadas: number;
  omitidas: number;
  totalFacturado: number;
}

export interface IngresosDTO {
  total: number;
  cantidad: number;
  porMes: { mes: string; total: number; cantidad: number }[];
  filas: {
    pagoId: number;
    numeroComprobante: string;
    fechaPago: string;
    alumno: string;
    legajo: string;
    factura: string;
    periodo: string;
    monto: number;
  }[];
}

export interface FilaPagoFactura {
  anio: number;
  alumnoId: number;
  alumno: string;
  legajo: string;
  facturaId: number;
  factura: string;
  periodo: string;
  total: number;
  pagado: number;
  saldo: number;
  ultimoPago: string;
}

export interface PagosPorFacturaDTO {
  totalFacturado: number;
  totalPagado: number;
  totalSaldo: number;
  filas: FilaPagoFactura[];
}

export type EstadoItem = 'PAGADO' | 'PENDIENTE';

export interface FilaPagoDeporte {
  deporte: string;
  nivel: string;
  horario: string;
  profesor: string;
  alumno: string;
  legajo: string;
  curso: string;
  periodo: string;
  factura: string;
  monto: number;
  estado: EstadoItem;
  fechaPago: string | null;
}

export interface PagosPorDeporteDTO {
  totalPagado: number;
  totalPendiente: number;
  filas: FilaPagoDeporte[];
}

export interface FilaPagoRecorrido {
  recorrido: string;
  alumno: string;
  legajo: string;
  nivel: string;
  curso: string;
  periodo: string;
  factura: string;
  monto: number;
  estado: EstadoItem;
  fechaPago: string | null;
}

export interface PagosPorRecorridoDTO {
  totalPagado: number;
  totalPendiente: number;
  filas: FilaPagoRecorrido[];
}

export type TipoCorreo = 'RECORDATORIO' | 'DEUDA';

export interface CorreoDTO {
  id: number;
  tipo: TipoCorreo;
  periodo: string;
  padre: string;
  destinatario: string;
  asunto: string;
  estado: 'SIMULADO' | 'ENVIADO' | 'ERROR';
  detalleError: string | null;
  creadoEn: string;
  cuerpo: string | null;
  adjuntos: { id: number; nombre: string }[];
}

export interface ResultadoCorreos {
  tipo: TipoCorreo;
  fecha: string;
  modo: string;
  facturasGeneradas: number;
  enviados: number;
  conError: number;
  yaEnviados: number;
  sinCorreo: number;
  sinDeuda: number;
}

/** Generar la facturación y preparar correos recorre a todos los alumnos, así que puede tardar. */
const TIEMPO_LARGO = 60000;

class FinanzasService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({ baseURL: API_URL, timeout: 15000 });
  }

  setToken(token: string): void {
    this.api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  async ingresos(rango: Rango): Promise<IngresosDTO> {
    return (await this.api.get<IngresosDTO>('/finanzas/ingresos', { params: rango })).data;
  }

  async pagosCompletos(rango: Rango): Promise<PagosPorFacturaDTO> {
    return (await this.api.get<PagosPorFacturaDTO>('/finanzas/pagos-completos', { params: rango })).data;
  }

  async pagosIncompletos(rango: Rango): Promise<PagosPorFacturaDTO> {
    return (await this.api.get<PagosPorFacturaDTO>('/finanzas/pagos-incompletos', { params: rango })).data;
  }

  async pagosPorDeporte(rango: Rango): Promise<PagosPorDeporteDTO> {
    return (await this.api.get<PagosPorDeporteDTO>('/finanzas/pagos-por-deporte', { params: rango })).data;
  }

  async pagosPorRecorrido(rango: Rango): Promise<PagosPorRecorridoDTO> {
    return (await this.api.get<PagosPorRecorridoDTO>('/finanzas/pagos-por-recorrido', { params: rango })).data;
  }

  async precios(): Promise<PreciosDTO> {
    return (await this.api.get<PreciosDTO>('/precios')).data;
  }

  async guardarPrecios(cambios: ActualizarPrecios): Promise<PreciosDTO> {
    return (await this.api.put<PreciosDTO>('/precios', cambios)).data;
  }

  async generarFacturacion(periodo: string): Promise<ResultadoFacturacion> {
    return (
      await this.api.post<ResultadoFacturacion>('/facturacion/generar', null, {
        params: { periodo },
        timeout: TIEMPO_LARGO,
      })
    ).data;
  }

  async correos(): Promise<CorreoDTO[]> {
    return (await this.api.get<CorreoDTO[]>('/correos')).data;
  }

  async correo(id: number): Promise<CorreoDTO> {
    return (await this.api.get<CorreoDTO>(`/correos/${id}`)).data;
  }

  async prepararCorreos(tipo: TipoCorreo, fecha?: string): Promise<ResultadoCorreos> {
    return (
      await this.api.post<ResultadoCorreos>('/correos/ejecutar', null, {
        params: { tipo, fecha },
        timeout: TIEMPO_LARGO,
      })
    ).data;
  }

  /** Descarga un PDF con la sesión actual y lo abre en otra pestaña. */
  async abrirPdf(ruta: string): Promise<void> {
    const respuesta = await this.api.get<Blob>(ruta, { responseType: 'blob' });
    const url = URL.createObjectURL(respuesta.data);
    window.open(url, '_blank');
    setTimeout(() => URL.revokeObjectURL(url), 60000);
  }
}

export const finanzasService = new FinanzasService();
