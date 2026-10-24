import { describe, expect, it } from 'vitest';
import { paginar } from './paginacion';

const items = Array.from({ length: 23 }, (_, i) => i + 1);

describe('paginar', () => {
  it('devuelve la primera página por defecto', () => {
    expect(paginar(items, 0, 10)).toEqual({ visibles: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10], paginaActual: 0 });
  });

  it('devuelve la última página con los elementos restantes', () => {
    expect(paginar(items, 2, 10)).toEqual({ visibles: [21, 22, 23], paginaActual: 2 });
  });

  it('se queda en la última página si la lista se achicó', () => {
    expect(paginar(items.slice(0, 5), 3, 10)).toEqual({ visibles: [1, 2, 3, 4, 5], paginaActual: 0 });
  });

  it('una lista vacía no tiene páginas', () => {
    expect(paginar([], 4, 10)).toEqual({ visibles: [], paginaActual: 0 });
  });
});
