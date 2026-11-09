import type { Factura, ItemFactura } from '../api/tipos';

export type FiltroEstado = 'PENDIENTES' | 'PAGADAS' | 'TODAS';

export function filtrarPorEstado(facturas: Factura[], filtro: FiltroEstado): Factura[] {
  if (filtro === 'TODAS') {
    return facturas;
  }
  return facturas.filter((f) => (filtro === 'PAGADAS' ? f.estado === 'PAGADA' : f.estado !== 'PAGADA'));
}

export function itemsPendientes(factura: Factura): ItemFactura[] {
  return factura.items.filter((i) => !i.pagado);
}

/** Suma de los ítems elegidos, en centavos para no arrastrar errores de decimales. */
export function totalElegido(factura: Factura, elegidos: ReadonlySet<number>): number {
  const centavos = factura.items
    .filter((i) => elegidos.has(i.id) && !i.pagado)
    .reduce((suma, i) => suma + Math.round(i.monto * 100), 0);
  return centavos / 100;
}

export function alternar(elegidos: ReadonlySet<number>, id: number): Set<number> {
  const nuevo = new Set(elegidos);
  if (nuevo.has(id)) {
    nuevo.delete(id);
  } else {
    nuevo.add(id);
  }
  return nuevo;
}

/** Fecha de hoy como AAAA-MM-DD, con los datos del reloj del dispositivo. */
export function hoyISO(ahora: Date = new Date()): string {
  const mes = String(ahora.getMonth() + 1).padStart(2, '0');
  const dia = String(ahora.getDate()).padStart(2, '0');
  return `${ahora.getFullYear()}-${mes}-${dia}`;
}

/** Acepta AAAA-MM-DD y verifica que el día exista (por ejemplo, rechaza 2026-02-30). */
export function esFechaValida(texto: string): boolean {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(texto)) {
    return false;
  }
  const [anio, mes, dia] = texto.split('-').map(Number);
  const fecha = new Date(anio, mes - 1, dia);
  return fecha.getFullYear() === anio && fecha.getMonth() === mes - 1 && fecha.getDate() === dia;
}
