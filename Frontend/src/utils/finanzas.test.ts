import { describe, expect, it } from 'vitest';
import type { FilaPagoFactura } from '../services/finanzasService';
import { agruparPorAnioYAlumno, fechaCorta, filtrarPorEstado, mesLegible, moneda, periodoSiguiente } from './finanzas';

function fila(anio: number, alumnoId: number, alumno: string, total: number, pagado: number, periodo = '2026-12'): FilaPagoFactura {
  return {
    anio, alumnoId, alumno, legajo: `ALU${alumnoId}`, facturaId: anio * 100 + alumnoId, factura: `F-${alumnoId}`, periodo,
    total, pagado, saldo: total - pagado, ultimoPago: `${anio}-11-09`,
  };
}

describe('moneda y fechas', () => {
  it('muestra los importes en pesos argentinos', () => {
    expect(moneda(150000).replace(/\s/g, ' ')).toBe('$ 150.000,00');
    expect(moneda(1234.5).replace(/\s/g, ' ')).toBe('$ 1.234,50');
  });

  it('da vuelta la fecha y escribe el mes con su nombre', () => {
    expect(fechaCorta('2026-12-20')).toBe('20/12/2026');
    expect(fechaCorta(null)).toBe('—');
    expect(mesLegible('2026-12')).toBe('Diciembre 2026');
  });

  it('el período a facturar es el mes siguiente, incluso al cambiar de año', () => {
    expect(periodoSiguiente(new Date(2026, 10, 9))).toBe('2026-12');
    expect(periodoSiguiente(new Date(2026, 11, 31))).toBe('2027-01');
  });
});

describe('agruparPorAnioYAlumno', () => {
  const filas = [
    fila(2027, 1, 'García, Carlos', 100, 100),
    fila(2026, 2, 'Martínez, Ana', 200, 150),
    fila(2026, 1, 'García, Carlos', 100, 40, '2026-11'),
    fila(2026, 1, 'García, Carlos', 100, 100, '2026-12'),
  ];

  it('ordena los años de menor a mayor', () => {
    expect(agruparPorAnioYAlumno(filas).map((g) => g.anio)).toEqual([2026, 2027]);
  });

  it('agrupa por alumno dentro de cada año y ordena por nombre', () => {
    const grupos = agruparPorAnioYAlumno(filas);

    expect(grupos[0].alumnos.map((a) => a.alumno)).toEqual(['García, Carlos', 'Martínez, Ana']);
    expect(grupos[0].alumnos[0].filas).toHaveLength(2);
    expect(grupos[1].alumnos).toHaveLength(1);
  });

  it('suma los subtotales de cada alumno y de cada año', () => {
    const [anio2026] = agruparPorAnioYAlumno(filas);

    expect(anio2026.alumnos[0]).toMatchObject({ total: 200, pagado: 140, saldo: 60 });
    expect(anio2026).toMatchObject({ total: 400, pagado: 290, saldo: 110 });
  });

  it('devuelve una lista vacía si no hay facturas', () => {
    expect(agruparPorAnioYAlumno([])).toEqual([]);
  });
});

describe('filtrarPorEstado', () => {
  const items = [
    { estado: 'PAGADO' as const, monto: 1 },
    { estado: 'PENDIENTE' as const, monto: 2 },
    { estado: 'PENDIENTE' as const, monto: 3 },
  ];

  it('filtra por estado o devuelve todo', () => {
    expect(filtrarPorEstado(items, 'PAGADO')).toHaveLength(1);
    expect(filtrarPorEstado(items, 'PENDIENTE')).toHaveLength(2);
    expect(filtrarPorEstado(items, 'TODOS')).toHaveLength(3);
  });
});
