import { beforeEach, describe, expect, it, vi } from 'vitest';

const { get } = vi.hoisted(() => ({ get: vi.fn() }));

vi.mock('axios', () => ({
  default: {
    create: () => ({ defaults: { headers: { common: {} } }, get }),
  },
}));

import { reportesService } from './reportesService';

describe('reportesService', () => {
  beforeEach(() => {
    get.mockReset();
    get.mockResolvedValue({ data: {} });
  });

  it('pide el catálogo de reportes', async () => {
    await reportesService.catalogo();

    expect(get).toHaveBeenCalledWith('/reportes');
  });

  it('genera un reporte enviando solo los filtros elegidos', async () => {
    await reportesService.generar('alumnos-curso', { nivelId: 2, cursoId: undefined });

    expect(get).toHaveBeenCalledWith('/reportes/alumnos-curso', { params: { nivelId: 2 } });
  });
});
