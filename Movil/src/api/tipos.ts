export interface Sesion {
  token: string;
  username: string;
  rol: string;
  debeCambiarPassword: boolean;
}

export interface Hijo {
  id: number;
  legajo: string;
  alumno: string;
  curso: string;
  nivel: string;
}

export type EstadoFactura = 'PENDIENTE' | 'PARCIAL' | 'PAGADA';

export interface ItemFactura {
  id: number;
  concepto: string;
  descripcion: string;
  monto: number;
  pagado: boolean;
  pagoId: number | null;
}

export interface PagoResumen {
  id: number;
  numeroComprobante: string;
  monto: number;
  fechaPago: string;
  registradoEn: string;
  nombreArchivo: string;
  itemIds: number[];
}

export interface Factura {
  id: number;
  numero: string;
  alumnoId: number;
  alumno: string;
  periodo: string;
  emision: string;
  vencimiento: string;
  total: number;
  pagado: number;
  saldo: number;
  estado: EstadoFactura;
  vencida: boolean;
  items: ItemFactura[];
  pagos: PagoResumen[];
}

export type EstadoItemDeuda = 'PAGADO' | 'PENDIENTE' | 'VENCIDO';

export interface ItemDeuda {
  facturaId: number;
  numeroFactura: string;
  periodo: string;
  vencimiento: string;
  concepto: string;
  descripcion: string;
  monto: number;
  estado: EstadoItemDeuda;
}

export interface Deuda {
  alumnoId: number;
  alumno: string;
  totalFacturado: number;
  totalPagado: number;
  totalAdeudado: number;
  items: ItemDeuda[];
}

export interface ServiciosAlumno {
  alumnoId: number;
  alumno: string;
  nivel: string;
  deportes: { id: number; deporteId: number; deporte: string; horario: string }[];
  transporte: { id: number; nombre: string } | null;
  comedor: boolean;
  idiomas: { id: number; idioma: string }[];
}

export interface MiInformacion {
  servicios: ServiciosAlumno;
}

export type ArchivoAdjunto =
  | { uri: string; name: string; type: string }
  | { blob: Blob; name: string; type: string };
