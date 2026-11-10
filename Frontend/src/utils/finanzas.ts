import type { EstadoItem, FilaPagoFactura } from '../services/finanzasService';

const FORMATO_MONEDA = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' });

export function moneda(valor: number): string {
  return FORMATO_MONEDA.format(valor);
}

/** De AAAA-MM-DD a DD/MM/AAAA. */
export function fechaCorta(iso: string | null): string {
  if (!iso) return '—';
  const [anio, mes, dia] = iso.slice(0, 10).split('-');
  return `${dia}/${mes}/${anio}`;
}

const MESES = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

/** De AAAA-MM a "Diciembre 2026". */
export function mesLegible(periodo: string): string {
  const [anio, mes] = periodo.split('-');
  return `${MESES[Number(mes) - 1] ?? mes} ${anio}`;
}

/** El mes siguiente al de la fecha, con formato AAAA-MM: es el que se factura a fin de mes. */
export function periodoSiguiente(hoy: Date = new Date()): string {
  const siguiente = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 1);
  return `${siguiente.getFullYear()}-${String(siguiente.getMonth() + 1).padStart(2, '0')}`;
}

export interface GrupoAlumno {
  alumnoId: number;
  alumno: string;
  legajo: string;
  filas: FilaPagoFactura[];
  total: number;
  pagado: number;
  saldo: number;
}

export interface GrupoAnio {
  anio: number;
  alumnos: GrupoAlumno[];
  total: number;
  pagado: number;
  saldo: number;
}

/** Agrupa las facturas por año y, dentro de cada año, por alumno, con los subtotales de cada nivel. */
export function agruparPorAnioYAlumno(filas: FilaPagoFactura[]): GrupoAnio[] {
  const anios = new Map<number, Map<number, GrupoAlumno>>();
  for (const fila of filas) {
    const delAnio = anios.get(fila.anio) ?? new Map<number, GrupoAlumno>();
    const grupo =
      delAnio.get(fila.alumnoId) ??
      { alumnoId: fila.alumnoId, alumno: fila.alumno, legajo: fila.legajo, filas: [], total: 0, pagado: 0, saldo: 0 };
    grupo.filas.push(fila);
    grupo.total += fila.total;
    grupo.pagado += fila.pagado;
    grupo.saldo += fila.saldo;
    delAnio.set(fila.alumnoId, grupo);
    anios.set(fila.anio, delAnio);
  }
  return [...anios.entries()]
    .sort(([a], [b]) => a - b)
    .map(([anio, alumnos]) => {
      const lista = [...alumnos.values()].sort((a, b) => a.alumno.localeCompare(b.alumno, 'es'));
      return {
        anio,
        alumnos: lista,
        total: lista.reduce((suma, g) => suma + g.total, 0),
        pagado: lista.reduce((suma, g) => suma + g.pagado, 0),
        saldo: lista.reduce((suma, g) => suma + g.saldo, 0),
      };
    });
}

export type FiltroEstado = 'TODOS' | EstadoItem;

export function filtrarPorEstado<T extends { estado: EstadoItem }>(filas: T[], filtro: FiltroEstado): T[] {
  return filtro === 'TODOS' ? filas : filas.filter((f) => f.estado === filtro);
}
