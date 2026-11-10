import { etiquetaConcepto, etiquetaEstado, fecha, moneda, periodoLegible } from '../utils/formato';

describe('moneda', () => {
  it('usa punto para los miles y coma para los decimales', () => {
    expect(moneda(150000)).toBe('$ 150.000,00');
    expect(moneda(1234.5)).toBe('$ 1.234,50');
    expect(moneda(1234567.89)).toBe('$ 1.234.567,89');
  });

  it('muestra cero y negativos', () => {
    expect(moneda(0)).toBe('$ 0,00');
    expect(moneda(-10.5)).toBe('-$ 10,50');
  });
});

describe('fecha y período', () => {
  it('da vuelta la fecha ISO', () => {
    expect(fecha('2026-12-20')).toBe('20/12/2026');
    expect(fecha('2026-12-20T10:30:00')).toBe('20/12/2026');
  });

  it('escribe el mes con su nombre', () => {
    expect(periodoLegible('2026-12')).toBe('Diciembre 2026');
    expect(periodoLegible('2027-03')).toBe('Marzo 2027');
  });
});

describe('etiquetas', () => {
  it('traduce los estados y conceptos del servidor', () => {
    expect(etiquetaEstado('PARCIAL')).toBe('Pago parcial');
    expect(etiquetaEstado('PAGADO')).toBe('Pagada');
    expect(etiquetaEstado('VENCIDO')).toBe('Vencida');
    expect(etiquetaConcepto('CUOTA_BASE')).toBe('Cuota');
    expect(etiquetaConcepto('TRANSPORTE')).toBe('Transporte');
  });

  it('deja tal cual lo que no conoce', () => {
    expect(etiquetaEstado('OTRO')).toBe('OTRO');
    expect(etiquetaConcepto('OTRO')).toBe('OTRO');
  });
});
