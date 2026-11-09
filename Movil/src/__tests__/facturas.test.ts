import type { Factura, ItemFactura } from '../api/tipos';
import { alternar, esFechaValida, filtrarPorEstado, hoyISO, itemsPendientes, totalElegido } from '../utils/facturas';

function item(id: number, monto: number, pagado = false): ItemFactura {
  return { id, concepto: 'DEPORTE', descripcion: `Ítem ${id}`, monto, pagado, pagoId: pagado ? 1 : null };
}

function factura(id: number, estado: Factura['estado'], items: ItemFactura[] = []): Factura {
  return {
    id, numero: `F-${id}`, alumnoId: 1, alumno: 'García, Carlos', periodo: '2026-12', emision: '2026-11-30',
    vencimiento: '2026-12-20', total: 0, pagado: 0, saldo: 0, estado, vencida: false, items, pagos: [],
  };
}

describe('filtrarPorEstado', () => {
  const todas = [factura(1, 'PENDIENTE'), factura(2, 'PARCIAL'), factura(3, 'PAGADA')];

  it('las pendientes incluyen las de pago parcial', () => {
    expect(filtrarPorEstado(todas, 'PENDIENTES').map((f) => f.id)).toEqual([1, 2]);
  });

  it('las pagadas son solo las completas', () => {
    expect(filtrarPorEstado(todas, 'PAGADAS').map((f) => f.id)).toEqual([3]);
  });

  it('todas devuelve todo', () => {
    expect(filtrarPorEstado(todas, 'TODAS')).toHaveLength(3);
  });
});

describe('selección de ítems a pagar', () => {
  const conItems = factura(1, 'PARCIAL', [item(10, 150000), item(11, 15000, true), item(12, 28000.1)]);

  it('solo ofrece los ítems que faltan pagar', () => {
    expect(itemsPendientes(conItems).map((i) => i.id)).toEqual([10, 12]);
  });

  it('suma los elegidos sin errores de decimales', () => {
    expect(totalElegido(conItems, new Set([10, 12]))).toBe(178000.1);
    expect(totalElegido(factura(2, 'PENDIENTE', [item(1, 0.1), item(2, 0.2)]), new Set([1, 2]))).toBe(0.3);
  });

  it('ignora un ítem ya pagado aunque esté elegido', () => {
    expect(totalElegido(conItems, new Set([11]))).toBe(0);
  });

  it('alterna sin modificar el conjunto original', () => {
    const original = new Set([1]);
    const conDos = alternar(original, 2);

    expect([...conDos]).toEqual([1, 2]);
    expect([...original]).toEqual([1]);
    expect([...alternar(conDos, 1)]).toEqual([2]);
  });
});

describe('fechas', () => {
  it('formatea el día de hoy con dos dígitos', () => {
    expect(hoyISO(new Date(2026, 0, 5))).toBe('2026-01-05');
    expect(hoyISO(new Date(2026, 11, 31))).toBe('2026-12-31');
  });

  it('valida el formato y que el día exista', () => {
    expect(esFechaValida('2026-12-20')).toBe(true);
    expect(esFechaValida('2028-02-29')).toBe(true);
    expect(esFechaValida('2026-02-30')).toBe(false);
    expect(esFechaValida('20/12/2026')).toBe(false);
    expect(esFechaValida('')).toBe(false);
  });
});
